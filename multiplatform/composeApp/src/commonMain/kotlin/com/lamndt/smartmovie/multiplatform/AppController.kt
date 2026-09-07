package com.lamndt.smartmovie.multiplatform

import com.lamndt.smartmovie.multiplatform.data.CatalogApi
import com.lamndt.smartmovie.multiplatform.data.CatalogApiV2
import com.lamndt.smartmovie.multiplatform.data.AccountApi
import com.lamndt.smartmovie.multiplatform.data.AccountMutationPayload
import com.lamndt.smartmovie.multiplatform.data.KtorAccountApi
import com.lamndt.smartmovie.multiplatform.data.KtorCatalogApi
import com.lamndt.smartmovie.multiplatform.data.LibraryCollection
import com.lamndt.smartmovie.multiplatform.data.LibraryRecord
import com.lamndt.smartmovie.multiplatform.data.PersistentLibrary
import com.lamndt.smartmovie.multiplatform.data.PersistentEpisodeProgress
import com.lamndt.smartmovie.multiplatform.data.PersistentAccountMutationOutbox
import com.lamndt.smartmovie.multiplatform.data.PendingAccountMutation
import com.lamndt.smartmovie.multiplatform.data.ListItemMutation
import com.lamndt.smartmovie.multiplatform.data.applyPendingListDetail
import com.lamndt.smartmovie.multiplatform.data.applyPendingLists
import com.lamndt.smartmovie.multiplatform.data.createInstallationId
import com.lamndt.smartmovie.multiplatform.data.pinDigest
import com.lamndt.smartmovie.multiplatform.model.AppLocale
import com.lamndt.smartmovie.multiplatform.model.DiscoverFilter
import com.lamndt.smartmovie.multiplatform.model.DiscoverConfiguration
import com.lamndt.smartmovie.multiplatform.model.DiscoverSort
import com.lamndt.smartmovie.multiplatform.model.Genre
import com.lamndt.smartmovie.multiplatform.model.HomeFeed
import com.lamndt.smartmovie.multiplatform.model.ImageConfiguration
import com.lamndt.smartmovie.multiplatform.model.MediaType
import com.lamndt.smartmovie.multiplatform.model.SearchScope
import com.lamndt.smartmovie.multiplatform.model.TitleDetail
import com.lamndt.smartmovie.multiplatform.model.TitleSummary
import com.lamndt.smartmovie.multiplatform.model.UserList
import com.lamndt.smartmovie.multiplatform.model.AccountProfile
import com.lamndt.smartmovie.multiplatform.model.AuthAttempt
import com.lamndt.smartmovie.multiplatform.model.CatalogEntity
import com.lamndt.smartmovie.multiplatform.model.CapabilitiesV2
import com.lamndt.smartmovie.multiplatform.model.CastMember
import com.lamndt.smartmovie.multiplatform.model.CollectionDetail
import com.lamndt.smartmovie.multiplatform.model.Credit
import com.lamndt.smartmovie.multiplatform.model.CreditDetail
import com.lamndt.smartmovie.multiplatform.model.CatalogSearchMode
import com.lamndt.smartmovie.multiplatform.model.EpisodeDetail
import com.lamndt.smartmovie.multiplatform.model.ExternalIdSource
import com.lamndt.smartmovie.multiplatform.model.KeywordDetail
import com.lamndt.smartmovie.multiplatform.model.OrganizationDetail
import com.lamndt.smartmovie.multiplatform.model.PersonDetail
import com.lamndt.smartmovie.multiplatform.model.PagedResult
import com.lamndt.smartmovie.multiplatform.model.SearchScopeV2
import com.lamndt.smartmovie.multiplatform.model.SeasonDetail
import com.lamndt.smartmovie.multiplatform.model.TitleDetailV2
import com.lamndt.smartmovie.multiplatform.model.WatchMonetizationType
import com.lamndt.smartmovie.multiplatform.model.supportsAccountAuthentication
import com.lamndt.smartmovie.multiplatform.model.visibleCatalogCredits
import com.lamndt.smartmovie.multiplatform.model.visibleCatalogEntities
import com.lamndt.smartmovie.multiplatform.model.visibleCatalogTitles
import com.lamndt.smartmovie.multiplatform.platform.KeyValueStore
import com.lamndt.smartmovie.multiplatform.platform.catalogBaseUrl
import com.lamndt.smartmovie.multiplatform.platform.createKeyValueStore
import com.lamndt.smartmovie.multiplatform.platform.authReturnUri
import com.lamndt.smartmovie.multiplatform.platform.authMode
import com.lamndt.smartmovie.multiplatform.platform.openExternalUrl
import com.lamndt.smartmovie.multiplatform.platform.systemTimeMillis
import com.lamndt.smartmovie.multiplatform.platform.systemRegion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppTab { HOME, EXPLORE, SEARCH, LIBRARY, PROFILE }

sealed interface AccountState {
    data object Checking : AccountState
    data object SignedOut : AccountState
    data class Authorizing(val attempt: AuthAttempt) : AccountState
    data class SignedIn(val profile: AccountProfile) : AccountState
    data class Error(val message: String) : AccountState
}

sealed interface EntityDetail {
    data class Person(val value: PersonDetail) : EntityDetail
    data class Collection(val value: CollectionDetail) : EntityDetail
    data class Organization(val value: OrganizationDetail) : EntityDetail
    data class Keyword(val value: KeywordDetail) : EntityDetail
    data class Season(val value: SeasonDetail) : EntityDetail
    data class Episode(val value: EpisodeDetail) : EntityDetail
    data class Credit(val value: CreditDetail) : EntityDetail
}

