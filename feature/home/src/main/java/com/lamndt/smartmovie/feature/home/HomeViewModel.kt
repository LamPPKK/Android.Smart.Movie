package com.lamndt.smartmovie.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lamndt.smartmovie.model.CatalogRepository
import com.lamndt.smartmovie.model.CatalogV2Repository
import com.lamndt.smartmovie.model.HomeFeed
import com.lamndt.smartmovie.model.Loadable
import com.lamndt.smartmovie.model.MediaType
import com.lamndt.smartmovie.model.TitleSummary
import com.lamndt.smartmovie.model.CatalogEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val mediaType: MediaType = MediaType.MOVIE,
    val trendingWindow: String = "week",
    val feed: Loadable<HomeFeed> = Loadable.Idle,
    val trending: Loadable<List<TitleSummary>> = Loadable.Idle,
)

class HomeViewModel(
    private val catalog: CatalogRepository,
    private val language: String,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init { refresh() }

    fun selectMediaType(type: MediaType) {
        if (type == mutableState.value.mediaType) return
        mutableState.update { it.copy(mediaType = type) }
        refresh()
    }

    fun selectTrendingWindow(window: String) {
        if (window !in setOf("day", "week") || window == mutableState.value.trendingWindow) return
        mutableState.update { it.copy(trendingWindow = window) }
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableState.update { it.copy(feed = Loadable.Loading, trending = Loadable.Idle) }
            try {
                val result = catalog.home(mutableState.value.mediaType, language)
                val trending = (catalog as? CatalogV2Repository)?.let {
                    runCatching { it.trending(mutableState.value.mediaType.wireValue, mutableState.value.trendingWindow, 1, language, false) }
                        .getOrNull()?.results.orEmpty().mapNotNull { (it as? CatalogEntity.Title)?.value }
                }.orEmpty()
                mutableState.update { it.copy(feed = Loadable.Loaded(result), trending = Loadable.Loaded(trending)) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                mutableState.update { it.copy(feed = Loadable.Failed(failure.message.orEmpty())) }
            }
        }
    }

    companion object {
        fun factory(catalog: CatalogRepository, language: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(catalog, language) as T
            }
    }
}
