package com.lamndt.smartmovie.feature.home

import com.google.common.truth.Truth.assertThat
import com.lamndt.smartmovie.model.HomeFeed
import com.lamndt.smartmovie.model.Loadable
import com.lamndt.smartmovie.model.MediaType
import com.lamndt.smartmovie.model.CatalogEntity
import com.lamndt.smartmovie.model.PagedResult
import com.lamndt.smartmovie.model.TitleSummary
import com.lamndt.smartmovie.testing.FakeCatalogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsRefreshesAndSwitchesMediaType() = runTest(dispatcher) {
        val catalog = FakeCatalogRepository().apply { homeResult = { HomeFeed(it) } }
        val viewModel = HomeViewModel(catalog, "en-US")
        advanceUntilIdle()
        assertThat((viewModel.state.value.feed as Loadable.Loaded).value.mediaType).isEqualTo(MediaType.MOVIE)

        viewModel.selectMediaType(MediaType.TV)
        advanceUntilIdle()

        assertThat((viewModel.state.value.feed as Loadable.Loaded).value.mediaType).isEqualTo(MediaType.TV)
        assertThat(catalog.homeCalls).containsExactly(MediaType.MOVIE, MediaType.TV).inOrder()
    }

    @Test
    fun loadsWeeklyTrendingWhenV2IsAvailable() = runTest(dispatcher) {
        val trendingTitle = TitleSummary(42, MediaType.MOVIE, "Trending", "Trending", "")
        val catalog = com.lamndt.smartmovie.testing.FakeCatalogV2Repository().apply {
            legacy.homeResult = { HomeFeed(it) }
            trendingResult = { _, window, _, _, _ ->
                assertThat(window).isEqualTo("week")
                PagedResult(1, 1, listOf(CatalogEntity.Title(trendingTitle)))
            }
        }
        val viewModel = HomeViewModel(catalog, "en-US")
        advanceUntilIdle()
        assertThat((viewModel.state.value.trending as Loadable.Loaded).value).containsExactly(trendingTitle)
    }
}