sealed interface LoadState<out T> {
    data object Idle : LoadState<Nothing>
    data object Loading : LoadState<Nothing>
    data class Content<T>(val value: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}

data class SmartMovieState(
    val selectedTab: AppTab = AppTab.HOME,
    val locale: AppLocale = AppLocale.ENGLISH,
    val imageConfiguration: ImageConfiguration = ImageConfiguration.Fallback,
    val homeType: MediaType = MediaType.MOVIE,
    val home: LoadState<HomeFeed> = LoadState.Idle,
    val trendingWindow: String = "week",
    val trending: List<TitleSummary> = emptyList(),
    val exploreType: MediaType = MediaType.MOVIE,
    val exploreFilter: DiscoverFilter = DiscoverFilter(),
    val exploreDraftFilter: DiscoverFilter = DiscoverFilter(),
    val discoverConfiguration: DiscoverConfiguration? = null,
    val genres: List<Genre> = emptyList(),
    val explore: LoadState<List<TitleSummary>> = LoadState.Idle,
    val explorePage: Int = 0,
    val exploreTotalPages: Int = 1,
    val searchQuery: String = "",
    val searchMode: CatalogSearchMode = CatalogSearchMode.CATALOG,
    val searchScope: SearchScope = SearchScope.ALL,
    val searchScopeV2: SearchScopeV2 = SearchScopeV2.ALL,
    val search: LoadState<List<TitleSummary>> = LoadState.Idle,
    val entitySearch: LoadState<List<CatalogEntity>> = LoadState.Idle,
    val externalIdSource: ExternalIdSource = ExternalIdSource.IMDB,
    val externalIdSearch: LoadState<List<CatalogEntity>> = LoadState.Idle,
    val searchPage: Int = 0,
    val searchTotalPages: Int = 1,
    val libraryCollection: LibraryCollection = LibraryCollection.FAVORITES,
    val library: List<LibraryRecord> = emptyList(),
    val detail: LoadState<TitleDetail> = LoadState.Idle,
    val deepDetail: TitleDetailV2? = null,
    val detailSelection: TitleSummary? = null,
    val detailRating: AccountRatingState = AccountRatingState(),
    val entitySelection: CatalogEntity? = null,
    val creditSelection: String? = null,
    val entityDetail: LoadState<EntityDetail> = LoadState.Idle,
    val episodeRating: AccountRatingState = AccountRatingState(),
    val capabilities: CapabilitiesV2? = null,
    val account: AccountState = AccountState.Checking,
    val accountLists: LoadState<List<UserList>> = LoadState.Idle,
    val selectedAccountListId: Int? = null,
    val accountListDetail: LoadState<UserList> = LoadState.Idle,
    val accountListLoadingMore: Boolean = false,
    val accountListSearchQuery: String = "",
    val accountListSearch: LoadState<List<TitleSummary>> = LoadState.Idle,
    val accountRecommendationType: MediaType = MediaType.MOVIE,
    val accountRecommendations: LoadState<List<TitleSummary>> = LoadState.Idle,
    val accountRecommendationPage: Int = 0,
    val accountRecommendationTotalPages: Int = 1,
    val accountRecommendationsLoadingMore: Boolean = false,
    val accountRecommendationError: String? = null,
    val regionOverride: String? = null,
    val adultConfigured: Boolean = false,
    val adultUnlocked: Boolean = false,
    val adultFailures: Int = 0,
    val adultLockUntil: Long = 0,
    val watchedEpisodeKeys: Set<String> = emptySet(),
)

data class AccountRatingState(
    val value: Double? = null,
    val pending: Boolean = false,
    val error: String? = null,
)

class AppController(
    private val store: KeyValueStore = createKeyValueStore(),
    apiFactory: (String) -> CatalogApi = { KtorCatalogApi(baseUrl = catalogBaseUrl(), clientId = it) },
    accountFactory: ((String) -> AccountApi)? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val deviceRegion: String = systemRegion(),
    private val nowMillis: () -> Long = ::systemTimeMillis,
) {
    private val library = PersistentLibrary(store)
    private val episodeProgress = PersistentEpisodeProgress(store)
    private val accountOutbox = PersistentAccountMutationOutbox(store)
    private val clientId = store.getString(INSTALLATION_ID_KEY)
        ?.takeIf { it.length == 36 }
        ?: createInstallationId().also { store.putString(INSTALLATION_ID_KEY, it) }
    private val api = apiFactory(clientId)
    private val apiV2 = api as? CatalogApiV2
    private val accountApi = accountFactory?.invoke(clientId)
        ?: if (api is KtorCatalogApi) KtorAccountApi(catalogBaseUrl(), clientId) else null
    private val mutableState = MutableStateFlow(
        SmartMovieState(
            locale = AppLocale.fromTag(store.getString(LOCALE_KEY)),
            regionOverride = store.getString(REGION_KEY)?.takeIf(String::isNotBlank),
            adultConfigured = store.getString(ADULT_DIGEST_KEY) != null,
            adultFailures = store.getString(ADULT_FAILURES_KEY)?.toIntOrNull() ?: 0,
            adultLockUntil = store.getString(ADULT_LOCK_UNTIL_KEY)?.toLongOrNull() ?: 0,
        ),
    )
    val state: StateFlow<SmartMovieState> = mutableState.asStateFlow()

    private var homeJob: Job? = null
    private var exploreJob: Job? = null
    private var searchJob: Job? = null
    private var detailJob: Job? = null
    private var titleRatingJob: Job? = null
    private var episodeRatingJob: Job? = null
    private var recommendationsJob: Job? = null
    private var accountListJob: Job? = null
    private var accountListSearchJob: Job? = null
    private var accountListsRequestRevision = 0

    init {
        syncExploreContext(reload = false)
        scope.launch { library.records.collect { records -> mutableState.update { it.copy(library = records) } } }
        scope.launch { episodeProgress.watched.collect { keys -> mutableState.update { it.copy(watchedEpisodeKeys = keys) } } }
        scope.launch {
            val configuration = runCatching { api.imageConfiguration() }
                .propagateCancellation()
                .getOrDefault(ImageConfiguration.Fallback)
            mutableState.update { it.copy(imageConfiguration = configuration) }
        }
        scope.launch {
            apiV2?.let { v2 ->
                runCatching { v2.capabilities() }.onSuccess { value ->
                    val enabled = value.supportsCatalog("advanced_discover")
                    mutableState.update { current ->
                        val certificationCountry = current.exploreFilter.region.takeIf {
                            enabled && current.exploreType == MediaType.MOVIE
                        }
                        val applied = if (enabled) {
                            current.exploreFilter.copy(certificationCountry = certificationCountry)
                        } else {
                            current.exploreFilter.basic()
                        }
                        val draft = if (enabled) {
                            current.exploreDraftFilter.copy(certificationCountry = certificationCountry)
                        } else {
                            applied
                        }
                        current.copy(
                            capabilities = value,
                            exploreFilter = applied,
                            exploreDraftFilter = draft,
                            discoverConfiguration = if (enabled) current.discoverConfiguration else null,
                        )
                    }
                    reloadExploreConfiguration()
                    reloadExplore()
                }
            }
            refreshAccount()
        }
        reloadExploreConfiguration()
        reloadHome()
        reloadExplore()
    }

    fun selectTab(tab: AppTab) {
        mutableState.update {
            it.copy(
                selectedTab = tab,
                detailSelection = null,
                detail = LoadState.Idle,
                deepDetail = null,
                entitySelection = null,
                creditSelection = null,
                entityDetail = LoadState.Idle,
            )
        }
        if (tab == AppTab.HOME && state.value.home is LoadState.Idle) reloadHome()
        if (tab == AppTab.EXPLORE && state.value.explore is LoadState.Idle) reloadExplore()
        if (tab == AppTab.PROFILE && state.value.account is AccountState.SignedIn &&
            state.value.accountRecommendations is LoadState.Idle
        ) refreshRecommendations()
    }

    fun setEpisodeWatched(seriesId: Int, season: Int, episode: Int, watched: Boolean) =
        episodeProgress.setWatched("$seriesId:$season:$episode", watched)

    fun setSeasonWatched(seriesId: Int, season: Int, episodes: Collection<Int>, watched: Boolean) =
        episodeProgress.setSeason(seriesId, season, episodes, watched)

    fun changeTrendingWindow(window: String) {
        if (window !in setOf("day", "week") || window == state.value.trendingWindow) return
        mutableState.update { it.copy(trendingWindow = window) }
        reloadHome()
    }

    fun changeLocale(locale: AppLocale) {
        if (locale == state.value.locale) return
        accountListSearchJob?.cancel()
        store.putString(LOCALE_KEY, locale.tag)
        mutableState.update {
            it.copy(
                locale = locale,
                home = LoadState.Idle,
                explore = LoadState.Idle,
                search = LoadState.Idle,
                accountListSearch = LoadState.Idle,
            )
        }
        syncExploreContext(reload = false)
        reloadExploreConfiguration()
        reloadHome()
        reloadGenresAndExplore()
        if (state.value.searchQuery.isNotBlank()) {
            if (state.value.searchMode == CatalogSearchMode.CATALOG) scheduleSearch(immediate = true)
            else if (state.value.externalIdSearch !is LoadState.Idle) findExternalId()
        }
        if (state.value.account is AccountState.SignedIn) refreshRecommendations()
        if ((state.value.selectedAccountListId ?: -1) > 0) refreshAccountList()
    }

    fun changeHomeType(mediaType: MediaType) {
        if (mediaType == state.value.homeType) return
        mutableState.update { it.copy(homeType = mediaType) }
        reloadHome()
    }

    fun reloadHome() {
        homeJob?.cancel()
        homeJob = scope.launch {
            mutableState.update { it.copy(home = LoadState.Loading) }
            val snapshot = state.value
            runCatching { api.home(snapshot.homeType, snapshot.locale.backendTag) }
                .propagateCancellation()
                .onSuccess { feed ->
                    val titles = runCatching {
                        (api as? CatalogApiV2)?.trending(snapshot.homeType.wireValue, snapshot.trendingWindow, 1, snapshot.locale.backendTag, snapshot.adultUnlocked)
                            ?.results.orEmpty()
                            .mapNotNull { (it as? CatalogEntity.Title)?.value }
                    }.getOrDefault(emptyList())
                    mutableState.update { it.copy(home = LoadState.Content(feed), trending = titles) }
                }
                .onFailure { failure -> mutableState.update { it.copy(home = LoadState.Error(failure.message.orEmpty())) } }
        }
    }

    fun changeExploreType(mediaType: MediaType) {
        if (mediaType == state.value.exploreType) return
        mutableState.update {
            val current = it.exploreFilter
            val filter = DiscoverFilter(
                region = current.region,
                certificationCountry = current.region.takeIf { mediaType == MediaType.MOVIE },
                includeAdult = current.includeAdult,
            )
            it.copy(exploreType = mediaType, exploreFilter = filter, exploreDraftFilter = filter)
        }
        reloadGenresAndExplore()
    }

    fun setMinimumRating(rating: Double) {
        mutableState.update {
            it.copy(
                exploreFilter = it.exploreFilter.copy(minimumRating = rating),
                exploreDraftFilter = it.exploreDraftFilter.copy(minimumRating = rating),
            )
        }
        reloadExplore()
    }

    fun setExploreYear(year: Int?) {
        mutableState.update {
            it.copy(
                exploreFilter = it.exploreFilter.copy(year = year),
                exploreDraftFilter = it.exploreDraftFilter.copy(year = year),
            )
        }
        reloadExplore()
    }

    fun setExploreSort(sort: DiscoverSort) {
        mutableState.update {
            it.copy(
                exploreFilter = it.exploreFilter.copy(sort = sort),
                exploreDraftFilter = it.exploreDraftFilter.copy(sort = sort),
            )
        }
        reloadExplore()
    }

    fun toggleGenre(genreId: Int) {
        mutableState.update {
            val selected = it.exploreFilter.genres.toMutableSet().apply {
                if (!add(genreId)) remove(genreId)
            }
            it.copy(
                exploreFilter = it.exploreFilter.copy(genres = selected),
                exploreDraftFilter = it.exploreDraftFilter.copy(genres = selected),
            )
        }
        reloadExplore()
    }

    fun resetExplore() {
        mutableState.update {
            val current = it.exploreFilter
            it.copy(
                exploreFilter = DiscoverFilter(
                    region = current.region,
                    certificationCountry = if (it.exploreType == MediaType.MOVIE) current.region else null,
                    includeAdult = current.includeAdult,
                ),
                exploreDraftFilter = DiscoverFilter(
                    region = current.region,
                    certificationCountry = if (it.exploreType == MediaType.MOVIE) current.region else null,
                    includeAdult = current.includeAdult,
                ),
            )
        }
        reloadExplore()
    }

    fun updateExploreFilter(transform: (DiscoverFilter) -> DiscoverFilter) {
        mutableState.update { it.copy(exploreDraftFilter = transform(it.exploreDraftFilter)) }
    }

    fun toggleWatchProvider(providerId: Int) = updateExploreFilter { filter ->
        val providers = filter.watchProviderIds.toMutableSet().apply { if (!add(providerId)) remove(providerId) }
        filter.copy(watchProviderIds = providers)
    }

    fun toggleMonetization(type: WatchMonetizationType) = updateExploreFilter { filter ->
        val values = filter.monetizationTypes.toMutableSet().apply { if (!add(type)) remove(type) }
        filter.copy(monetizationTypes = values)
    }

    fun applyExploreFilters() {
        mutableState.update {
            val normalized = it.exploreDraftFilter.normalized().let { filter ->
                if (it.advancedDiscoverEnabled()) filter else filter.basic()
            }
            it.copy(exploreFilter = normalized, exploreDraftFilter = normalized)
        }
        reloadExplore()
    }

    fun reloadExplore() {
        exploreJob?.cancel()
        exploreJob = scope.launch {
            mutableState.update { it.copy(explore = LoadState.Loading, explorePage = 0) }
            val snapshot = state.value
            runCatching { discoverPage(snapshot, 1) }
                .propagateCancellation()
                .onSuccess { page -> mutableState.update { current ->
                    if (!current.matchesExplore(snapshot)) return@update current
                    current.copy(
                        explore = LoadState.Content(page.results.distinctBy(TitleSummary::libraryKey)),
                        explorePage = page.page,
                        exploreTotalPages = page.totalPages,
                    )
                } }
                .onFailure { failure -> mutableState.update { current ->
                    if (current.matchesExplore(snapshot)) current.copy(explore = LoadState.Error(failure.message.orEmpty())) else current
                } }
        }
    }

    fun loadMoreExplore() {
        val snapshot = state.value
        val content = (snapshot.explore as? LoadState.Content)?.value ?: return
        if (snapshot.explorePage >= snapshot.exploreTotalPages || exploreJob?.isActive == true) return
        exploreJob = scope.launch {
            runCatching {
                discoverPage(snapshot, snapshot.explorePage + 1)
            }.propagateCancellation().onSuccess { page -> mutableState.update { current ->
                if (!current.matchesExplore(snapshot) || current.explorePage != snapshot.explorePage) return@update current
                current.copy(
                    explore = LoadState.Content((content + page.results).distinctBy(TitleSummary::libraryKey)),
                    explorePage = page.page,
                    exploreTotalPages = page.totalPages,
                )
            } }
        }
    }

    private suspend fun discoverPage(snapshot: SmartMovieState, page: Int): PagedResult<TitleSummary> =
        if (snapshot.advancedDiscoverEnabled()) {
            api.discover(snapshot.exploreType, snapshot.exploreFilter, page, snapshot.locale.backendTag)
        } else {
            api.discoverBasic(snapshot.exploreType, snapshot.exploreFilter, page, snapshot.locale.backendTag)
        }

    fun updateSearchQuery(query: String) {
        if (state.value.searchMode == CatalogSearchMode.CATALOG) {
            mutableState.update { it.copy(searchQuery = query) }
            scheduleSearch(immediate = false)
        } else {
            searchJob?.cancel()
            mutableState.update { it.copy(searchQuery = query, externalIdSearch = LoadState.Idle) }
        }
    }

    fun changeSearchMode(mode: CatalogSearchMode) {
        if (mode == state.value.searchMode) return
        searchJob?.cancel()
        mutableState.update {
            it.copy(
                searchMode = mode,
                searchQuery = "",
                search = LoadState.Idle,
                entitySearch = LoadState.Idle,
                externalIdSearch = LoadState.Idle,
                searchPage = 0,
                searchTotalPages = 1,
            )
        }
    }

    fun changeExternalIdSource(source: ExternalIdSource) {
        if (source == state.value.externalIdSource) return
        searchJob?.cancel()
        mutableState.update { it.copy(externalIdSource = source, externalIdSearch = LoadState.Idle) }
    }

    fun findExternalId() {
        val snapshot = state.value
        val externalId = snapshot.searchQuery.trim()
        if (snapshot.searchMode != CatalogSearchMode.EXTERNAL_ID || externalId.isEmpty()) return
        val catalog = apiV2
        if (catalog == null) {
            mutableState.update {
                it.copy(externalIdSearch = LoadState.Error("External ID search requires the /v2 catalog"))
            }
            return
        }
        searchJob?.cancel()
        searchJob = scope.launch {
            mutableState.update { it.copy(externalIdSearch = LoadState.Loading) }
            runCatching {
                catalog.findExternalId(
                    externalId,
                    snapshot.externalIdSource,
                    snapshot.locale.backendTag,
                    includeAdult(snapshot),
                )
            }
                .propagateCancellation()
                .onSuccess { result ->
                    val current = state.value
                    if (current.searchMode == CatalogSearchMode.EXTERNAL_ID &&
                        current.searchQuery.trim() == externalId &&
                        current.externalIdSource == snapshot.externalIdSource
                    ) {
                        mutableState.update {
                            val visible = if (includeAdult(it)) result.results else result.results.withoutAdultTitles()
                            it.copy(externalIdSearch = LoadState.Content(visible.distinctBy(CatalogEntity::stableKey)))
                        }
                    }
                }
                .onFailure { failure ->
                    val current = state.value
                    if (current.searchMode == CatalogSearchMode.EXTERNAL_ID &&
                        current.searchQuery.trim() == externalId &&
                        current.externalIdSource == snapshot.externalIdSource
                    ) {
                        mutableState.update { it.copy(externalIdSearch = LoadState.Error(failure.message.orEmpty())) }
                    }
                }
        }
    }

    fun changeSearchScope(scope: SearchScope) {
        mutableState.update { it.copy(searchScope = scope) }
        scheduleSearch(immediate = true)
    }

    fun changeSearchScope(scope: SearchScopeV2) {
        mutableState.update { it.copy(searchScopeV2 = scope) }
        scheduleSearch(immediate = true)
    }

    fun retrySearch() {
        if (state.value.searchMode == CatalogSearchMode.EXTERNAL_ID) findExternalId()
        else scheduleSearch(immediate = true)
    }

    fun loadMoreSearch() {
        val snapshot = state.value
        if (snapshot.searchMode != CatalogSearchMode.CATALOG) return
        if (snapshot.searchPage >= snapshot.searchTotalPages || searchJob?.isActive == true) return
        searchJob = scope.launch {
            if (apiV2 != null) {
                runCatching {
                    apiV2.searchEntities(
                        snapshot.searchQuery,
                        snapshot.searchScopeV2,
                        snapshot.searchPage + 1,
                        snapshot.locale.backendTag,
                        snapshot.regionOverride,
                        includeAdult(snapshot),
                    )
                }.propagateCancellation().onSuccess { page -> mutableState.update { current ->
                    if (current.searchQuery != snapshot.searchQuery || current.searchScopeV2 != snapshot.searchScopeV2) {
                        return@update current
                    }
                    val includeAdult = includeAdult(current)
                    val existingEntities = (current.entitySearch as? LoadState.Content)?.value.orEmpty()
                    val entities = (existingEntities + page.results)
                        .distinctBy(CatalogEntity::stableKey)
                        .let { results -> if (includeAdult) results else results.withoutAdultTitles() }
                    val existingTitles = (current.search as? LoadState.Content)?.value.orEmpty()
                    val titles = (existingTitles + page.results.mapNotNull { entity ->
                        (entity as? CatalogEntity.Title)?.value
                    })
                        .distinctBy(TitleSummary::libraryKey)
                        .filter { includeAdult || !it.adult }
                    current.copy(
                        search = LoadState.Content(titles),
                        entitySearch = LoadState.Content(entities),
                        searchPage = page.page,
                        searchTotalPages = page.totalPages,
                    )
                } }
            } else {
                runCatching {
                    api.search(
                        snapshot.searchQuery,
                        snapshot.searchScope,
                        snapshot.searchPage + 1,
                        snapshot.locale.backendTag,
                    )
                }.propagateCancellation().onSuccess { page -> mutableState.update { current ->
                    if (current.searchQuery != snapshot.searchQuery || current.searchScope != snapshot.searchScope) {
                        return@update current
                    }
                    val includeAdult = includeAdult(current)
                    val currentTitles = (current.search as? LoadState.Content)?.value.orEmpty()
                    val results = (currentTitles + page.results)
                        .distinctBy(TitleSummary::libraryKey)
                        .filter { includeAdult || !it.adult }
                    val currentEntities = (current.entitySearch as? LoadState.Content)?.value.orEmpty()
                    val entities = (currentEntities + page.results.map(CatalogEntity::Title))
                        .distinctBy(CatalogEntity::stableKey)
                        .let { values -> if (includeAdult) values else values.withoutAdultTitles() }
                    current.copy(
                        search = LoadState.Content(results),
                        entitySearch = LoadState.Content(entities),
                        searchPage = page.page,
                        searchTotalPages = page.totalPages,
                    )
                } }
            }
        }
    }

    fun openDetail(title: TitleSummary) {
        detailJob?.cancel()
        mutableState.update {
            it.copy(
                detailSelection = title,
                detail = LoadState.Loading,
                detailRating = AccountRatingState(),
                entitySelection = null,
                creditSelection = null,
            )
        }
        refreshTitleRating(title)
        detailJob = scope.launch {
            val snapshot = state.value
            runCatching {
                apiV2?.deepDetail(
                    title.mediaType,
                    title.id,
                    snapshot.locale.backendTag,
                    effectiveRegion(snapshot),
                    includeAdult(snapshot),
                )
            }
                .propagateCancellation()
                .onSuccess { deep ->
                    if (deep != null) mutableState.update { current ->
                        if (current.detailSelection?.libraryKey != title.libraryKey || (!includeAdult(current) && deep.adult)) {
                            return@update current.copy(
                                detail = LoadState.Idle,
                                deepDetail = null,
                                detailSelection = null,
                                detailRating = AccountRatingState(),
                            )
                        }
                        current.copy(detail = LoadState.Content(deep.toLegacy()), deepDetail = deep)
                    }
                    else runCatching { api.detail(title.mediaType, title.id, snapshot.locale.backendTag) }
                        .onSuccess { detail -> mutableState.update { current ->
                            if (current.detailSelection?.libraryKey != title.libraryKey || (!includeAdult(current) && title.adult)) {
                                current
                            } else current.copy(detail = LoadState.Content(detail))
                        } }
                        .onFailure { failure -> mutableState.update { it.copy(detail = LoadState.Error(failure.message.orEmpty())) } }
                }
                .onFailure { failure -> mutableState.update { it.copy(detail = LoadState.Error(failure.message.orEmpty())) } }
        }
    }

    fun openEntity(entity: CatalogEntity) {
        if (entity is CatalogEntity.Title) {
            openDetail(entity.value)
            return
        }
        val catalog = apiV2 ?: return
        detailJob?.cancel()
        mutableState.update {
            it.copy(
                detailSelection = null,
                entitySelection = entity,
                creditSelection = null,
                entityDetail = LoadState.Loading,
                episodeRating = AccountRatingState(),
            )
        }
        if (entity is CatalogEntity.Episode) refreshEpisodeRating(entity.value.seriesId, entity.value.seasonNumber, entity.value.episodeNumber)
        detailJob = scope.launch {
            val snapshot = state.value
            val language = snapshot.locale.backendTag
            val allowAdult = includeAdult(snapshot)
            runCatching {
                when (entity) {
                    is CatalogEntity.Person -> EntityDetail.Person(catalog.person(entity.value.id, language, allowAdult))
                    is CatalogEntity.Collection -> EntityDetail.Collection(catalog.collection(entity.value.id, language, allowAdult))
                    is CatalogEntity.Organization -> EntityDetail.Organization(
                        catalog.organization(entity.value.entityKind, entity.value.id, language, 1, allowAdult),
                    )
                    is CatalogEntity.Keyword -> EntityDetail.Keyword(catalog.keyword(entity.value.id, language, 1, allowAdult))
                    is CatalogEntity.Season -> {
                        val seriesId = requireNotNull(entity.value.seriesId) { "Season is missing its series context." }
                        EntityDetail.Season(catalog.season(seriesId, entity.value.seasonNumber, language))
                    }
                    is CatalogEntity.Episode -> EntityDetail.Episode(
                        catalog.episode(entity.value.seriesId, entity.value.seasonNumber, entity.value.episodeNumber, language),
                    )
                    is CatalogEntity.Title -> error("Title detail is handled separately")
                }
            }.propagateCancellation()
                .onSuccess { detail ->
                    mutableState.update {
                        it.copy(entityDetail = LoadState.Content(detail.withAdultVisibility(includeAdult(it))))
                    }
                }
                .onFailure { error -> mutableState.update { it.copy(entityDetail = LoadState.Error(error.message.orEmpty())) } }
        }
    }

    fun openCredit(credit: Credit) {
        val creditID = credit.creditId ?: return
        openCredit(creditID)
    }

    private fun openCredit(creditID: String) {
        val catalog = apiV2 ?: return
        detailJob?.cancel()
        mutableState.update {
            it.copy(
                detailSelection = null,
                entitySelection = null,
                creditSelection = creditID,
                entityDetail = LoadState.Loading,
            )
        }
        detailJob = scope.launch {
            val snapshot = state.value
            runCatching {
                EntityDetail.Credit(
                    catalog.credit(creditID, snapshot.locale.backendTag, includeAdult(snapshot)),
                )
            }
                .propagateCancellation()
                .onSuccess { detail ->
                    mutableState.update {
                        it.copy(entityDetail = LoadState.Content(detail.withAdultVisibility(includeAdult(it))))
                    }
                }
                .onFailure { error -> mutableState.update { it.copy(entityDetail = LoadState.Error(error.message.orEmpty())) } }
        }
    }

    fun closeDetail() {
        detailJob?.cancel()
        titleRatingJob?.cancel()
        episodeRatingJob?.cancel()
        mutableState.update {
            it.copy(
                detailSelection = null,
                detail = LoadState.Idle,
                deepDetail = null,
                detailRating = AccountRatingState(),
                entitySelection = null,
                creditSelection = null,
                entityDetail = LoadState.Idle,
                episodeRating = AccountRatingState(),
            )
        }
    }

    fun retryDetail() = state.value.detailSelection?.let(::openDetail)

    fun retryEntityDetail() {
        state.value.creditSelection?.let(::openCredit)
            ?: state.value.entitySelection?.let(::openEntity)
    }

    fun toggleLibrary(title: TitleSummary, collection: LibraryCollection) {
        library.toggle(title, collection)
        scope.launch { flushLibraryOutbox() }
    }

    fun rateTitle(value: Double?) {
        val profile = (state.value.account as? AccountState.SignedIn)?.profile ?: return
        val title = state.value.detailSelection ?: return
        val rating = normalizeRating(value)
        accountOutbox.enqueue(profile.id, AccountMutationPayload.TitleRating(title.mediaType, title.id, rating))
        mutableState.update { it.copy(detailRating = AccountRatingState(rating, pending = true)) }
        scope.launch { flushAccountOutbox(profile.id) }
    }

    fun rateEpisode(value: Double?) {
        val profile = (state.value.account as? AccountState.SignedIn)?.profile ?: return
        val episode = (state.value.entitySelection as? CatalogEntity.Episode)?.value ?: return
        val rating = normalizeRating(value)
        accountOutbox.enqueue(
            profile.id,
            AccountMutationPayload.EpisodeRating(
                episode.seriesId,
                episode.seasonNumber,
                episode.episodeNumber,
                rating,
            ),
        )
        mutableState.update { it.copy(episodeRating = AccountRatingState(rating, pending = true)) }
        scope.launch { flushAccountOutbox(profile.id) }
    }

    fun changeLibraryCollection(collection: LibraryCollection) {
        mutableState.update { it.copy(libraryCollection = collection) }
    }

    fun setRegion(region: String?) {
        val normalized = region?.trim()?.uppercase()?.takeIf { it.matches(Regex("[A-Z]{2}")) }
        store.putString(REGION_KEY, normalized.orEmpty())
        mutableState.update { it.copy(regionOverride = normalized) }
        syncExploreContext(reload = true)
        reloadExploreConfiguration()
    }

    fun contentRegion(): String = effectiveRegion(state.value)

    fun configureAdultPin(pin: String, confirmation: String, ageConfirmed: Boolean): Boolean {
        if (!isValidAdultConfiguration(pin, confirmation, ageConfirmed)) return false
        val salt = createInstallationId()
        store.putString(ADULT_SALT_KEY, salt)
        store.putString(ADULT_DIGEST_KEY, pinDigest(salt, pin))
        resetAdultFailures()
        mutableState.update { it.copy(adultConfigured = true, adultUnlocked = true) }
        syncExploreContext(reload = true)
        refreshRecommendations()
        refreshAccountList()
        return true
    }

    fun unlockAdult(pin: String): Boolean {
        val snapshot = state.value
        if (snapshot.adultLockUntil > nowMillis()) return false
        val salt = store.getString(ADULT_SALT_KEY) ?: return false
        val digest = store.getString(ADULT_DIGEST_KEY) ?: return false
        if (pinDigest(salt, pin) == digest) {
            resetAdultFailures()
            mutableState.update { it.copy(adultUnlocked = true) }
            syncExploreContext(reload = true)
            refreshRecommendations()
            refreshAccountList()
            return true
        }
        val failures = snapshot.adultFailures + 1
        val lockUntil = if (failures >= MAX_ADULT_FAILURES) nowMillis() + ADULT_LOCK_MILLIS else 0
        val persistedFailures = if (lockUntil > 0) 0 else failures
        store.putString(ADULT_FAILURES_KEY, persistedFailures.toString())
        store.putString(ADULT_LOCK_UNTIL_KEY, lockUntil.toString())
        mutableState.update {
            it.copy(adultFailures = persistedFailures, adultLockUntil = lockUntil, adultUnlocked = false)
        }
        return false
    }

    fun lockAdult() {
        searchJob?.cancel()
        detailJob?.cancel()
        recommendationsJob?.cancel()
        accountListJob?.cancel()
        accountListSearchJob?.cancel()
        mutableState.update { snapshot ->
            val visible = (snapshot.accountRecommendations as? LoadState.Content)?.value
                ?.filterNot(TitleSummary::adult)
            val list = (snapshot.accountListDetail as? LoadState.Content)?.value
                ?.let { value -> value.copy(results = value.results.filterNot(TitleSummary::adult)) }
            val search = (snapshot.accountListSearch as? LoadState.Content)?.value
                ?.filterNot(TitleSummary::adult)
            val catalogSearch = (snapshot.search as? LoadState.Content)?.value
                ?.filterNot(TitleSummary::adult)
            val entitySearch = (snapshot.entitySearch as? LoadState.Content)?.value
                ?.withoutAdultTitles()
            val externalSearch = (snapshot.externalIdSearch as? LoadState.Content)?.value
                ?.withoutAdultTitles()
            val adultDetail = snapshot.detailSelection?.adult == true || snapshot.deepDetail?.adult == true
            val entityDetail = (snapshot.entityDetail as? LoadState.Content)?.value
                ?.withAdultVisibility(includeAdult = false)
            snapshot.copy(
                adultUnlocked = false,
                search = catalogSearch?.let { LoadState.Content(it) } ?: snapshot.search,
                entitySearch = entitySearch?.let { LoadState.Content(it) } ?: snapshot.entitySearch,
                externalIdSearch = externalSearch?.let { LoadState.Content(it) } ?: snapshot.externalIdSearch,
                searchTotalPages = if (catalogSearch != null) snapshot.searchPage.coerceAtLeast(1) else snapshot.searchTotalPages,
                detail = if (adultDetail) LoadState.Idle else snapshot.detail,
                deepDetail = if (adultDetail) null else snapshot.deepDetail,
                detailSelection = if (adultDetail) null else snapshot.detailSelection,
                detailRating = if (adultDetail) AccountRatingState() else snapshot.detailRating,
                entityDetail = entityDetail?.let { LoadState.Content(it) } ?: snapshot.entityDetail,
                accountRecommendations = visible?.let { LoadState.Content(it) } ?: snapshot.accountRecommendations,
                accountListDetail = list?.let { LoadState.Content(it) } ?: snapshot.accountListDetail,
                accountListSearch = search?.let { LoadState.Content(it) } ?: LoadState.Idle,
                accountRecommendationsLoadingMore = false,
            )
        }
        syncExploreContext(reload = true)
    }

    fun disableAdult() {
        listOf(ADULT_SALT_KEY, ADULT_DIGEST_KEY, ADULT_FAILURES_KEY, ADULT_LOCK_UNTIL_KEY).forEach(store::remove)
        lockAdult()
        mutableState.update {
            it.copy(adultConfigured = false, adultUnlocked = false, adultFailures = 0, adultLockUntil = 0)
        }
    }

    fun beginSignIn(mode: String = authMode()) {
        if (!state.value.capabilities.supportsAccountAuthentication()) return
        val account = accountApi ?: return
        scope.launch {
            mutableState.update { it.copy(account = AccountState.Checking) }
            runCatching { account.createAuthAttempt(authReturnUri(), mode) }
                .propagateCancellation()
                .onSuccess { attempt ->
                    mutableState.update { it.copy(account = AccountState.Authorizing(attempt)) }
                    openExternalUrl(attempt.authorizationUrl)
                    pollAndCompleteAuth(attempt)
                }
                .onFailure { error -> mutableState.update { it.copy(account = AccountState.Error(error.message.orEmpty())) } }
        }
    }

    fun cancelSignIn() {
        mutableState.update { it.copy(account = AccountState.SignedOut) }
    }

    fun signOut(keepAsLocal: Boolean) {
        val account = accountApi ?: return
        val accountId = (state.value.account as? AccountState.SignedIn)?.profile?.id
        recommendationsJob?.cancel()
        scope.launch {
            runCatching { account.logout() }
            library.deactivateAccount(removeAccountData = !keepAsLocal)
            if (!keepAsLocal && accountId != null) accountOutbox.clear(accountId)
            mutableState.update {
                it.copy(
                    account = AccountState.SignedOut,
                    accountLists = LoadState.Idle,
                    selectedAccountListId = null,
                    accountListDetail = LoadState.Idle,
                    accountListLoadingMore = false,
                    accountListSearchQuery = "",
                    accountListSearch = LoadState.Idle,
                    accountRecommendations = LoadState.Idle,
                    accountRecommendationPage = 0,
                    accountRecommendationTotalPages = 1,
                    accountRecommendationsLoadingMore = false,
                    accountRecommendationError = null,
                    detailRating = AccountRatingState(),
                    episodeRating = AccountRatingState(),
                )
            }
        }
    }

    fun refreshLists() {
        val profile = (state.value.account as? AccountState.SignedIn)?.profile ?: return
        scope.launch { refreshLists(profile.id) }
    }

    fun selectRecommendationType(mediaType: MediaType) {
        if (mediaType == state.value.accountRecommendationType) return
        mutableState.update {
            it.copy(
                accountRecommendationType = mediaType,
                accountRecommendations = LoadState.Idle,
                accountRecommendationPage = 0,
                accountRecommendationTotalPages = 1,
                accountRecommendationError = null,
            )
        }
        refreshRecommendations()
    }

    fun refreshRecommendations() {
        loadRecommendations(reset = true)
    }

    fun loadMoreRecommendations() {
        val snapshot = state.value
        if (snapshot.accountRecommendationsLoadingMore || snapshot.accountRecommendationPage !in 1 until snapshot.accountRecommendationTotalPages) return
        loadRecommendations(reset = false)
    }

    fun createList(name: String, description: String) {
        val profile = (state.value.account as? AccountState.SignedIn)?.profile ?: return
        val normalized = name.trim().takeIf(String::isNotEmpty) ?: return
        accountOutbox.enqueue(
            profile.id,
            AccountMutationPayload.CreateList(
                normalized,
                description.trim(),
                public = false,
                region = state.value.regionOverride ?: "US",
                language = state.value.locale.tag.substringBefore('-'),
            ),
        )
        publishOptimisticLists(profile.id)
        scope.launch { flushAccountOutbox(profile.id) }
    }

    fun deleteList(id: Int) {
        val profile = (state.value.account as? AccountState.SignedIn)?.profile ?: return
        if (id < 0) {
            accountOutbox.pending(profile.id).firstOrNull { it.localListId == id }?.let { accountOutbox.cancel(it.id) }
        } else {
            accountOutbox.enqueue(profile.id, AccountMutationPayload.DeleteList(id))
        }
        publishOptimisticLists(profile.id)
        if (state.value.selectedAccountListId == id) closeAccountList()
        scope.launch { flushAccountOutbox(profile.id) }
    }

    fun updateList(id: Int, name: String, description: String, public: Boolean) {
        val profile = (state.value.account as? AccountState.SignedIn)?.profile ?: return
        val normalized = name.trim().takeIf(String::isNotEmpty) ?: return
        accountOutbox.enqueue(
            profile.id,
            AccountMutationPayload.UpdateList(id, normalized, description.trim(), public),
        )
        publishOptimisticLists(profile.id)
        mutableState.update { snapshot ->
            val detail = (snapshot.accountListDetail as? LoadState.Content)?.value
            snapshot.copy(
                accountListDetail = if (detail?.id == id) {
                    LoadState.Content(detail.copy(name = normalized, description = description.trim(), public = public))
                } else snapshot.accountListDetail,
            )
        }
        scope.launch { flushAccountOutbox(profile.id) }
    }

    fun mutateListItems(
        id: Int,
        items: List<ListItemMutation>,
        titles: List<TitleSummary> = emptyList(),
        remove: Boolean,
    ) {
        val profile = (state.value.account as? AccountState.SignedIn)?.profile ?: return
        if (items.isEmpty()) return
        accountOutbox.enqueue(profile.id, AccountMutationPayload.MutateListItems(id, items, titles, remove))
        scope.launch { flushAccountOutbox(profile.id) }
    }

    fun openAccountList(id: Int) {
        val snapshot = state.value
        val summary = (snapshot.accountLists as? LoadState.Content)?.value
            ?.firstOrNull { it.id == id }
            ?.let { list -> list.copy(results = list.results.filter { includeAdult(snapshot) || !it.adult }) }
        mutableState.update {
            it.copy(
                selectedAccountListId = id,
                accountListDetail = summary?.let { value -> LoadState.Content(value) } ?: LoadState.Loading,
                accountListLoadingMore = false,
                accountListSearchQuery = "",
                accountListSearch = LoadState.Idle,
            )
        }
        if (id > 0) loadAccountList(reset = true)
    }

    fun closeAccountList() {
        accountListJob?.cancel()
        accountListSearchJob?.cancel()
        mutableState.update {
            it.copy(
                selectedAccountListId = null,
                accountListDetail = LoadState.Idle,
                accountListLoadingMore = false,
                accountListSearchQuery = "",
                accountListSearch = LoadState.Idle,
            )
        }
    }

    fun refreshAccountList() {
        if ((state.value.selectedAccountListId ?: -1) > 0) loadAccountList(reset = true)
    }

    fun loadMoreAccountList() {
        val detail = (state.value.accountListDetail as? LoadState.Content)?.value ?: return
        if (state.value.accountListLoadingMore || (detail.page ?: 0) !in 1 until (detail.totalPages ?: 1)) return
        loadAccountList(reset = false)
    }

    fun searchAccountList(query: String) {
        val normalized = query.trim()
        val list = (state.value.accountListDetail as? LoadState.Content)?.value ?: return
        if (list.id < 0 || normalized.isEmpty()) return
        accountListSearchJob?.cancel()
        accountListSearchJob = scope.launch {
            val snapshot = state.value
            mutableState.update {
                it.copy(accountListSearchQuery = normalized, accountListSearch = LoadState.Loading)
            }
            runCatching { api.search(normalized, SearchScope.ALL, 1, snapshot.locale.backendTag) }
                .propagateCancellation()
                .onSuccess { page ->
                    mutableState.update { current ->
                        if (current.selectedAccountListId != list.id ||
                            current.accountListSearchQuery != normalized ||
                            current.locale != snapshot.locale
                        ) {
                            return@update current
                        }
                        val currentList = (current.accountListDetail as? LoadState.Content)?.value ?: list
                        current.copy(
                            accountListSearch = LoadState.Content(
                                filterAccountListSearchResults(page.results, currentList.results, includeAdult(current)),
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    mutableState.update { current ->
                        if (current.selectedAccountListId == list.id &&
                            current.accountListSearchQuery == normalized &&
                            current.locale == snapshot.locale
                        ) {
                            current.copy(accountListSearch = LoadState.Error(error.message.orEmpty()))
                        } else current
                    }
                }
        }
    }

    fun changeAccountListSearchQuery(query: String) {
        accountListSearchJob?.cancel()
        mutableState.update {
            it.copy(accountListSearchQuery = query, accountListSearch = LoadState.Idle)
        }
    }

    fun addAccountListTitle(title: TitleSummary) {
        val list = (state.value.accountListDetail as? LoadState.Content)?.value ?: return
        if (list.id < 0) return
        mutateListItems(
            list.id,
            listOf(ListItemMutation(title.mediaType.wireValue, title.id)),
            titles = listOf(title),
            remove = false,
        )
        mutableState.update { snapshot ->
            snapshot.copy(
                accountListDetail = LoadState.Content(list.copy(results = (list.results + title).distinctBy(TitleSummary::libraryKey))),
                accountListSearch = when (val search = snapshot.accountListSearch) {
                    is LoadState.Content -> LoadState.Content(search.value.filterNot { it.libraryKey == title.libraryKey })
                    else -> search
                },
            )
        }
    }

    fun removeAccountListTitle(title: TitleSummary) {
        val list = (state.value.accountListDetail as? LoadState.Content)?.value ?: return
        if (list.id < 0) return
        mutateListItems(
            list.id,
            listOf(ListItemMutation(title.mediaType.wireValue, title.id)),
            titles = listOf(title),
            remove = true,
        )
        mutableState.update {
            it.copy(accountListDetail = LoadState.Content(list.copy(results = list.results.filterNot { value -> value.libraryKey == title.libraryKey })))
        }
    }

    fun close() = scope.cancel()

    private fun loadRecommendations(reset: Boolean) {
        val profile = (state.value.account as? AccountState.SignedIn)?.profile ?: return
        recommendationsJob?.cancel()
        recommendationsJob = scope.launch { refreshRecommendations(profile.id, reset) }
    }

    private suspend fun refreshRecommendations(accountId: Int, reset: Boolean) {
        val account = accountApi ?: return
        val snapshot = state.value
        val signedInId = (snapshot.account as? AccountState.SignedIn)?.profile?.id ?: return
        if (signedInId != accountId) return
        val mediaType = snapshot.accountRecommendationType
        val existing = if (reset) emptyList() else {
            (snapshot.accountRecommendations as? LoadState.Content)?.value.orEmpty()
        }
        val requestedPage = if (reset) 1 else snapshot.accountRecommendationPage + 1
        mutableState.update {
            if (reset) {
                it.copy(
                    accountRecommendations = LoadState.Loading,
                    accountRecommendationPage = 0,
                    accountRecommendationTotalPages = 1,
                    accountRecommendationsLoadingMore = false,
                    accountRecommendationError = null,
                )
            } else {
                it.copy(accountRecommendationsLoadingMore = true, accountRecommendationError = null)
            }
        }
        runCatching { account.recommendations(mediaType, requestedPage, snapshot.locale.backendTag) }
            .propagateCancellation()
            .onSuccess { page ->
                val current = state.value
                if ((current.account as? AccountState.SignedIn)?.profile?.id != accountId ||
                    current.accountRecommendationType != mediaType
                ) return@onSuccess
                mutableState.update {
                    it.copy(
                        accountRecommendations = LoadState.Content(
                            mergeAccountRecommendations(existing, page, includeAdult(it)),
                        ),
                        accountRecommendationPage = page.page,
                        accountRecommendationTotalPages = page.totalPages,
                        accountRecommendationsLoadingMore = false,
                        accountRecommendationError = null,
                    )
                }
            }
            .onFailure { error ->
                val current = state.value
                if ((current.account as? AccountState.SignedIn)?.profile?.id != accountId ||
                    current.accountRecommendationType != mediaType
                ) return@onFailure
                mutableState.update {
                    if (reset) {
                        it.copy(
                            accountRecommendations = LoadState.Error(error.message.orEmpty()),
                            accountRecommendationsLoadingMore = false,
                        )
                    } else {
                        it.copy(
                            accountRecommendationsLoadingMore = false,
                            accountRecommendationError = error.message.orEmpty(),
                        )
                    }
                }
            }
    }

    private suspend fun refreshLists(accountId: Int) {
        val account = accountApi ?: return
        val requestRevision = ++accountListsRequestRevision
        val initialPending = accountOutbox.pending(accountId)
        val current = (state.value.accountLists as? LoadState.Content)?.value.orEmpty().filter { it.id > 0 }
        val hasPendingListMutation = initialPending.any { it.payload.isListMutation() }
        if (current.isEmpty() && !hasPendingListMutation) {
            mutableState.update { it.copy(accountLists = LoadState.Loading) }
        }
        runCatching { loadAllAccountLists(loadPage = account::lists) }
            .propagateCancellation()
            .onSuccess { lists ->
                val pending = accountOutbox.pending(accountId)
                mutableState.update { currentState ->
                    val currentAccountId = (currentState.account as? AccountState.SignedIn)?.profile?.id
                    if (requestRevision != accountListsRequestRevision || currentAccountId != accountId) currentState
                    else currentState.copy(accountLists = LoadState.Content(applyPendingLists(lists, pending)))
                }
            }
            .onFailure { error ->
                val pending = accountOutbox.pending(accountId)
                mutableState.update { currentState ->
                    val currentAccountId = (currentState.account as? AccountState.SignedIn)?.profile?.id
                    if (requestRevision != accountListsRequestRevision || currentAccountId != accountId) return@update currentState
                    currentState.copy(
                        accountLists = if (pending.any { it.payload.isListMutation() }) {
                            LoadState.Content(applyPendingLists(current, pending))
                        } else {
                            LoadState.Error(error.message.orEmpty())
                        },
                    )
                }
            }
    }

    private fun loadAccountList(reset: Boolean) {
        val account = accountApi ?: return
        val listId = state.value.selectedAccountListId ?: return
        if (listId < 0) return
        if (!reset && accountListJob?.isActive == true) return
        if (reset) accountListJob?.cancel()
        val existing = (state.value.accountListDetail as? LoadState.Content)?.value
        val requestedPage = if (reset) 1 else (existing?.page ?: 1) + 1
        mutableState.update {
            if (reset) it.copy(accountListDetail = LoadState.Loading, accountListLoadingMore = false)
            else it.copy(accountListLoadingMore = true)
        }
        accountListJob = scope.launch {
            val snapshot = state.value
            val profile = (snapshot.account as? AccountState.SignedIn)?.profile ?: return@launch
            runCatching { account.list(listId, requestedPage, snapshot.locale.backendTag) }
                .propagateCancellation()
                .onSuccess { page ->
                    mutableState.update { current ->
                        if (current.selectedAccountListId != listId || current.locale != snapshot.locale) {
                            return@update current
                        }
                        val merged = applyPendingListDetail(
                            mergeAccountListPage(if (reset) null else existing, page, includeAdult(current)),
                            accountOutbox.pending(profile.id),
                            includeAdult(current),
                        ) ?: return@update current.copy(
                            selectedAccountListId = null,
                            accountListDetail = LoadState.Idle,
                            accountListLoadingMore = false,
                        )
                        current.copy(
                            accountListDetail = LoadState.Content(merged),
                            accountListLoadingMore = false,
                        )
                    }
                }
                .onFailure { error ->
                    mutableState.update { current ->
                        if (current.selectedAccountListId != listId || current.locale != snapshot.locale) {
                            return@update current
                        }
                        val cached = existing?.let {
                            applyPendingListDetail(it, accountOutbox.pending(profile.id), includeAdult(current))
                        }
                        current.copy(
                            accountListDetail = when {
                                reset && cached != null -> LoadState.Content(cached)
                                reset -> LoadState.Error(error.message.orEmpty())
                                else -> current.accountListDetail
                            },
                            accountListLoadingMore = false,
                        )
                    }
                }
        }
    }

    private fun publishOptimisticLists(accountId: Int) {
        val remote = (state.value.accountLists as? LoadState.Content)?.value.orEmpty().filter { it.id > 0 }
        val pending = accountOutbox.pending(accountId)
        mutableState.update { it.copy(accountLists = LoadState.Content(applyPendingLists(remote, pending))) }
    }

    private fun refreshTitleRating(title: TitleSummary) {
        titleRatingJob?.cancel()
        val account = accountApi
        val profile = (state.value.account as? AccountState.SignedIn)?.profile
        if (account == null || profile == null) {
            mutableState.update { it.copy(detailRating = AccountRatingState()) }
            return
        }
        titleRatingJob = scope.launch {
            val local = accountOutbox.pending(profile.id).lastOrNull { mutation ->
                val payload = mutation.payload as? AccountMutationPayload.TitleRating
                payload?.mediaType == title.mediaType && payload.mediaId == title.id
            }
            if (local != null) {
                val payload = local.payload as AccountMutationPayload.TitleRating
                mutableState.update {
                    it.copy(detailRating = AccountRatingState(payload.value, pending = true, error = local.lastError))
                }
                return@launch
            }
            runCatching { account.accountState(title.mediaType, title.id).ratingValue }
                .propagateCancellation()
                .onSuccess { value -> mutableState.update { it.copy(detailRating = AccountRatingState(value)) } }
                .onFailure { error -> mutableState.update {
                    it.copy(detailRating = it.detailRating.copy(pending = false, error = error.message))
                } }
        }
    }

    private fun refreshEpisodeRating(seriesId: Int, season: Int, episode: Int) {
        episodeRatingJob?.cancel()
        val account = accountApi
        val profile = (state.value.account as? AccountState.SignedIn)?.profile
        if (account == null || profile == null) {
            mutableState.update { it.copy(episodeRating = AccountRatingState()) }
            return
        }
        episodeRatingJob = scope.launch {
            val local = accountOutbox.pending(profile.id).lastOrNull { mutation ->
                val payload = mutation.payload as? AccountMutationPayload.EpisodeRating
                payload?.seriesId == seriesId && payload.seasonNumber == season && payload.episodeNumber == episode
            }
            if (local != null) {
                val payload = local.payload as AccountMutationPayload.EpisodeRating
                mutableState.update {
                    it.copy(episodeRating = AccountRatingState(payload.value, pending = true, error = local.lastError))
                }
                return@launch
            }
            runCatching { account.episodeAccountState(seriesId, season, episode).ratingValue }
                .propagateCancellation()
                .onSuccess { value -> mutableState.update { it.copy(episodeRating = AccountRatingState(value)) } }
                .onFailure { error -> mutableState.update {
                    it.copy(episodeRating = it.episodeRating.copy(pending = false, error = error.message))
                } }
        }
    }

    private suspend fun flushAccountOutbox(accountId: Int) {
        val account = accountApi ?: return
        if ((state.value.account as? AccountState.SignedIn)?.profile?.id != accountId) return
        val before = accountOutbox.pending(accountId)
        accountOutbox.flush(accountId) { mutation -> dispatchAccountMutation(account, mutation) }
        if (before.any { it.payload.isListMutation() }) refreshLists(accountId)
        state.value.detailSelection?.let(::refreshTitleRating)
        (state.value.entitySelection as? CatalogEntity.Episode)?.value?.let { episode ->
            refreshEpisodeRating(episode.seriesId, episode.seasonNumber, episode.episodeNumber)
        }
    }

    private suspend fun dispatchAccountMutation(
        account: AccountApi,
        mutation: PendingAccountMutation,
    ) = when (val payload = mutation.payload) {
        is AccountMutationPayload.TitleRating -> account.setRating(
            payload.mediaType,
            payload.mediaId,
            payload.value,
            mutation.id,
        )
        is AccountMutationPayload.EpisodeRating -> account.setEpisodeRating(
            payload.seriesId,
            payload.seasonNumber,
            payload.episodeNumber,
            payload.value,
            mutation.id,
        )
        is AccountMutationPayload.CreateList -> account.createList(
            payload.name,
            payload.description,
            payload.public,
            payload.region,
            payload.language,
            mutation.id,
        )
        is AccountMutationPayload.UpdateList -> account.updateList(
            payload.listId,
            payload.name,
            payload.description,
            payload.public,
            mutation.id,
        )
        is AccountMutationPayload.DeleteList -> account.deleteList(payload.listId, mutation.id)
        is AccountMutationPayload.MutateListItems -> account.mutateListItems(
            payload.listId,
            payload.items,
            payload.remove,
            mutation.id,
        )
    }

    private fun reloadGenresAndExplore() {
        scope.launch {
            val snapshot = state.value
            val genres = runCatching { api.genres(snapshot.exploreType, snapshot.locale.backendTag) }
                .propagateCancellation()
                .getOrDefault(emptyList())
            mutableState.update { current ->
                if (current.exploreType == snapshot.exploreType && current.locale == snapshot.locale) {
                    current.copy(genres = genres)
                } else current
            }
        }
        reloadExplore()
    }

    private fun reloadExploreConfiguration() {
        val v2 = apiV2 ?: return
        val snapshot = state.value
        if (!snapshot.advancedDiscoverEnabled()) {
            mutableState.update { it.copy(discoverConfiguration = null) }
            return
        }
        val region = effectiveRegion(snapshot)
        val language = snapshot.locale.backendTag
        mutableState.update { it.copy(discoverConfiguration = null) }
        scope.launch {
            runCatching { v2.discoverConfiguration(language, region) }
                .propagateCancellation()
                .onSuccess { configuration ->
                    val current = state.value
                    if (effectiveRegion(current) == region && current.locale.backendTag == language) {
                        mutableState.update { it.copy(discoverConfiguration = configuration) }
                    }
                }
        }
    }

    private fun syncExploreContext(reload: Boolean) {
        val snapshot = state.value
        val region = effectiveRegion(snapshot)
        val adult = includeAdult(snapshot)
        val regionChanged = snapshot.exploreFilter.region != region
        val filter = snapshot.exploreFilter.withContext(snapshot.exploreType, region, adult, regionChanged).let {
            if (snapshot.advancedDiscoverEnabled()) it else it.basic()
        }
        val draft = snapshot.exploreDraftFilter.withContext(snapshot.exploreType, region, adult, regionChanged).let {
            if (snapshot.advancedDiscoverEnabled()) it else it.basic()
        }
        if (filter != snapshot.exploreFilter || draft != snapshot.exploreDraftFilter || regionChanged) {
            mutableState.update {
                it.copy(
                    exploreFilter = filter,
                    exploreDraftFilter = draft,
                    discoverConfiguration = if (regionChanged) null else it.discoverConfiguration,
                )
            }
        }
        if (reload) reloadExplore()
    }

    private fun scheduleSearch(immediate: Boolean) {
        searchJob?.cancel()
        if (state.value.searchMode != CatalogSearchMode.CATALOG) return
        val query = state.value.searchQuery.trim()
        if (query.isEmpty()) {
            mutableState.update { it.copy(search = LoadState.Idle, entitySearch = LoadState.Idle, searchPage = 0) }
            return
        }
        searchJob = scope.launch {
            if (!immediate) delay(350)
            mutableState.update { it.copy(search = LoadState.Loading, entitySearch = LoadState.Loading, searchPage = 0) }
            val snapshot = state.value
            runCatching {
                apiV2?.searchEntities(
                    query,
                    snapshot.searchScopeV2,
                    1,
                    snapshot.locale.backendTag,
                    snapshot.regionOverride,
                    includeAdult(snapshot),
                )
            }
                .propagateCancellation()
                .onSuccess { entityPage ->
                    if (entityPage != null) mutableState.update { current ->
                        if (current.searchMode != CatalogSearchMode.CATALOG || current.searchQuery.trim() != query) {
                            return@update current
                        }
                        val entities = if (includeAdult(current)) entityPage.results else entityPage.results.withoutAdultTitles()
                        current.copy(
                            search = LoadState.Content(entities.mapNotNull { value -> (value as? CatalogEntity.Title)?.value }.distinctBy(TitleSummary::libraryKey)),
                            entitySearch = LoadState.Content(entities.distinctBy(CatalogEntity::stableKey)),
                            searchPage = entityPage.page,
                            searchTotalPages = entityPage.totalPages,
                        )
                    } else runCatching { api.search(query, snapshot.searchScope, 1, snapshot.locale.backendTag) }
                        .onSuccess { page -> mutableState.update { current ->
                            if (current.searchMode != CatalogSearchMode.CATALOG || current.searchQuery.trim() != query) {
                                return@update current
                            }
                            val results = if (includeAdult(current)) page.results else page.results.filterNot(TitleSummary::adult)
                            current.copy(
                                search = LoadState.Content(results.distinctBy(TitleSummary::libraryKey)),
                                entitySearch = LoadState.Content(results.map(CatalogEntity::Title)),
                                searchPage = page.page,
                                searchTotalPages = page.totalPages,
                            )
                        } }
                        .onFailure { failure -> mutableState.update { it.copy(search = LoadState.Error(failure.message.orEmpty()), entitySearch = LoadState.Error(failure.message.orEmpty())) } }
                }
                .onFailure { failure -> mutableState.update {
                    it.copy(search = LoadState.Error(failure.message.orEmpty()), entitySearch = LoadState.Error(failure.message.orEmpty()))
                } }
        }
    }

    private suspend fun refreshAccount() {
        if (!state.value.capabilities.supportsAccountAuthentication()) {
            mutableState.update { it.copy(account = AccountState.SignedOut) }
            return
        }
        val account = accountApi ?: run {
            mutableState.update { it.copy(account = AccountState.SignedOut) }
            return
        }
        runCatching { account.profile() }
            .propagateCancellation()
            .onSuccess { profile ->
                library.activateAccount(profile.id)
                mutableState.update { it.copy(account = AccountState.SignedIn(profile)) }
                refreshRecommendations(profile.id, reset = true)
                syncAccountLibrary(profile.id)
                flushAccountOutbox(profile.id)
                refreshLists(profile.id)
            }
            .onFailure { mutableState.update { it.copy(account = AccountState.SignedOut) } }
    }

    private suspend fun pollAndCompleteAuth(attempt: AuthAttempt) {
        val account = accountApi ?: return
        val interval = (attempt.pollingInterval ?: DEFAULT_AUTH_POLL_SECONDS).coerceAtLeast(2)
        repeat(MAX_AUTH_POLLS) {
            delay(interval * 1_000L)
            val status = runCatching { account.authAttempt(attempt.attemptId, attempt.deviceCode) }.getOrNull()
            when (status) {
                "approved" -> {
                    val session = runCatching { account.completeAuth(attempt.attemptId, attempt.deviceCode) }
                        .getOrElse { error ->
                            mutableState.update { it.copy(account = AccountState.Error(error.message.orEmpty())) }
                            return
                        }
                    library.activateAccount(session.profile.id)
                    mutableState.update { it.copy(account = AccountState.SignedIn(session.profile)) }
                    refreshRecommendations(session.profile.id, reset = true)
                    syncAccountLibrary(session.profile.id)
                    flushAccountOutbox(session.profile.id)
                    refreshLists(session.profile.id)
                    return
                }
                "expired", "denied" -> {
                    mutableState.update { it.copy(account = AccountState.Error("TMDb authorization $status.")) }
                    return
                }
            }
        }
        mutableState.update { it.copy(account = AccountState.Error("TMDb authorization timed out.")) }
    }

    private suspend fun syncAccountLibrary(accountId: Int) {
        val account = accountApi ?: return
        LibraryCollection.entries.forEach { collection ->
            MediaType.entries.forEach { mediaType ->
                var pageNumber = 1
                do {
                    val page = runCatching {
                        account.library(collection, mediaType, pageNumber, state.value.locale.backendTag)
                    }.getOrNull() ?: break
                    library.mergeRemote(page.results, collection, mediaType, accountId)
                    pageNumber += 1
                } while (pageNumber <= page.totalPages.coerceAtMost(MAX_LIBRARY_SYNC_PAGES))
            }
        }
        flushLibraryOutbox()
    }

    private suspend fun flushLibraryOutbox() {
        val account = accountApi ?: return
        if (state.value.account !is AccountState.SignedIn) return
        library.pendingMutations().forEach { mutation ->
            val collection = if (mutation.collection == LibraryCollection.FAVORITES.wireValue) {
                LibraryCollection.FAVORITES
            } else LibraryCollection.WATCHLIST
            val mediaType = if (mutation.mediaType == MediaType.MOVIE.wireValue) MediaType.MOVIE else MediaType.TV
            runCatching {
                account.setLibrary(collection, mediaType, mutation.mediaId, mutation.enabled, mutation.id)
            }.onSuccess { library.confirmMutation(mutation.id) }
                .onFailure { library.failMutation(mutation.id, it.message.orEmpty()) }
        }
    }

    private fun includeAdult(snapshot: SmartMovieState): Boolean = snapshot.adultConfigured &&
        snapshot.adultUnlocked && snapshot.adultLockUntil <= nowMillis()

    private fun effectiveRegion(snapshot: SmartMovieState): String = snapshot.regionOverride
        ?: deviceRegion.uppercase().takeIf { it.length == 2 }
        ?: "US"

    private fun normalizeRating(value: Double?): Double? = value?.let {
        ((it * 2).roundToInt() / 2.0).coerceIn(0.5, 10.0)
    }

    private fun resetAdultFailures() {
        store.putString(ADULT_FAILURES_KEY, "0")
        store.putString(ADULT_LOCK_UNTIL_KEY, "0")
        mutableState.update { it.copy(adultFailures = 0, adultLockUntil = 0) }
    }

    private companion object {
        const val INSTALLATION_ID_KEY = "smartmovie_installation_id"
        const val LOCALE_KEY = "smartmovie_locale"
        const val REGION_KEY = "smartmovie_region"
        const val ADULT_SALT_KEY = "smartmovie_adult_salt"
        const val ADULT_DIGEST_KEY = "smartmovie_adult_digest"
        const val ADULT_FAILURES_KEY = "smartmovie_adult_failures"
        const val ADULT_LOCK_UNTIL_KEY = "smartmovie_adult_lock_until"
        const val MAX_ADULT_FAILURES = 5
        const val ADULT_LOCK_MILLIS = 5 * 60 * 1_000L
        const val DEFAULT_AUTH_POLL_SECONDS = 5
        const val MAX_AUTH_POLLS = 120
        const val MAX_LIBRARY_SYNC_PAGES = 50
    }
}

internal fun isValidAdultConfiguration(pin: String, confirmation: String, ageConfirmed: Boolean): Boolean =
    ageConfirmed && pin == confirmation && pin.matches(Regex("[0-9]{6}"))

private fun List<CatalogEntity>.withoutAdultTitles(): List<CatalogEntity> =
    visibleCatalogEntities(this, includeAdult = false)

private fun EntityDetail.withAdultVisibility(includeAdult: Boolean): EntityDetail = when (this) {
    is EntityDetail.Person -> EntityDetail.Person(
        value.copy(
            knownFor = visibleCatalogTitles(value.knownFor, includeAdult),
            credits = value.credits.copy(
                cast = visibleCatalogCredits(value.credits.cast, includeAdult),
                crew = visibleCatalogCredits(value.credits.crew, includeAdult),
            ),
        ),
    )
    is EntityDetail.Collection -> EntityDetail.Collection(
        value.copy(parts = visibleCatalogTitles(value.parts, includeAdult)),
    )
    is EntityDetail.Organization -> EntityDetail.Organization(
        value.copy(titles = value.titles.copy(results = visibleCatalogTitles(value.titles.results, includeAdult))),
    )
    is EntityDetail.Keyword -> EntityDetail.Keyword(
        value.copy(titles = value.titles.copy(results = visibleCatalogTitles(value.titles.results, includeAdult))),
    )
    is EntityDetail.Credit -> EntityDetail.Credit(
        value.copy(titleSummary = value.titleSummary?.takeIf { includeAdult || !it.adult }),
    )
    is EntityDetail.Season,
    is EntityDetail.Episode,
    -> this
}

private fun DiscoverFilter.normalized(): DiscoverFilter {
    val minimum = minimumRuntime?.coerceAtLeast(0)
    val maximum = maximumRuntime?.coerceAtLeast(0)
    return copy(
        releaseDateFrom = releaseDateFrom.clean(),
        releaseDateThrough = releaseDateThrough.clean(),
        originalLanguage = originalLanguage.clean()?.lowercase(),
        originCountry = originCountry.clean()?.uppercase(),
        certificationCountry = certificationCountry.clean()?.uppercase(),
        certificationMinimum = certificationMinimum.clean(),
        certificationMaximum = certificationMaximum.clean(),
        minimumRuntime = if (minimum != null && maximum != null) minOf(minimum, maximum) else minimum,
        maximumRuntime = if (minimum != null && maximum != null) maxOf(minimum, maximum) else maximum,
        minimumVoteCount = minimumVoteCount.coerceAtLeast(0),
        region = region.clean()?.uppercase(),
    )
}

private fun DiscoverFilter.withContext(
    mediaType: MediaType,
    region: String,
    includeAdult: Boolean,
    regionChanged: Boolean,
): DiscoverFilter = copy(
    region = region,
    certificationCountry = when {
        mediaType != MediaType.MOVIE -> null
        regionChanged -> region
        else -> certificationCountry
    },
    certificationMinimum = if (regionChanged) null else certificationMinimum,
    certificationMaximum = if (regionChanged) null else certificationMaximum,
    watchProviderIds = if (regionChanged) emptySet() else watchProviderIds,
    includeAdult = includeAdult,
)

private fun SmartMovieState.matchesExplore(snapshot: SmartMovieState): Boolean =
    exploreType == snapshot.exploreType &&
        exploreFilter == snapshot.exploreFilter &&
        locale == snapshot.locale &&
        advancedDiscoverEnabled() == snapshot.advancedDiscoverEnabled()

private fun SmartMovieState.advancedDiscoverEnabled(): Boolean =
    capabilities?.supportsCatalog("advanced_discover") == true

private fun DiscoverFilter.basic(): DiscoverFilter = DiscoverFilter(
    genres = genres,
    year = year,
    minimumRating = minimumRating,
    sort = sort,
    region = region,
    includeAdult = includeAdult,
)

private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

internal suspend fun loadAllAccountLists(
    maxPages: Int = 500,
    loadPage: suspend (Int) -> PagedResult<UserList>,
): List<UserList> {
    var page = 1
    var totalPages = 1
    val lists = mutableListOf<UserList>()
    do {
        val response = loadPage(page)
        lists += response.results
        totalPages = response.totalPages.coerceIn(1, maxPages)
        page += 1
    } while (page <= totalPages)
    return lists.distinctBy(UserList::id)
}

internal fun mergeAccountRecommendations(
    existing: List<TitleSummary>,
    page: PagedResult<TitleSummary>,
    includeAdult: Boolean,
): List<TitleSummary> = (existing + page.results)
    .filter { includeAdult || !it.adult }
    .distinctBy(TitleSummary::libraryKey)

internal fun mergeAccountListPage(existing: UserList?, page: UserList, includeAdult: Boolean): UserList {
    val pageNumber = page.page ?: 1
    val results = ((if (pageNumber > 1) existing?.results.orEmpty() else emptyList()) + page.results)
        .filter { includeAdult || !it.adult }
        .distinctBy(TitleSummary::libraryKey)
    return page.copy(page = pageNumber, totalPages = page.totalPages ?: pageNumber, results = results)
}

internal fun filterAccountListSearchResults(
    candidates: List<TitleSummary>,
    existing: List<TitleSummary>,
    includeAdult: Boolean,
): List<TitleSummary> {
    val existingKeys = existing.mapTo(hashSetOf(), TitleSummary::libraryKey)
    return candidates.filter { (includeAdult || !it.adult) && it.libraryKey !in existingKeys }
        .distinctBy(TitleSummary::libraryKey)
}

private fun AccountMutationPayload.isListMutation(): Boolean = when (this) {
    is AccountMutationPayload.CreateList,
    is AccountMutationPayload.DeleteList,
    is AccountMutationPayload.MutateListItems,
    is AccountMutationPayload.UpdateList,
    -> true
    is AccountMutationPayload.EpisodeRating,
    is AccountMutationPayload.TitleRating,
    -> false
}

private fun TitleDetailV2.toLegacy(): TitleDetail = TitleDetail(
    id = id,
    mediaType = mediaType,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genres = genres,
    runtimeMinutes = runtimeMinutes,
    numberOfSeasons = numberOfSeasons,
    status = status,
    cast = cast.mapNotNull { credit ->
        credit.id?.let { personId -> CastMember(personId, credit.title.orEmpty(), credit.character, credit.profilePath) }
    },
    videos = videos,
    similar = similar,
)

private fun <T> Result<T>.propagateCancellation(): Result<T> = also { result ->
    if (result.exceptionOrNull() is CancellationException) throw result.exceptionOrNull()!!
}
