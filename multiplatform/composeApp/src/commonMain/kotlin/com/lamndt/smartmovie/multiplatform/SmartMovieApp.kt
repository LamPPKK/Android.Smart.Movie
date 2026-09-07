package com.lamndt.smartmovie.multiplatform

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lamndt.smartmovie.multiplatform.data.LibraryCollection
import com.lamndt.smartmovie.multiplatform.data.LibraryRecord
import com.lamndt.smartmovie.multiplatform.model.AppLocale
import com.lamndt.smartmovie.multiplatform.model.CatalogEntity
import com.lamndt.smartmovie.multiplatform.model.CatalogSearchMode
import com.lamndt.smartmovie.multiplatform.model.Credit
import com.lamndt.smartmovie.multiplatform.model.DiscoverSort
import com.lamndt.smartmovie.multiplatform.model.EntityKind
import com.lamndt.smartmovie.multiplatform.model.ExternalIdSource
import com.lamndt.smartmovie.multiplatform.model.HomeFeed
import com.lamndt.smartmovie.multiplatform.model.ImageAsset
import com.lamndt.smartmovie.multiplatform.model.ImageUrlFactory
import com.lamndt.smartmovie.multiplatform.platform.systemTimeMillis
import kotlinx.coroutines.delay
import com.lamndt.smartmovie.multiplatform.model.MediaType
import com.lamndt.smartmovie.multiplatform.model.PersonSummary
import com.lamndt.smartmovie.multiplatform.model.Review
import com.lamndt.smartmovie.multiplatform.model.SearchScope
import com.lamndt.smartmovie.multiplatform.model.SearchScopeV2
import com.lamndt.smartmovie.multiplatform.model.TitleDetail
import com.lamndt.smartmovie.multiplatform.model.TitleDetailV2
import com.lamndt.smartmovie.multiplatform.model.TitleSummary
import com.lamndt.smartmovie.multiplatform.model.UiStrings
import com.lamndt.smartmovie.multiplatform.model.Video
import com.lamndt.smartmovie.multiplatform.model.WatchMonetizationType
import com.lamndt.smartmovie.multiplatform.model.playableVideos
import com.lamndt.smartmovie.multiplatform.model.presentedEditorialTitles
import com.lamndt.smartmovie.multiplatform.model.presentedExternalIds
import com.lamndt.smartmovie.multiplatform.model.presentedImages
import com.lamndt.smartmovie.multiplatform.model.presentedReviews
import com.lamndt.smartmovie.multiplatform.model.supportsAccountAuthentication
import com.lamndt.smartmovie.multiplatform.model.preferredTrailer
import com.lamndt.smartmovie.multiplatform.model.strings
import com.lamndt.smartmovie.multiplatform.platform.openExternalUrl
import com.lamndt.smartmovie.multiplatform.platform.platformName
import com.lamndt.smartmovie.multiplatform.ui.CinemaBackground
import com.lamndt.smartmovie.multiplatform.ui.CinemaCardShape
import com.lamndt.smartmovie.multiplatform.ui.CinemaColors
import com.lamndt.smartmovie.multiplatform.ui.LoadingPane
import com.lamndt.smartmovie.multiplatform.ui.MessagePane
import com.lamndt.smartmovie.multiplatform.ui.PosterCard
import com.lamndt.smartmovie.multiplatform.ui.RatingBadge
import com.lamndt.smartmovie.multiplatform.ui.RemoteArtwork
import com.lamndt.smartmovie.multiplatform.ui.SectionTitle
import com.lamndt.smartmovie.multiplatform.ui.SmartMovieTheme
import kotlin.math.roundToInt

@Composable
fun SmartMovieApp(controller: AppController = remember { AppController() }) {
    val state by controller.state.collectAsState()
    val copy = strings(state.locale)

    DisposableEffect(controller) { onDispose(controller::close) }

    SmartMovieTheme {
        CinemaBackground {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val expanded = maxWidth >= 860.dp
                val splitDetail = maxWidth >= 1220.dp
                if (expanded) {
                    Row(Modifier.fillMaxSize()) {
                        DesktopNavigation(state.selectedTab, copy, controller::selectTab)
                        Box(Modifier.weight(1f).fillMaxHeight()) {
                            AppContent(state, copy, controller, Modifier.fillMaxSize())
                            if (state.detailSelection != null) {
                                DetailPane(
                                    state = state,
                                    copy = copy,
                                    controller = controller,
                                    modifier = if (splitDetail) {
                                        Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(570.dp)
                                    } else Modifier.fillMaxSize(),
                                )
                            }
                            if (state.entitySelection != null || state.creditSelection != null) {
                                EntityDetailPane(
                                    state = state,
                                    copy = copy,
                                    controller = controller,
                                    modifier = if (splitDetail) Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(570.dp)
                                    else Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            AppContent(state, copy, controller, Modifier.fillMaxSize())
                            if (state.detailSelection != null) {
                                DetailPane(state, copy, controller, Modifier.fillMaxSize())
                            }
                            if (state.entitySelection != null || state.creditSelection != null) {
                                EntityDetailPane(state, copy, controller, Modifier.fillMaxSize())
                            }
                        }
                        CompactNavigation(state.selectedTab, copy, controller::selectTab)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppContent(
    state: SmartMovieState,
    copy: UiStrings,
    controller: AppController,
    modifier: Modifier,
) {
    Column(modifier) {
        AppHeader(state.locale, copy, controller::changeLocale)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (state.selectedTab) {
                AppTab.HOME -> HomeScreen(state, copy, controller)
                AppTab.EXPLORE -> ExploreScreen(state, copy, controller)
                AppTab.SEARCH -> SearchScreen(state, copy, controller)
                AppTab.LIBRARY -> LibraryScreen(state, copy, controller)
                AppTab.PROFILE -> ProfileScreen(state, controller)
            }
        }
    }
}

@Composable
private fun AppHeader(locale: AppLocale, copy: UiStrings, onLocale: (AppLocale) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(WindowInsets.safeDrawing.asPaddingValues()).padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = CinemaColors.Accent, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.padding(9.dp))
            }
            Column {
                Text(
                    "SMARTMOVIE",
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaColors.Foreground,
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                )
                Text("${copy.platformEdition} · ${platformName()}", style = MaterialTheme.typography.labelMedium, color = CinemaColors.Muted)
            }
        }
        LocalePicker(locale, copy, onLocale)
    }
}

@Composable
private fun LocalePicker(locale: AppLocale, copy: UiStrings, onLocale: (AppLocale) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(locale.nativeName) },
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, Modifier.size(18.dp)) },
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLocale.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.nativeName) },
                    onClick = { expanded = false; onLocale(option) },
                    leadingIcon = if (option == locale) ({ Icon(Icons.Default.Star, contentDescription = copy.language) }) else null,
                )
            }
        }
    }
}

@Composable
private fun DesktopNavigation(selected: AppTab, copy: UiStrings, onSelect: (AppTab) -> Unit) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight().width(92.dp),
        containerColor = CinemaColors.Elevated.copy(alpha = 0.92f),
        header = { Spacer(Modifier.height(78.dp)) },
    ) {
        AppTab.entries.forEach { tab ->
            val selectedNow = tab == selected
            NavigationRailItem(
                selected = selectedNow,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon(selectedNow), contentDescription = tab.label(copy)) },
                label = { Text(tab.label(copy), maxLines = 1) },
                alwaysShowLabel = true,
            )
        }
    }
}

@Composable
private fun CompactNavigation(selected: AppTab, copy: UiStrings, onSelect: (AppTab) -> Unit) {
    NavigationBar(
        containerColor = CinemaColors.Elevated,
        modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
    ) {
        AppTab.entries.forEach { tab ->
            val selectedNow = tab == selected
            NavigationBarItem(
                selected = selectedNow,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon(selectedNow), contentDescription = tab.label(copy)) },
                label = { Text(tab.label(copy)) },
            )
        }
    }
}

private fun AppTab.label(copy: UiStrings): String = when (this) {
    AppTab.HOME -> copy.home
    AppTab.EXPLORE -> copy.explore
    AppTab.SEARCH -> copy.search
    AppTab.LIBRARY -> copy.library
    AppTab.PROFILE -> copy.profile
}

private fun AppTab.icon(selected: Boolean): ImageVector = when (this) {
    AppTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    AppTab.EXPLORE -> if (selected) Icons.Filled.Explore else Icons.Outlined.Explore
    AppTab.SEARCH -> if (selected) Icons.Filled.Search else Icons.Outlined.Search
    AppTab.LIBRARY -> if (selected) Icons.AutoMirrored.Filled.LibraryBooks else Icons.AutoMirrored.Outlined.LibraryBooks
    AppTab.PROFILE -> Icons.Default.AccountCircle
}

@Composable
private fun ProfileScreen(state: SmartMovieState, controller: AppController) {
    val copy = profileCopy(state.locale)
    val uiCopy = strings(state.locale)
    val images = remember(state.imageConfiguration) { ImageUrlFactory(state.imageConfiguration) }
    var region by remember(state.regionOverride) { mutableStateOf(state.regionOverride.orEmpty()) }
    var pin by remember { mutableStateOf("") }
    var pinConfirmation by remember { mutableStateOf("") }
    var adultAgeConfirmed by remember { mutableStateOf(false) }
    var adultMessage by remember { mutableStateOf<String?>(null) }
    var adultNow by remember(state.adultLockUntil) { mutableStateOf(systemTimeMillis()) }
    var listName by remember { mutableStateOf("") }
    var listDescription by remember { mutableStateOf("") }
    LaunchedEffect(state.adultLockUntil) {
        adultNow = systemTimeMillis()
        val remaining = state.adultLockUntil - adultNow
        if (remaining > 0) {
            delay(remaining)
            adultNow = systemTimeMillis()
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item { SectionTitle(copy.profile) }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = CinemaColors.Elevated) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(copy.tmdbAccount, style = MaterialTheme.typography.titleLarge)
                    when (val account = state.account) {
                        AccountState.Checking -> Text(copy.checking, color = CinemaColors.Muted)
                        AccountState.SignedOut -> {
                            val accountAuthenticationAvailable = state.capabilities.supportsAccountAuthentication()
                            Text(
                                if (accountAuthenticationAvailable) copy.signInDescription else copy.accountUnavailable,
                                color = CinemaColors.Muted,
                            )
                            Button(
                                onClick = { controller.beginSignIn() },
                                enabled = accountAuthenticationAvailable,
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Accent),
                            ) {
                                Text(copy.signIn)
                            }
                        }
                        is AccountState.Authorizing -> {
                            Text(copy.finishBrowser, color = CinemaColors.Muted)
                            account.attempt.deviceCode?.let { Text("${copy.deviceCode}: $it", style = MaterialTheme.typography.titleMedium) }
                            Button(onClick = controller::cancelSignIn) { Text(copy.cancel) }
                        }
                        is AccountState.SignedIn -> {
                            Text(account.profile.name.ifBlank { account.profile.username }, style = MaterialTheme.typography.titleMedium)
                            Text("@${account.profile.username}", color = CinemaColors.Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { controller.signOut(keepAsLocal = true) }) { Text(copy.signOutKeep) }
                                Button(onClick = { controller.signOut(keepAsLocal = false) }) { Text(copy.signOutRemove) }
                            }
                        }
                        is AccountState.Error -> {
                            Text(account.message, color = CinemaColors.Accent)
                            Button(
                                onClick = { controller.beginSignIn() },
                                enabled = state.capabilities.supportsAccountAuthentication(),
                            ) { Text(copy.retry) }
                        }
                    }
                }
            }
        }
        if (state.account is AccountState.SignedIn) item {
            Surface(shape = RoundedCornerShape(22.dp), color = CinemaColors.Elevated) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(copy.recommendations, style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MediaType.entries.forEach { mediaType ->
                            FilterChip(
                                selected = state.accountRecommendationType == mediaType,
                                onClick = { controller.selectRecommendationType(mediaType) },
                                label = { Text(typeLabel(mediaType, uiCopy)) },
                            )
                        }
                    }
                    when (val recommendations = state.accountRecommendations) {
                        LoadState.Idle, LoadState.Loading -> CircularProgressIndicator()
                        is LoadState.Error -> MessagePane(
                            uiCopy.serviceError,
                            recommendations.message,
                            uiCopy.retry,
                            controller::refreshRecommendations,
                        )
                        is LoadState.Content -> if (recommendations.value.isEmpty()) {
                            Text(copy.noRecommendations, color = CinemaColors.Muted)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(recommendations.value, key = TitleSummary::libraryKey) { title ->
                                    PosterCard(title, images, typeLabel(title.mediaType, uiCopy), { controller.openDetail(title) })
                                }
                            }
                        }
                    }
                    if (state.accountRecommendationPage in 1 until state.accountRecommendationTotalPages) {
                        Button(
                            onClick = controller::loadMoreRecommendations,
                            enabled = !state.accountRecommendationsLoadingMore,
                        ) {
                            if (state.accountRecommendationsLoadingMore) CircularProgressIndicator(Modifier.size(18.dp))
                            else Text(uiCopy.loadMore)
                        }
                    }
                    state.accountRecommendationError?.let { Text(it, color = CinemaColors.Accent) }
                }
            }
        }
        if (state.account is AccountState.SignedIn) item {
            Surface(shape = RoundedCornerShape(22.dp), color = CinemaColors.Elevated) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(copy.customLists, style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(listName, { listName = it.take(100) }, label = { Text(copy.listName) }, singleLine = true)
                    OutlinedTextField(listDescription, { listDescription = it.take(1000) }, label = { Text(copy.listDescription) })
                    Button(onClick = {
                        controller.createList(listName, listDescription)
                        listName = ""; listDescription = ""
                    }, enabled = listName.isNotBlank()) { Text(copy.createList) }
                    when (val lists = state.accountLists) {
                        LoadState.Idle, LoadState.Loading -> CircularProgressIndicator()
                        is LoadState.Error -> Text(lists.message, color = CinemaColors.Accent)
                        is LoadState.Content -> lists.value.forEach { list ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(list.name, style = MaterialTheme.typography.titleMedium)
                                    Text(list.description, color = CinemaColors.Muted, maxLines = 2)
                                }
                                Row {
                                    TextButton(onClick = { controller.openAccountList(list.id) }) { Text(copy.openList) }
                                    TextButton(onClick = { controller.deleteList(list.id) }) { Text(copy.deleteList) }
                                }
                            }
                        }
                    }
                    AccountListDetailPanel(state, controller, copy, uiCopy)
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = CinemaColors.Elevated) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(copy.region, style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = region,
                        onValueChange = { region = it.take(2).uppercase() },
                        label = { Text(copy.regionHint) },
                        singleLine = true,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = state.regionOverride == null,
                                onClick = {
                                    region = ""
                                    controller.setRegion(null)
                                },
                                label = { Text(copy.deviceRegion) },
                            )
                        }
                        val options = state.discoverConfiguration?.watchProviderRegions.orEmpty().ifEmpty {
                            fallbackProviderRegionCodes.map { code ->
                                com.lamndt.smartmovie.multiplatform.model.ConfigurationCountry(code, code)
                            }
                        }
                        items(options, key = { it.code }) { option ->
                            FilterChip(
                                selected = state.regionOverride == option.code,
                                onClick = {
                                    region = option.code
                                    controller.setRegion(option.code)
                                },
                                label = { Text(option.displayName) },
                            )
                        }
                    }
                    Button(onClick = { controller.setRegion(region.ifBlank { null }) }) { Text(copy.save) }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = CinemaColors.Elevated) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(copy.adultContent, style = MaterialTheme.typography.titleLarge)
                    Text(copy.adultDescription, color = CinemaColors.Muted)
                    if (state.adultLockUntil > adultNow) {
                        Text(copy.locked, color = CinemaColors.Accent)
                    } else {
                        if (!state.adultConfigured) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Switch(
                                    checked = adultAgeConfirmed,
                                    onCheckedChange = { adultAgeConfirmed = it; adultMessage = null },
                                )
                                Text(copy.adultAgeConfirmation, modifier = Modifier.weight(1f))
                            }
                        }
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { value -> pin = value.filter(Char::isDigit).take(6) },
                            label = { Text(copy.pin) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                        )
                        if (!state.adultConfigured) {
                            OutlinedTextField(
                                value = pinConfirmation,
                                onValueChange = { value -> pinConfirmation = value.filter(Char::isDigit).take(6) },
                                label = { Text(copy.confirmPin) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                            )
                        }
                        Button(
                            onClick = {
                                val succeeded = if (state.adultConfigured) {
                                    controller.unlockAdult(pin)
                                } else {
                                    controller.configureAdultPin(pin, pinConfirmation, adultAgeConfirmed)
                                }
                                adultMessage = when {
                                    succeeded -> null
                                    !state.adultConfigured && !adultAgeConfirmed -> copy.adultAgeRequired
                                    else -> copy.pinInvalid
                                }
                                if (succeeded) adultAgeConfirmed = false
                                pin = ""
                                pinConfirmation = ""
                            },
                        ) { Text(if (state.adultConfigured) copy.unlock else copy.enable) }
                        if (state.adultUnlocked) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = controller::lockAdult) { Text(copy.lock) }
                                Button(onClick = {
                                    controller.disableAdult()
                                    adultAgeConfirmed = false
                                    pin = ""
                                    pinConfirmation = ""
                                    adultMessage = null
                                }) { Text(copy.disable) }
                            }
                        }
                        adultMessage?.let { Text(it, color = CinemaColors.Accent) }
                    }
                }
            }
        }
        item { Text(copy.attribution, color = CinemaColors.Muted, style = MaterialTheme.typography.bodySmall) }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = CinemaColors.Elevated) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(copy.sourceRepositories, style = MaterialTheme.typography.titleLarge)
                    Text(titleMetadataCopy(state.locale).aboutDescription, color = CinemaColors.Muted)
                    TextButton(onClick = { openExternalUrl("https://github.com/LamPPKK/Smart-Movie-iOS") }) {
                        Text("Smart Movie iOS")
                    }
                    TextButton(onClick = { openExternalUrl("https://github.com/LamPPKK/Smart-Movie-Android") }) {
                        Text("Smart Movie Android")
                    }
                    TextButton(onClick = {
                        openExternalUrl("https://github.com/LamPPKK/Smart-Movie-iOS/blob/main/docs/DEVELOPMENT_PLAN.md")
                    }) {
                        Text(copy.developmentPlan)
                    }
                }
            }
        }
    }
}

private val fallbackProviderRegionCodes = listOf(
    "US", "GB", "CA", "AU", "FR", "DE", "JP", "KR", "VN", "TW", "HK", "SG", "IN", "BR", "MX",
)

@Composable
private fun AccountListDetailPanel(
    state: SmartMovieState,
    controller: AppController,
    copy: ProfileCopy,
    uiCopy: UiStrings,
) {
    val listId = state.selectedAccountListId ?: return
    when (val detail = state.accountListDetail) {
        LoadState.Idle -> Unit
        LoadState.Loading -> CircularProgressIndicator()
        is LoadState.Error -> MessagePane(uiCopy.serviceError, detail.message, uiCopy.retry, controller::refreshAccountList)
        is LoadState.Content -> {
            val list = detail.value
            var name by remember(list.id, list.name) { mutableStateOf(list.name) }
            var description by remember(list.id, list.description) { mutableStateOf(list.description) }
            var public by remember(list.id, list.public) { mutableStateOf(list.public) }
            var query by remember(list.id) { mutableStateOf("") }
            Text(copy.listDetails, style = MaterialTheme.typography.titleLarge)
            if (listId < 0) Text(copy.waitingListSync, color = CinemaColors.Muted)
            OutlinedTextField(name, { name = it.take(100) }, label = { Text(copy.listName) }, singleLine = true)
            OutlinedTextField(description, { description = it.take(1000) }, label = { Text(copy.listDescription) })
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Switch(public, { public = it }, enabled = listId > 0)
                Text(copy.publicList)
            }
            Button(
                onClick = { controller.updateList(listId, name, description, public) },
                enabled = listId > 0 && name.isNotBlank(),
            ) { Text(copy.saveChanges) }

            Text(copy.titlesInList, style = MaterialTheme.typography.titleMedium)
            if (list.results.isEmpty()) {
                Text(copy.emptyListTitles, color = CinemaColors.Muted)
            } else {
                list.results.forEach { title ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { controller.openDetail(title) }, modifier = Modifier.weight(1f)) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(title.title)
                                Text(typeLabel(title.mediaType, uiCopy), color = CinemaColors.Muted)
                            }
                        }
                        TextButton(
                            onClick = { controller.removeAccountListTitle(title) },
                            enabled = listId > 0,
                        ) { Text(copy.removeTitle) }
                    }
                }
            }
            if ((list.page ?: 0) in 1 until (list.totalPages ?: 1)) {
                Button(onClick = controller::loadMoreAccountList, enabled = !state.accountListLoadingMore) {
                    if (state.accountListLoadingMore) CircularProgressIndicator(Modifier.size(18.dp))
                    else Text(uiCopy.loadMore)
                }
            }

            Text(copy.addTitles, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                query,
                {
                    query = it
                    controller.changeAccountListSearchQuery(it)
                },
                label = { Text(copy.searchTitles) },
                singleLine = true,
            )
            Button(
                onClick = { controller.searchAccountList(query) },
                enabled = listId > 0 && query.isNotBlank() && state.accountListSearch !is LoadState.Loading,
            ) { Text(uiCopy.search) }
            when (val search = state.accountListSearch) {
                LoadState.Idle -> Unit
                LoadState.Loading -> CircularProgressIndicator()
                is LoadState.Error -> Text(search.message, color = CinemaColors.Accent)
                is LoadState.Content -> search.value.forEach { title ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(title.title)
                            Text(typeLabel(title.mediaType, uiCopy), color = CinemaColors.Muted)
                        }
                        TextButton(onClick = { controller.addAccountListTitle(title) }) { Text(copy.addTitle) }
                    }
                }
            }
            TextButton(onClick = controller::closeAccountList) { Text(copy.closeList) }
        }
    }
}

@Composable
private fun HomeScreen(state: SmartMovieState, copy: UiStrings, controller: AppController) {
    when (val home = state.home) {
        LoadState.Idle, LoadState.Loading -> LoadingPane()
        is LoadState.Error -> MessagePane(copy.serviceError, home.message, copy.retry, controller::reloadHome)
        is LoadState.Content -> HomeContent(home.value, state, copy, controller)
    }
}

@Composable
private fun HomeContent(feed: HomeFeed, state: SmartMovieState, copy: UiStrings, controller: AppController) {
    val images = remember(state.imageConfiguration) { ImageUrlFactory(state.imageConfiguration) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 28.dp, end = 28.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            MediaTypeChips(state.homeType, copy, controller::changeHomeType)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                FilterChip(selected = state.trendingWindow == "day", onClick = { controller.changeTrendingWindow("day") }, label = { Text("Today") })
                FilterChip(selected = state.trendingWindow == "week", onClick = { controller.changeTrendingWindow("week") }, label = { Text("This week") })
            }
        }
        feed.hero?.let { hero ->
            item { HeroCard(hero, images, copy) { controller.openDetail(hero) } }
        }
        feed.sections.forEach { section ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    SectionTitle(section.title)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(section.items.distinctBy(TitleSummary::libraryKey), key = { it.libraryKey }) { title ->
                            PosterCard(title, images, typeLabel(title.mediaType, copy), { controller.openDetail(title) })
                        }
                    }
                }
            }
        }
        if (state.trending.isNotEmpty()) item {
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                SectionTitle(if (state.trendingWindow == "day") "Trending today" else "Trending this week")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.trending.distinctBy(TitleSummary::libraryKey), key = { it.libraryKey }) { title ->
                        PosterCard(title, images, typeLabel(title.mediaType, copy), { controller.openDetail(title) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    hero: TitleSummary,
    images: ImageUrlFactory,
    copy: UiStrings,
    onOpen: () -> Unit,
) {
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(430.dp).clip(RoundedCornerShape(28.dp)).background(CinemaColors.Elevated),
    ) {
        val wideHero = maxWidth > 700.dp
        RemoteArtwork(images.backdrop(hero.backdropPath), hero.displayTitle, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = 0.92f), Color.Black.copy(alpha = 0.48f), Color.Transparent),
                ),
            ),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, CinemaColors.Background.copy(alpha = 0.52f))),
            ),
        )
        Column(
            Modifier.align(Alignment.BottomStart).padding(30.dp).widthIn(max = if (wideHero) 620.dp else 470.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                RatingBadge(hero.voteAverage)
                Text(typeLabel(hero.mediaType, copy).uppercase(), style = MaterialTheme.typography.labelLarge, color = CinemaColors.Muted)
                hero.releaseYear?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = CinemaColors.Muted) }
            }
            Text(
                hero.displayTitle,
                style = if (wideHero) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displayMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            Text(hero.overview, style = MaterialTheme.typography.bodyLarge, color = CinemaColors.Foreground.copy(alpha = 0.82f), maxLines = 3)
            Button(onClick = onOpen, colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Accent), modifier = Modifier.height(50.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text(copy.details, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun MediaTypeChips(selected: MediaType, copy: UiStrings, onSelect: (MediaType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MediaType.entries.forEach { mediaType ->
            FilterChip(
                selected = mediaType == selected,
                onClick = { onSelect(mediaType) },
                label = { Text(typeLabel(mediaType, copy)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CinemaColors.Accent),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ExploreScreen(state: SmartMovieState, copy: UiStrings, controller: AppController) {
    val images = remember(state.imageConfiguration) { ImageUrlFactory(state.imageConfiguration) }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle(copy.discover)
                MediaTypeChips(state.exploreType, copy, controller::changeExploreType)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    item {
                        FilterChip(
                            selected = state.exploreFilter.minimumRating == 0.0,
                            onClick = { controller.setMinimumRating(0.0) },
                            label = { Text("${copy.rating}: ${copy.all}") },
                        )
                    }
                    listOf(6.0, 7.0, 8.0, 9.0).forEach { rating ->
                        item {
                            FilterChip(
                                selected = state.exploreFilter.minimumRating == rating,
                                onClick = { controller.setMinimumRating(rating) },
                                label = { Text("${copy.rating} ${rating.toInt()}+") },
                            )
                        }
                    }
                    DiscoverSort.entries.forEach { sort ->
                        item {
                            FilterChip(
                                selected = state.exploreFilter.sort == sort,
                                onClick = { controller.setExploreSort(sort) },
                                label = { Text(sortLabel(sort, copy)) },
                            )
                        }
                    }
                    item { AssistChip(onClick = controller::resetExplore, label = { Text(copy.reset) }) }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    item {
                        FilterChip(
                            selected = state.exploreFilter.year == null,
                            onClick = { controller.setExploreYear(null) },
                            label = { Text("${copy.year}: ${copy.all}") },
                        )
                    }
                    items((CURRENT_YEAR downTo 1950).toList()) { year ->
                        FilterChip(
                            selected = state.exploreFilter.year == year,
                            onClick = { controller.setExploreYear(year) },
                            label = { Text(year.toString()) },
                        )
                    }
                }
                if (state.genres.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.genres.forEach { genre ->
                            FilterChip(
                                selected = genre.id in state.exploreFilter.genres,
                                onClick = { controller.toggleGenre(genre.id) },
                                label = { Text(genre.name) },
                            )
                        }
                    }
                }
                if (state.capabilities?.supportsCatalog("advanced_discover") == true) {
                    Text(copy.advancedDiscover.filters, style = MaterialTheme.typography.titleMedium)
                    Text(copy.advancedDiscover.releaseRange, color = CinemaColors.Muted)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DiscoverTextField(
                        state.exploreDraftFilter.releaseDateFrom.orEmpty(),
                        copy.advancedDiscover.dateFrom,
                        { value -> controller.updateExploreFilter { it.copy(releaseDateFrom = value) } },
                        Modifier.weight(1f),
                    )
                    DiscoverTextField(
                        state.exploreDraftFilter.releaseDateThrough.orEmpty(),
                        copy.advancedDiscover.dateThrough,
                        { value -> controller.updateExploreFilter { it.copy(releaseDateThrough = value) } },
                        Modifier.weight(1f),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DiscoverTextField(
                        state.exploreDraftFilter.originalLanguage.orEmpty(),
                        copy.advancedDiscover.originalLanguage,
                        { value -> controller.updateExploreFilter { it.copy(originalLanguage = value) } },
                        Modifier.weight(1f),
                    )
                    DiscoverTextField(
                        state.exploreDraftFilter.originCountry.orEmpty(),
                        copy.advancedDiscover.originCountry,
                        { value -> controller.updateExploreFilter { it.copy(originCountry = value) } },
                        Modifier.weight(1f),
                    )
                }
                if (state.exploreType == MediaType.MOVIE) {
                    Text(copy.advancedDiscover.certification, color = CinemaColors.Muted)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DiscoverTextField(
                            state.exploreDraftFilter.certificationMinimum.orEmpty(),
                            copy.advancedDiscover.minimum,
                            { value -> controller.updateExploreFilter { it.copy(certificationMinimum = value) } },
                            Modifier.weight(1f),
                        )
                        DiscoverTextField(
                            state.exploreDraftFilter.certificationMaximum.orEmpty(),
                            copy.advancedDiscover.maximum,
                            { value -> controller.updateExploreFilter { it.copy(certificationMaximum = value) } },
                            Modifier.weight(1f),
                        )
                    }
                }
                Text(copy.advancedDiscover.runtimeAndVotes, color = CinemaColors.Muted)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DiscoverTextField(
                        state.exploreDraftFilter.minimumRuntime?.toString().orEmpty(),
                        copy.advancedDiscover.minimumRuntime,
                        { value -> controller.updateExploreFilter { it.copy(minimumRuntime = value.toIntOrNull()) } },
                        Modifier.weight(1f),
                    )
                    DiscoverTextField(
                        state.exploreDraftFilter.maximumRuntime?.toString().orEmpty(),
                        copy.advancedDiscover.maximumRuntime,
                        { value -> controller.updateExploreFilter { it.copy(maximumRuntime = value.toIntOrNull()) } },
                        Modifier.weight(1f),
                    )
                    DiscoverTextField(
                        state.exploreDraftFilter.minimumVoteCount.takeIf { it > 0 }?.toString().orEmpty(),
                        copy.advancedDiscover.minimumVoteCount,
                        { value -> controller.updateExploreFilter { it.copy(minimumVoteCount = value.toIntOrNull() ?: 0) } },
                        Modifier.weight(1f),
                    )
                }
                Text(
                    "${copy.advancedDiscover.watchProviders} · ${copy.advancedDiscover.region}: ${state.exploreDraftFilter.region.orEmpty()}",
                    color = CinemaColors.Muted,
                )
                val providers = state.discoverConfiguration?.watchProviders?.values(state.exploreType).orEmpty()
                if (providers.isEmpty()) {
                    Text(copy.advancedDiscover.providersUnavailable, color = CinemaColors.Muted)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(providers, key = { it.id }) { provider ->
                            FilterChip(
                                selected = provider.id in state.exploreDraftFilter.watchProviderIds,
                                onClick = { controller.toggleWatchProvider(provider.id) },
                                label = { Text(provider.name) },
                            )
                        }
                    }
                }
                Text(copy.advancedDiscover.availability, color = CinemaColors.Muted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(WatchMonetizationType.entries) { type ->
                        FilterChip(
                            selected = type in state.exploreDraftFilter.monetizationTypes,
                            onClick = { controller.toggleMonetization(type) },
                            label = { Text(monetizationLabel(type, copy)) },
                        )
                    }
                }
                Text(copy.advancedDiscover.justWatch, style = MaterialTheme.typography.labelSmall, color = CinemaColors.Muted)
                    Button(
                        onClick = controller::applyExploreFilters,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Accent),
                    ) { Text(copy.advancedDiscover.apply) }
                }
            }
        }
        when (val result = state.explore) {
            LoadState.Idle, LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { LoadingPane(Modifier.height(320.dp)) }
            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                MessagePane(copy.serviceError, result.message, copy.retry, controller::reloadExplore, Modifier.height(340.dp))
            }
            is LoadState.Content -> {
                if (result.value.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { MessagePane(copy.noResults, modifier = Modifier.height(320.dp)) }
                } else {
                    items(result.value, key = { it.libraryKey }) { title ->
                        PosterCard(title, images, typeLabel(title.mediaType, copy), { controller.openDetail(title) }, Modifier.fillMaxWidth())
                    }
                    if (state.explorePage < state.exploreTotalPages) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Button(
                                onClick = controller::loadMoreExplore,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Surface),
                            ) { Text(copy.loadMore) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
    )
}

private fun monetizationLabel(type: WatchMonetizationType, copy: UiStrings): String = when (type) {
    WatchMonetizationType.SUBSCRIPTION -> copy.advancedDiscover.subscription
    WatchMonetizationType.FREE -> copy.advancedDiscover.free
    WatchMonetizationType.ADS -> copy.advancedDiscover.withAds
    WatchMonetizationType.RENT -> copy.advancedDiscover.rent
    WatchMonetizationType.BUY -> copy.advancedDiscover.buy
}

private const val CURRENT_YEAR = 2026

@Composable
private fun SearchScreen(state: SmartMovieState, copy: UiStrings, controller: AppController) {
    val images = remember(state.imageConfiguration) { ImageUrlFactory(state.imageConfiguration) }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                SectionTitle(copy.search)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(CatalogSearchMode.entries) { mode ->
                        FilterChip(
                            selected = state.searchMode == mode,
                            onClick = { controller.changeSearchMode(mode) },
                            label = { Text(if (mode == CatalogSearchMode.CATALOG) copy.catalog else copy.externalId) },
                        )
                    }
                }
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = controller::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text(
                            if (state.searchMode == CatalogSearchMode.CATALOG) copy.searchHint
                            else state.externalIdSource.example,
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaColors.Accent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                        focusedContainerColor = CinemaColors.Elevated,
                        unfocusedContainerColor = CinemaColors.Elevated,
                    ),
                    shape = RoundedCornerShape(17.dp),
                )
                if (state.searchMode == CatalogSearchMode.CATALOG) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(SearchScopeV2.entries) { scope ->
                            FilterChip(
                                selected = state.searchScopeV2 == scope,
                                onClick = { controller.changeSearchScope(scope) },
                                label = { Text(scopeLabelV2(scope, copy, state.locale)) },
                            )
                        }
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(ExternalIdSource.entries) { source ->
                            FilterChip(
                                selected = state.externalIdSource == source,
                                onClick = { controller.changeExternalIdSource(source) },
                                label = { Text(source.displayName) },
                            )
                        }
                    }
                    Button(
                        onClick = controller::findExternalId,
                        enabled = state.searchQuery.isNotBlank() && state.externalIdSearch !is LoadState.Loading,
                    ) { Text(copy.findMatches) }
                }
            }
        }
        when (val result = if (state.searchMode == CatalogSearchMode.CATALOG) state.entitySearch else state.externalIdSearch) {
            LoadState.Idle -> item(span = { GridItemSpan(maxLineSpan) }) {
                MessagePane(
                    if (state.searchMode == CatalogSearchMode.CATALOG) copy.searchHint else copy.searchByExternalId,
                    if (state.searchMode == CatalogSearchMode.CATALOG) null else copy.externalIdHint,
                    modifier = Modifier.height(320.dp),
                )
            }
            LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { LoadingPane(Modifier.height(320.dp)) }
            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                MessagePane(copy.serviceError, result.message, copy.retry, controller::retrySearch, Modifier.height(340.dp))
            }
            is LoadState.Content -> {
                if (result.value.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        MessagePane(
                            copy.noResults,
                            if (state.searchMode == CatalogSearchMode.EXTERNAL_ID) copy.tryAnotherExternalId else null,
                            modifier = Modifier.height(320.dp),
                        )
                    }
                } else {
                    items(result.value, key = CatalogEntity::stableKey) { entity ->
                        CatalogEntityCard(entity, images, copy, state.locale, { controller.openEntity(entity) })
                    }
                    if (state.searchMode == CatalogSearchMode.CATALOG && state.searchPage < state.searchTotalPages) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Button(
                                onClick = controller::loadMoreSearch,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Surface),
                            ) { Text(copy.loadMore) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogEntityCard(
    entity: CatalogEntity,
    images: ImageUrlFactory,
    copy: UiStrings,
    locale: AppLocale,
    onOpen: () -> Unit,
) {
    when (entity) {
        is CatalogEntity.Title -> PosterCard(
            entity.value,
            images,
            typeLabel(entity.value.mediaType, copy),
            onOpen,
            Modifier.fillMaxWidth(),
        )
        else -> {
            val title = when (entity) {
                is CatalogEntity.Person -> entity.value.name
                is CatalogEntity.Collection -> entity.value.name
                is CatalogEntity.Organization -> entity.value.name
                is CatalogEntity.Keyword -> entity.value.name
                is CatalogEntity.Season -> entity.value.name
                is CatalogEntity.Episode -> entity.value.name
                is CatalogEntity.Title -> entity.value.displayTitle
            }
            val path = when (entity) {
                is CatalogEntity.Person -> entity.value.profilePath
                is CatalogEntity.Collection -> entity.value.posterPath
                is CatalogEntity.Organization -> entity.value.logoPath
                is CatalogEntity.Season -> entity.value.posterPath
                is CatalogEntity.Episode -> entity.value.stillPath
                else -> null
            }
            val url = when (entity) {
                is CatalogEntity.Person -> images.profile(path)
                is CatalogEntity.Episode -> images.backdrop(path)
                else -> images.poster(path)
            }
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
                shape = CinemaCardShape,
                color = CinemaColors.Elevated,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    if (url != null) RemoteArtwork(url, title, Modifier.fillMaxWidth().aspectRatio(0.72f))
                    else Box(Modifier.fillMaxWidth().aspectRatio(0.72f).background(CinemaColors.Surface), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CinemaColors.Muted, modifier = Modifier.size(42.dp))
                    }
                    Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(entityKindLabel(entity.entityKind, locale), style = MaterialTheme.typography.labelMedium, color = CinemaColors.Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(state: SmartMovieState, copy: UiStrings, controller: AppController) {
    val images = remember(state.imageConfiguration) { ImageUrlFactory(state.imageConfiguration) }
    val records = state.library.filter {
        if (state.libraryCollection == LibraryCollection.FAVORITES) it.isFavorite else it.isWatchlisted
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                SectionTitle(copy.library)
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    LibraryCollection.entries.forEach { collection ->
                        FilterChip(
                            selected = state.libraryCollection == collection,
                            onClick = { controller.changeLibraryCollection(collection) },
                            label = { Text(if (collection == LibraryCollection.FAVORITES) copy.favorites else copy.watchlist) },
                        )
                    }
                }
            }
        }
        if (records.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { MessagePane(copy.emptyLibrary, modifier = Modifier.height(320.dp)) }
        } else {
            items(records, key = { it.title.libraryKey }) { record ->
                PosterCard(
                    record.title,
                    images,
                    typeLabel(record.title.mediaType, copy),
                    { controller.openDetail(record.title) },
                    Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DetailPane(
    state: SmartMovieState,
    copy: UiStrings,
    controller: AppController,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        color = CinemaColors.Background,
        shadowElevation = 24.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        when (val detail = state.detail) {
            LoadState.Idle, LoadState.Loading -> Box(Modifier.fillMaxSize()) {
                DetailClose(copy.close, controller::closeDetail, Modifier.align(Alignment.TopEnd).padding(18.dp))
                LoadingPane()
            }
            is LoadState.Error -> Box(Modifier.fillMaxSize()) {
                DetailClose(copy.close, controller::closeDetail, Modifier.align(Alignment.TopEnd).padding(18.dp))
                MessagePane(copy.serviceError, detail.message, copy.retry, controller::retryDetail)
            }
            is LoadState.Content -> DetailContent(detail.value, state, copy, controller)
        }
    }
}

@Composable
private fun EntityDetailPane(
    state: SmartMovieState,
    copy: UiStrings,
    controller: AppController,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        color = CinemaColors.Background,
        shadowElevation = 24.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        when (val result = state.entityDetail) {
            LoadState.Idle, LoadState.Loading -> Box(Modifier.fillMaxSize()) {
                DetailClose(copy.close, controller::closeDetail, Modifier.align(Alignment.TopEnd).padding(18.dp))
                LoadingPane()
            }
            is LoadState.Error -> Box(Modifier.fillMaxSize()) {
                DetailClose(copy.close, controller::closeDetail, Modifier.align(Alignment.TopEnd).padding(18.dp))
                MessagePane(copy.serviceError, result.message, copy.retry, controller::retryEntityDetail)
            }
            is LoadState.Content -> EntityDetailContent(result.value, state, copy, controller)
        }
    }
}

@Composable
private fun EntityDetailContent(
    detail: EntityDetail,
    state: SmartMovieState,
    copy: UiStrings,
    controller: AppController,
) {
    val images = remember(state.imageConfiguration) { ImageUrlFactory(state.imageConfiguration) }
    val mediaCopy = mediaCopy(state.locale)
    val title: String
    val overview: String
    val image: String?
    val related: List<TitleSummary>
    when (detail) {
        is EntityDetail.Person -> {
            title = detail.value.name
            overview = detail.value.biography
            image = images.profile(detail.value.profilePath)
            related = detail.value.knownFor.ifEmpty { detail.value.credits.cast.mapNotNull(::creditTitle) }
        }
        is EntityDetail.Collection -> {
            title = detail.value.name
            overview = detail.value.overview
            image = images.backdrop(detail.value.backdropPath ?: detail.value.posterPath)
            related = detail.value.parts
        }
        is EntityDetail.Organization -> {
            title = detail.value.name
            overview = detail.value.description
            image = images.poster(detail.value.logoPath)
            related = detail.value.titles.results
        }
        is EntityDetail.Keyword -> {
            title = detail.value.name
            overview = entityKindLabel(EntityKind.KEYWORD, state.locale)
            image = null
            related = detail.value.titles.results
        }
        is EntityDetail.Season -> {
            title = detail.value.name
            overview = detail.value.overview
            image = images.poster(detail.value.posterPath)
            related = emptyList()
        }
        is EntityDetail.Episode -> {
            title = detail.value.name
            overview = detail.value.overview
            image = images.backdrop(detail.value.stillPath)
            related = emptyList()
        }
        is EntityDetail.Credit -> {
            title = detail.value.personSummary?.name ?: copy.creditDetails
            overview = ""
            image = images.profile(detail.value.personSummary?.profilePath)
            related = listOfNotNull(detail.value.titleSummary)
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(330.dp)) {
                RemoteArtwork(image, title, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, CinemaColors.Background))))
                DetailClose(copy.close, controller::closeDetail, Modifier.align(Alignment.TopEnd).padding(18.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.align(Alignment.BottomStart).padding(28.dp).semantics { heading() },
                )
            }
        }
        if (overview.isNotBlank()) item {
            Column(Modifier.padding(horizontal = 28.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(copy.story)
                Text(overview, color = CinemaColors.Foreground.copy(alpha = 0.82f), style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (detail is EntityDetail.Person) item {
            Column(Modifier.padding(horizontal = 28.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                CreditShelf(copy.cast, detail.value.credits.cast, images, controller::openCredit)
                CreditShelf(copy.crew, detail.value.credits.crew, images, controller::openCredit)
            }
        }
        if (detail is EntityDetail.Season) item {
            Column(Modifier.padding(horizontal = 28.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                val episodes = detail.value.episodes.map { it.episodeNumber }.distinct()
                val watchedCount = episodes.count { "${detail.value.seriesId}:${detail.value.seasonNumber}:$it" in state.watchedEpisodeKeys }
                Text(mediaCopy.episodeProgress, style = MaterialTheme.typography.titleLarge)
                Text(mediaCopy.watchedProgress.replace("%1${'$'}d", watchedCount.toString()).replace("%2${'$'}d", episodes.size.toString()), color = CinemaColors.Muted)
                Button(
                    onClick = { controller.setSeasonWatched(detail.value.seriesId, detail.value.seasonNumber, episodes, watchedCount != episodes.size) },
                    enabled = episodes.isNotEmpty(),
                ) { Text(if (watchedCount == episodes.size) mediaCopy.markSeasonUnwatched else mediaCopy.markSeasonWatched) }
                CatalogMetadataPanel(
                    values = buildList {
                        detail.value.airDate?.let { add(mediaCopy.airDate to it.take(10)) }
                        add(mediaCopy.episodes to detail.value.episodeCount.toString())
                    },
                    externalIds = detail.value.externalIds,
                    copy = mediaCopy,
                )
                CatalogMediaPanel(detail.value.images, detail.value.videos, images, mediaCopy)
                CreditShelf(copy.cast, detail.value.credits.cast, images, controller::openCredit)
                CreditShelf(copy.crew, detail.value.credits.crew, images, controller::openCredit)
            }
        }
        if (detail is EntityDetail.Credit) item {
            Column(
                Modifier.padding(horizontal = 28.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SectionTitle(copy.role)
                detail.value.character?.let { Text("${copy.character}: $it") }
                detail.value.job?.let { Text("${copy.job}: $it") }
                detail.value.department?.let { Text("${copy.department}: $it") }
            }
        }
        if (detail is EntityDetail.Credit) detail.value.personSummary?.let { person ->
            item {
                AssistChip(
                    onClick = { controller.openEntity(CatalogEntity.Person(person)) },
                    label = { Text("${copy.person}: ${person.name}") },
                    modifier = Modifier.padding(horizontal = 28.dp),
                )
            }
        }
        if (detail is EntityDetail.Season && detail.value.episodes.isNotEmpty()) item {
            Column(Modifier.padding(horizontal = 28.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(entityKindLabel(EntityKind.EPISODE, state.locale))
                val nextEpisode = detail.value.episodes.firstOrNull {
                    "${it.seriesId}:${it.seasonNumber}:${it.episodeNumber}" !in state.watchedEpisodeKeys
                }
                nextEpisode?.let { episode ->
                    Button(onClick = { controller.openEntity(CatalogEntity.Episode(episode)) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(continueEpisodeLabel(state.locale), modifier = Modifier.padding(start = 8.dp))
                    }
                }
                detail.value.episodes.forEach { episode ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { controller.openEntity(CatalogEntity.Episode(episode)) },
                        color = CinemaColors.Elevated,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("${episode.episodeNumber}. ${episode.name}", style = MaterialTheme.typography.titleMedium)
                            episode.airDate?.let { Text(it, color = CinemaColors.Muted) }
                            val key = episode.episodeKey
                            Button(onClick = { controller.setEpisodeWatched(episode.seriesId, episode.seasonNumber, episode.episodeNumber, key !in state.watchedEpisodeKeys) }) {
                                Text(if (key in state.watchedEpisodeKeys) mediaCopy.markAsUnwatched else mediaCopy.markAsWatched)
                            }
                        }
                    }
                }
            }
        }
        if (detail is EntityDetail.Episode) item {
            Column(Modifier.padding(horizontal = 28.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val key = "${detail.value.seriesId}:${detail.value.seasonNumber}:${detail.value.episodeNumber}"
                Button(onClick = { controller.setEpisodeWatched(detail.value.seriesId, detail.value.seasonNumber, detail.value.episodeNumber, key !in state.watchedEpisodeKeys) }) {
                    Text(if (key in state.watchedEpisodeKeys) mediaCopy.markAsUnwatched else mediaCopy.markAsWatched)
                }
                CatalogMetadataPanel(
                    values = buildList {
                        detail.value.airDate?.let { add(mediaCopy.airDate to it.take(10)) }
                        detail.value.runtimeMinutes?.let {
                            add(mediaCopy.runtime to mediaCopy.minutes.replace("%d", it.toString()))
                        }
                        detail.value.productionCode?.takeIf(String::isNotBlank)?.let {
                            add(mediaCopy.productionCode to it)
                        }
                        detail.value.voteAverage?.let { add(mediaCopy.rating to "$it / 10") }
                        add(mediaCopy.votes to detail.value.voteCount.toString())
                    },
                    externalIds = detail.value.externalIds,
                    copy = mediaCopy,
                )
                CatalogMediaPanel(detail.value.images, detail.value.videos, images, mediaCopy)
                CreditShelf(copy.guestStars, detail.value.guestStars, images, controller::openCredit)
                CreditShelf(copy.crew, detail.value.crew, images, controller::openCredit)
            }
        }
        if (detail is EntityDetail.Episode && state.account is AccountState.SignedIn) item {
            AccountRatingPanel(
                rating = state.episodeRating,
                locale = state.locale,
                onChange = controller::rateEpisode,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 18.dp),
            )
        }
        if (related.isNotEmpty()) item {
            Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionTitle(
                    if (detail is EntityDetail.Credit) copy.creditTitle else copy.details,
                    Modifier.padding(horizontal = 28.dp),
                )
                LazyRow(contentPadding = PaddingValues(horizontal = 28.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(related.distinctBy(TitleSummary::libraryKey), key = { it.libraryKey }) { item ->
                        PosterCard(item, images, typeLabel(item.mediaType, copy), { controller.openDetail(item) })
                    }
                }
            }
        }
    }
}

private fun continueEpisodeLabel(locale: AppLocale): String = when (locale) {
    AppLocale.ENGLISH -> "Continue with episode"
    AppLocale.VIETNAMESE -> "Tiếp tục với tập phim"
    AppLocale.JAPANESE -> "エピソードを続ける"
    AppLocale.KOREAN -> "에피소드 계속 보기"
    AppLocale.CHINESE_SIMPLIFIED -> "继续观看剧集"
    AppLocale.CHINESE_TRADITIONAL -> "繼續觀看劇集"
}

private fun creditTitle(credit: com.lamndt.smartmovie.multiplatform.model.Credit): TitleSummary? {
    val id = credit.id ?: return null
    val mediaType = credit.mediaType ?: return null
    val title = credit.title ?: return null
    return TitleSummary(id, mediaType, title, title, "", posterPath = credit.posterPath)
}

@Composable
private fun CreditShelf(
    label: String,
    credits: List<Credit>,
    images: ImageUrlFactory,
    onCredit: (Credit) -> Unit,
) {
    if (credits.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle(label)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(credits.take(40).mapIndexed { index, credit -> index to credit }, key = { (index, credit) ->
                credit.creditId ?: "$index:${credit.id}"
            }) { (_, credit) ->
                val profile = credit.mediaType == null
                Column(
                    Modifier.width(112.dp).clickable(enabled = credit.creditId != null) { onCredit(credit) },
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    RemoteArtwork(
                        if (profile) images.profile(credit.profilePath) else images.poster(credit.posterPath),
                        credit.title.orEmpty(),
                        Modifier.fillMaxWidth().aspectRatio(if (profile) 0.78f else 0.68f).clip(CinemaCardShape),
                    )
                    Text(credit.title.orEmpty(), style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    (credit.character ?: credit.job ?: credit.department)?.let { role ->
                        Text(role, style = MaterialTheme.typography.labelMedium, color = CinemaColors.Muted, maxLines = 2)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(detail: TitleDetail, state: SmartMovieState, copy: UiStrings, controller: AppController) {
    val images = remember(state.imageConfiguration) { ImageUrlFactory(state.imageConfiguration) }
    val record = state.library.firstOrNull { it.title.libraryKey == detail.summary.libraryKey }
    val trailer = preferredTrailer(detail.videos, state.locale.backendTag)
    val metadataCopy = titleMetadataCopy(state.locale)
    val mediaCopy = mediaCopy(state.locale)
    val editorialCopy = editorialCopy(state.locale)
    val region = controller.contentRegion()
    val includeAdult = state.adultConfigured && state.adultUnlocked && state.adultLockUntil <= systemTimeMillis()
    val similar = presentedEditorialTitles(detail.similar, includeAdult = includeAdult)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(330.dp)) {
                RemoteArtwork(images.backdrop(detail.backdropPath), detail.title, Modifier.fillMaxSize())
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, CinemaColors.Background.copy(alpha = 0.36f), CinemaColors.Background)),
                    ),
                )
                DetailClose(copy.close, controller::closeDetail, Modifier.align(Alignment.TopEnd).padding(18.dp))
                Column(
                    Modifier.align(Alignment.BottomStart).padding(horizontal = 28.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RatingBadge(detail.voteAverage)
                        detail.releaseDate?.take(4)?.let { Text(it, color = CinemaColors.Muted) }
                        detail.status?.let { Text("• $it", color = CinemaColors.Muted) }
                    }
                    Text(detail.title, style = MaterialTheme.typography.displayMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 28.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    trailer?.let {
                        Button(
                            onClick = { openExternalUrl("https://www.youtube.com/watch?v=${it.key}") },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Accent),
                            modifier = Modifier.height(50.dp),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text(copy.trailer, Modifier.padding(start = 7.dp))
                        }
                    }
                    Button(
                        onClick = { controller.toggleLibrary(detail.summary, LibraryCollection.FAVORITES) },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Surface),
                        modifier = Modifier.height(50.dp),
                    ) {
                        Icon(if (record?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
                        Text(if (record?.isFavorite == true) copy.removeFavorite else copy.favorite, Modifier.padding(start = 7.dp))
                    }
                    Button(
                        onClick = { controller.toggleLibrary(detail.summary, LibraryCollection.WATCHLIST) },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Surface),
                        modifier = Modifier.height(50.dp),
                    ) {
                        Icon(if (record?.isWatchlisted == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null)
                        Text(if (record?.isWatchlisted == true) copy.removeWatchlist else copy.watchLater, Modifier.padding(start = 7.dp))
                    }
                }
                if (state.account is AccountState.SignedIn) {
                    AccountRatingPanel(
                        rating = state.detailRating,
                        locale = state.locale,
                        onChange = controller::rateTitle,
                    )
                }
                if (detail.genres.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        detail.genres.forEach { genre -> AssistChip(onClick = {}, label = { Text(genre.name) }) }
                    }
                }
                SectionTitle(copy.story)
                state.deepDetail?.tagline?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.titleMedium, color = CinemaColors.Accent)
                }
                Text(detail.overview, style = MaterialTheme.typography.bodyLarge, color = CinemaColors.Foreground.copy(alpha = 0.82f))
                state.deepDetail?.collection?.let { collection ->
                    AssistChip(
                        onClick = { controller.openEntity(CatalogEntity.Collection(collection)) },
                        label = { Text(collection.name) },
                    )
                }
                state.deepDetail?.let { deep ->
                    val organizations = deep.companies + deep.networks
                    if (organizations.isNotEmpty()) {
                        SectionTitle(metadataCopy.production)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            organizations.forEach { organization ->
                                AssistChip(
                                    onClick = { controller.openEntity(CatalogEntity.Organization(organization)) },
                                    label = { Text(organization.name) },
                                )
                            }
                        }
                    }
                    TitleMetadataPanel(
                        detail = deep,
                        language = state.locale.backendTag,
                        region = region,
                        copy = metadataCopy,
                    )
                    CatalogMediaPanel(
                        imageAssets = deep.images.backdrops + deep.images.posters + deep.images.logos,
                        videos = deep.videos,
                        images = images,
                        copy = mediaCopy,
                    )
                }
                state.deepDetail?.watchProviders?.firstOrNull { it.region.equals(region, ignoreCase = true) }?.let { providers ->
                    SectionTitle("${providers.region} · ${providers.attribution}")
                    val names = (providers.stream + providers.rent + providers.buy).distinctBy { it.providerId }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        names.forEach { provider -> AssistChip(onClick = { providers.tmdbUrl?.let(::openExternalUrl) }, label = { Text(provider.providerName) }) }
                    }
                }
                state.deepDetail?.seasons?.takeIf { it.isNotEmpty() }?.let { seasons ->
                    SectionTitle(entityKindLabel(EntityKind.SEASON, state.locale))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        seasons.forEach { season ->
                            AssistChip(onClick = { controller.openEntity(CatalogEntity.Season(season)) }, label = { Text(season.name) })
                        }
                    }
                }
            }
        }
        val deepCredits = state.deepDetail
        if (deepCredits != null) {
            item {
                Column(
                    Modifier.padding(start = 28.dp, top = 28.dp, end = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    CreditShelf(copy.cast, deepCredits.cast, images, controller::openCredit)
                    CreditShelf(copy.crew, deepCredits.crew, images, controller::openCredit)
                }
            }
        } else if (detail.cast.isNotEmpty()) {
            item {
                Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionTitle(copy.cast, Modifier.padding(horizontal = 28.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(detail.cast, key = { it.id }) { member ->
                            Column(
                                Modifier.width(112.dp).clickable {
                                    controller.openEntity(CatalogEntity.Person(PersonSummary(member.id, member.name, member.profilePath)))
                                },
                                verticalArrangement = Arrangement.spacedBy(7.dp),
                            ) {
                                RemoteArtwork(
                                    images.profile(member.profilePath),
                                    member.name,
                                    Modifier.fillMaxWidth().aspectRatio(0.78f).clip(CinemaCardShape),
                                )
                                Text(member.name, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                member.character?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = CinemaColors.Muted, maxLines = 2) }
                            }
                        }
                    }
                }
            }
        }
        state.deepDetail?.let { deep ->
            val reviews = presentedReviews(deep.reviews.results)
            if (reviews.isNotEmpty()) {
                item {
                    CatalogReviewPanel(
                        values = reviews,
                        copy = editorialCopy,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 28.dp),
                    )
                }
            }
            val recommendations = presentedEditorialTitles(
                deep.recommendations.results,
                deep.summary.libraryKey,
                includeAdult,
            )
            if (recommendations.isNotEmpty()) {
                item {
                    CatalogEditorialTitleShelf(
                        title = editorialCopy.recommendations,
                        values = recommendations,
                        images = images,
                        copy = copy,
                        onTitleClick = controller::openDetail,
                    )
                }
            }
        }
        if (similar.isNotEmpty()) {
            item {
                Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionTitle(copy.similar, Modifier.padding(horizontal = 28.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(similar, key = { it.libraryKey }) { title ->
                            PosterCard(title, images, typeLabel(title.mediaType, copy), { controller.openDetail(title) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogReviewPanel(
    values: List<Review>,
    copy: EditorialCopy,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle(copy.reviews)
        values.forEach { review ->
            Surface(shape = CinemaCardShape, color = CinemaColors.Surface) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                review.author.takeIf(String::isNotBlank) ?: copy.tmdbMember,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            review.createdAt?.let {
                                Text(it.take(10), color = CinemaColors.Muted, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        review.rating?.let { RatingBadge(it) }
                    }
                    Text(
                        review.content,
                        color = CinemaColors.Muted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogEditorialTitleShelf(
    title: String,
    values: List<TitleSummary>,
    images: ImageUrlFactory,
    copy: UiStrings,
    onTitleClick: (TitleSummary) -> Unit,
) {
    Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle(title, Modifier.padding(horizontal = 28.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(values, key = TitleSummary::libraryKey) { value ->
                PosterCard(value, images, typeLabel(value.mediaType, copy), { onTitleClick(value) })
            }
        }
    }
}

private data class EditorialCopy(
    val reviews: String,
    val recommendations: String,
    val tmdbMember: String,
)

private fun editorialCopy(locale: AppLocale): EditorialCopy = when (locale) {
    AppLocale.ENGLISH -> EditorialCopy("Reviews", "Recommendations", "TMDb member")
    AppLocale.VIETNAMESE -> EditorialCopy("Bài đánh giá", "Đề xuất", "Thành viên TMDb")
    AppLocale.JAPANESE -> EditorialCopy("レビュー", "おすすめ", "TMDbメンバー")
    AppLocale.KOREAN -> EditorialCopy("리뷰", "추천", "TMDb 회원")
    AppLocale.CHINESE_SIMPLIFIED -> EditorialCopy("影评", "推荐", "TMDb 成员")
    AppLocale.CHINESE_TRADITIONAL -> EditorialCopy("影評", "推薦", "TMDb 會員")
}

@Composable
private fun TitleMetadataPanel(detail: TitleDetailV2, language: String, region: String, copy: TitleMetadataCopy) {
    val release = detail.releaseInformationFor(region)
    val aliases = detail.displayAlternativeTitles(region).take(6)
    val translations = detail.displayTranslations(language).take(6)
    val externalIds = detail.externalIds
        .filterValues(String::isNotBlank)
        .entries
        .sortedBy { entry -> entry.key }
        .take(8)
    if (release == null && aliases.isEmpty() && translations.isEmpty() && externalIds.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(copy.releaseAndLocalization)
        release?.certification?.let { Text("${copy.certification}: $it", color = CinemaColors.Muted) }
        release?.firstReleaseDate?.let { Text("${copy.releaseDate}: ${it.take(10)}", color = CinemaColors.Muted) }
        if (aliases.isNotEmpty()) {
            Text(copy.alternativeTitles, fontWeight = FontWeight.SemiBold)
            aliases.forEach { alias ->
                Text(listOfNotNull(alias.title, alias.countryCode).joinToString(" · "), color = CinemaColors.Muted)
            }
        }
        if (translations.isNotEmpty()) {
            Text(copy.translations, fontWeight = FontWeight.SemiBold)
            translations.forEach { translation ->
                val language = translation.languageName.ifBlank { translation.languageCode.uppercase() }
                Text("${translation.localizedTitle} · $language", color = CinemaColors.Muted)
            }
        }
        if (externalIds.isNotEmpty()) {
            Text(copy.externalIdentifiers, fontWeight = FontWeight.SemiBold)
            externalIds.forEach { (source, value) -> Text("$source: $value", color = CinemaColors.Muted) }
        }
    }
}

@Composable
private fun CatalogMediaPanel(
    imageAssets: List<ImageAsset>,
    videos: List<Video>,
    images: ImageUrlFactory,
    copy: MediaCopy,
) {
    val displayedImages = presentedImages(imageAssets)
    val displayedVideos = playableVideos(videos)
    if (displayedImages.isEmpty() && displayedVideos.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        if (displayedImages.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(copy.images)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(displayedImages, key = ImageAsset::filePath) { asset ->
                        val poster = asset.kind.equals("poster", ignoreCase = true)
                        RemoteArtwork(
                            url = if (poster) images.poster(asset.filePath, expanded = true) else images.backdrop(asset.filePath),
                            contentDescription = copy.imageDescription,
                            modifier = Modifier.width(if (poster) 140.dp else 240.dp)
                                .height(if (poster) 210.dp else 135.dp)
                                .clip(CinemaCardShape),
                            contentScale = if (asset.kind.equals("logo", ignoreCase = true)) ContentScale.Fit else ContentScale.Crop,
                        )
                    }
                }
            }
        }
        if (displayedVideos.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(copy.videos)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(displayedVideos, key = Video::key) { video ->
                        Surface(shape = RoundedCornerShape(16.dp), color = CinemaColors.Elevated) {
                            TextButton(
                                onClick = { openExternalUrl("https://www.youtube.com/watch?v=${video.key}") },
                                modifier = Modifier.width(240.dp).padding(5.dp),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CinemaColors.Accent)
                                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                                    Text(video.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(video.type, color = CinemaColors.Muted, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogMetadataPanel(
    values: List<Pair<String, String>>,
    externalIds: Map<String, String>,
    copy: MediaCopy,
) {
    val identifiers = presentedExternalIds(externalIds)
    if (values.isEmpty() && identifiers.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(copy.details)
        values.forEach { (label, value) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontWeight = FontWeight.SemiBold)
                Text(value, color = CinemaColors.Muted)
            }
        }
        if (identifiers.isNotEmpty()) {
            Text(copy.externalIdentifiers, fontWeight = FontWeight.SemiBold)
            identifiers.forEach { (source, value) -> Text("$source: $value", color = CinemaColors.Muted) }
        }
    }
}

private data class MediaCopy(
    val details: String,
    val images: String,
    val videos: String,
    val imageDescription: String,
    val airDate: String,
    val episodes: String,
    val runtime: String,
    val productionCode: String,
    val rating: String,
    val votes: String,
    val minutes: String,
    val externalIdentifiers: String,
    val episodeProgress: String,
    val watchedProgress: String,
    val markAsWatched: String,
    val markAsUnwatched: String,
    val markSeasonWatched: String,
    val markSeasonUnwatched: String,
)

private fun mediaCopy(locale: AppLocale): MediaCopy = when (locale) {
    AppLocale.ENGLISH -> MediaCopy(
        "Details", "Images", "Videos", "TMDb catalog image", "Air date", "Episodes", "Runtime",
        "Production code", "Rating", "Votes", "%d min", "External identifiers", "Episode progress", "Watched %1${'$'}d of %2${'$'}d", "Mark watched", "Mark unwatched", "Mark season watched", "Mark season unwatched",
    )
    AppLocale.VIETNAMESE -> MediaCopy(
        "Chi tiết", "Hình ảnh", "Video", "Hình ảnh danh mục TMDb", "Ngày phát sóng", "Tập phim", "Thời lượng",
        "Mã sản xuất", "Điểm đánh giá", "Lượt đánh giá", "%d phút", "Định danh bên ngoài", "Tiến độ tập", "Đã xem %1${'$'}d/%2${'$'}d", "Đã xem", "Bỏ đã xem", "Đánh dấu mùa đã xem", "Bỏ đánh dấu mùa",
    )
    AppLocale.JAPANESE -> MediaCopy(
        "詳細", "画像", "動画", "TMDbカタログ画像", "放送日", "エピソード", "上映時間", "制作コード", "評価", "投票数", "%d分", "外部識別子", "エピソードの進捗", "%1${'$'}d/%2${'$'}d 話を視聴済み", "視聴済みにする", "未視聴に戻す", "シーズンを視聴済みにする", "シーズンを未視聴に戻す",
    )
    AppLocale.KOREAN -> MediaCopy(
        "세부 정보", "이미지", "동영상", "TMDb 카탈로그 이미지", "방영일", "에피소드", "재생 시간", "제작 코드", "평점", "투표 수", "%d분", "외부 식별자", "에피소드 진행률", "%1${'$'}d개 중 %2${'$'}d개 시청", "시청 완료", "시청 취소", "시즌 시청 완료", "시즌 시청 취소",
    )
    AppLocale.CHINESE_SIMPLIFIED -> MediaCopy(
        "详细信息", "图片", "视频", "TMDb 目录图片", "播出日期", "剧集", "时长", "制作代码", "评分", "投票数", "%d 分钟", "外部标识符", "剧集进度", "已看 %1${'$'}d/%2${'$'}d", "标记为已看", "标记为未看", "标记本季已看", "取消本季已看",
    )
    AppLocale.CHINESE_TRADITIONAL -> MediaCopy(
        "詳細資料", "圖片", "影片", "TMDb 目錄圖片", "播出日期", "集數", "片長", "製作代碼", "評分", "投票數", "%d 分鐘", "外部識別碼", "劇集進度", "已看 %1${'$'}d/%2${'$'}d", "標記為已看", "標記為未看", "標記本季已看", "取消本季已看",
    )
}

private data class TitleMetadataCopy(
    val production: String,
    val releaseAndLocalization: String,
    val certification: String,
    val releaseDate: String,
    val alternativeTitles: String,
    val translations: String,
    val externalIdentifiers: String,
    val aboutDescription: String,
)

private fun titleMetadataCopy(locale: AppLocale): TitleMetadataCopy = when (locale) {
    AppLocale.ENGLISH -> TitleMetadataCopy(
        "Production", "Release & localization", "Certification", "Release date", "Alternative titles", "Translations",
        "External identifiers", "Explore TMDb image and video galleries, member reviews, recommendations, regional release dates, certifications, localized titles, translations and external identifiers. Artwork uses normalized TMDb CDN URLs and shows a placeholder when unavailable. The local adult-content PIN applies to Search and every related Person, Collection, Company, Network, Keyword and Credit Detail surface. Sign in through TMDb to sync favorites, watchlist, ratings, recommendations, and mixed Movie/TV lists with durable offline changes and safe retry.",
    )
    AppLocale.VIETNAMESE -> TitleMetadataCopy(
        "Sản xuất", "Phát hành & bản địa hóa", "Phân loại độ tuổi", "Ngày phát hành", "Tên gọi khác", "Bản dịch",
        "Định danh bên ngoài", "Khám phá hình ảnh, video, bài đánh giá, đề xuất, thông tin phát hành, bản địa hóa và định danh ngoài từ TMDb. Ảnh dùng URL CDN TMDb đã chuẩn hóa và hiển thị ảnh giữ chỗ khi không khả dụng. PIN nội dung 18+ cục bộ áp dụng cho Tìm kiếm và mọi trang Người, Bộ sưu tập, Công ty, Mạng, Từ khóa và Chi tiết vai trò liên quan. Đăng nhập qua TMDb để đồng bộ Yêu thích, Danh sách xem, điểm đánh giá, đề xuất và danh sách Phim/TV hỗn hợp; thay đổi offline được lưu bền vững và thử lại an toàn.",
    )
    AppLocale.JAPANESE -> TitleMetadataCopy(
        "制作", "公開・ローカライズ", "年齢区分", "公開日", "別タイトル", "翻訳", "外部識別子",
        "TMDbの画像、動画、レビュー、おすすめ、公開情報、ローカライズ、外部識別子を確認できます。画像は正規化したTMDb CDN URLを使用し、利用できない場合はプレースホルダーを表示します。端末内の成人向けPINは、検索と人物、コレクション、会社、ネットワーク、キーワード、クレジット詳細の関連画面すべてに適用されます。TMDb経由でログインし、お気に入り、ウォッチリスト、評価、おすすめ、映画とTVの混合リストを同期できます。オフライン変更は永続化され、安全に再試行されます。",
    )
    AppLocale.KOREAN -> TitleMetadataCopy(
        "제작", "공개 및 현지화", "시청 등급", "공개일", "대체 제목", "번역", "외부 식별자",
        "TMDb 이미지, 동영상, 리뷰, 추천, 공개 정보, 현지화 및 외부 식별자를 확인하세요. 이미지는 정규화된 TMDb CDN URL을 사용하며 사용할 수 없을 때 자리 표시자를 보여 줍니다. 기기 내 성인 콘텐츠 PIN은 검색과 인물, 컬렉션, 제작사, 네트워크, 키워드 및 크레딧 상세의 모든 관련 화면에 적용됩니다. TMDb를 통해 로그인하여 즐겨찾기, 시청 목록, 평점, 추천, 영화/TV 혼합 목록을 동기화하세요. 오프라인 변경은 안전하게 저장되고 재시도됩니다.",
    )
    AppLocale.CHINESE_SIMPLIFIED -> TitleMetadataCopy(
        "制作", "发行与本地化", "分级", "上映日期", "其他片名", "翻译", "外部标识符",
        "查看 TMDb 图片、视频、影评、推荐、发行信息、本地化内容和外部标识符。图片使用规范化的 TMDb CDN URL；不可用时会显示占位图。本地成人内容 PIN 适用于搜索及人物、合集、公司、电视网、关键词和演职员详情的所有相关页面。通过 TMDb 登录，同步收藏、观看列表、评分、推荐及电影/电视混合片单；离线更改会持久保存并安全重试。",
    )
    AppLocale.CHINESE_TRADITIONAL -> TitleMetadataCopy(
        "製作", "發行與在地化", "分級", "上映日期", "其他片名", "翻譯", "外部識別碼",
        "查看 TMDb 圖片、影片、影評、推薦、發行資訊、在地化內容和外部識別碼。圖片使用標準化的 TMDb CDN URL；無法使用時會顯示預留位置。本機成人內容 PIN 適用於搜尋及人物、合輯、公司、電視網、關鍵字和演職員詳細資料的所有相關頁面。透過 TMDb 登入，同步收藏、觀看清單、評分、推薦及電影／電視混合片單；離線變更會持久保存並安全重試。",
    )
}

@Composable
private fun AccountRatingPanel(
    rating: AccountRatingState,
    locale: AppLocale,
    onChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = ratingCopy(locale)
    var draft by remember(rating.value) { mutableStateOf((rating.value ?: 5.0).toFloat()) }
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = CinemaColors.Elevated) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SectionTitle(copy.title)
                Text("${((draft * 2).roundToInt() / 2f)} / 10", color = CinemaColors.Accent)
            }
            Slider(
                value = draft,
                onValueChange = { draft = (it * 2).roundToInt() / 2f },
                valueRange = 0.5f..10f,
                steps = 18,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onChange(draft.toDouble()) }) { Text(copy.save) }
                if (rating.value != null) TextButton(onClick = { onChange(null) }) { Text(copy.remove) }
                if (rating.pending) Text(copy.pending, color = CinemaColors.Muted)
            }
            rating.error?.let { Text(it, color = CinemaColors.Accent, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun DetailClose(label: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClose,
        modifier = modifier.size(48.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.74f)).semantics {
            contentDescription = label
        },
    ) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
}

private fun typeLabel(type: MediaType, copy: UiStrings): String = if (type == MediaType.MOVIE) copy.movies else copy.tvSeries

private data class RatingCopy(val title: String, val save: String, val remove: String, val pending: String)

private fun ratingCopy(locale: AppLocale): RatingCopy = when (locale) {
    AppLocale.ENGLISH -> RatingCopy("Your rating", "Save rating", "Remove rating", "Waiting to sync")
    AppLocale.VIETNAMESE -> RatingCopy("Điểm của bạn", "Lưu điểm", "Xóa điểm", "Đang chờ đồng bộ")
    AppLocale.JAPANESE -> RatingCopy("あなたの評価", "評価を保存", "評価を削除", "同期を待機中")
    AppLocale.KOREAN -> RatingCopy("내 평점", "평점 저장", "평점 삭제", "동기화 대기 중")
    AppLocale.CHINESE_SIMPLIFIED -> RatingCopy("我的评分", "保存评分", "删除评分", "等待同步")
    AppLocale.CHINESE_TRADITIONAL -> RatingCopy("我的評分", "儲存評分", "刪除評分", "等待同步")
}

private fun scopeLabel(scope: SearchScope, copy: UiStrings): String = when (scope) {
    SearchScope.ALL -> copy.all
    SearchScope.MOVIE -> copy.movies
    SearchScope.TV -> copy.tvSeries
}

private fun scopeLabelV2(scope: SearchScopeV2, copy: UiStrings, locale: AppLocale): String = when (scope) {
    SearchScopeV2.ALL -> copy.all
    SearchScopeV2.MOVIE -> copy.movies
    SearchScopeV2.TV -> copy.tvSeries
    SearchScopeV2.PERSON -> entityKindLabel(EntityKind.PERSON, locale)
    SearchScopeV2.COLLECTION -> entityKindLabel(EntityKind.COLLECTION, locale)
    SearchScopeV2.COMPANY -> entityKindLabel(EntityKind.COMPANY, locale)
    SearchScopeV2.KEYWORD -> entityKindLabel(EntityKind.KEYWORD, locale)
}

private fun entityKindLabel(kind: EntityKind, locale: AppLocale): String {
    val labels = when (locale) {
        AppLocale.ENGLISH -> listOf("Movie", "TV", "Person", "Collection", "Company", "Network", "Keyword", "Season", "Episode")
        AppLocale.VIETNAMESE -> listOf("Phim", "Phim bộ", "Con người", "Bộ sưu tập", "Công ty", "Mạng", "Từ khóa", "Mùa", "Tập")
        AppLocale.JAPANESE -> listOf("映画", "テレビ", "人物", "コレクション", "会社", "ネットワーク", "キーワード", "シーズン", "エピソード")
        AppLocale.KOREAN -> listOf("영화", "TV", "인물", "컬렉션", "제작사", "네트워크", "키워드", "시즌", "에피소드")
        AppLocale.CHINESE_SIMPLIFIED -> listOf("电影", "剧集", "人物", "合集", "公司", "电视网", "关键词", "季", "集")
        AppLocale.CHINESE_TRADITIONAL -> listOf("電影", "影集", "人物", "合輯", "公司", "電視網", "關鍵字", "季", "集")
    }
    return labels[kind.ordinal]
}

private fun sortLabel(sort: DiscoverSort, copy: UiStrings): String = when (sort) {
    DiscoverSort.POPULARITY -> copy.popularity
    DiscoverSort.RATING -> copy.topRated
    DiscoverSort.RELEASE_DATE -> copy.releaseDate
}

private data class ProfileCopy(
    val profile: String,
    val tmdbAccount: String,
    val checking: String,
    val signInDescription: String,
    val accountUnavailable: String,
    val signIn: String,
    val finishBrowser: String,
    val deviceCode: String,
    val cancel: String,
    val signOutKeep: String,
    val signOutRemove: String,
    val retry: String,
    val region: String,
    val regionHint: String,
    val deviceRegion: String,
    val save: String,
    val adultContent: String,
    val adultDescription: String,
    val adultAgeConfirmation: String,
    val adultAgeRequired: String,
    val locked: String,
    val pin: String,
    val confirmPin: String,
    val pinInvalid: String,
    val unlock: String,
    val enable: String,
    val lock: String,
    val disable: String,
    val attribution: String,
    val customLists: String,
    val listName: String,
    val listDescription: String,
    val createList: String,
    val deleteList: String,
    val recommendations: String,
    val noRecommendations: String,
    val openList: String,
    val closeList: String,
    val listDetails: String,
    val waitingListSync: String,
    val publicList: String,
    val saveChanges: String,
    val titlesInList: String,
    val emptyListTitles: String,
    val removeTitle: String,
    val addTitles: String,
    val searchTitles: String,
    val addTitle: String,
    val sourceRepositories: String,
    val developmentPlan: String,
)

private fun profileCopy(locale: AppLocale): ProfileCopy = when (locale) {
    AppLocale.ENGLISH -> ProfileCopy(
        "Profile", "TMDb account", "Checking your session…", "Sign in through TMDb in your browser. SmartMovie never sees your password.",
        "TMDb account is temporarily unavailable.",
        "Sign in with TMDb", "Finish approval in your browser; SmartMovie will reconnect automatically.", "Device code", "Cancel",
        "Sign out · keep local", "Sign out · remove data", "Try again", "Content region", "Two-letter country code", "Device region", "Save",
        "Adult content", "Off by default. Confirm you are at least 18 and create a six-digit PIN stored only on this device.",
        "I confirm that I am at least 18 years old.", "Confirm that you are at least 18 years old.",
        "Too many attempts. Try again in five minutes.", "Six-digit PIN", "Confirm PIN", "Enter the same valid six-digit PIN twice or try again.",
        "Unlock", "Enable", "Lock", "Disable",
        "Movie data and images: TMDb. Availability data: JustWatch via TMDb.", "Custom lists", "List name", "Description", "Create list", "Delete",
        "Account recommendations", "No account recommendations yet.",
        "Open", "Close list", "List details", "Waiting for this list to sync before editing.", "Public list", "Save changes",
        "Titles in this list", "This list has no titles yet.", "Remove", "Add movies and TV series", "Search titles", "Add", "Source repositories", "Development roadmap",
    )
    AppLocale.VIETNAMESE -> ProfileCopy(
        "Hồ sơ", "Tài khoản TMDb", "Đang kiểm tra phiên…", "Đăng nhập qua TMDb trong trình duyệt. SmartMovie không bao giờ nhận mật khẩu của bạn.",
        "Tài khoản TMDb tạm thời chưa khả dụng.",
        "Đăng nhập với TMDb", "Hoàn tất phê duyệt trong trình duyệt; SmartMovie sẽ tự kết nối lại.", "Mã thiết bị", "Hủy",
        "Đăng xuất · giữ cục bộ", "Đăng xuất · xóa dữ liệu", "Thử lại", "Khu vực nội dung", "Mã quốc gia hai chữ cái", "Vùng thiết bị", "Lưu",
        "Nội dung 18+", "Mặc định tắt. Xác nhận bạn đủ 18 tuổi và tạo PIN sáu chữ số chỉ lưu trên thiết bị này.",
        "Tôi xác nhận mình đã đủ 18 tuổi.", "Hãy xác nhận bạn đã đủ 18 tuổi.",
        "Sai quá nhiều lần. Hãy thử lại sau năm phút.", "PIN sáu chữ số", "Xác nhận PIN", "Hãy nhập cùng một PIN sáu chữ số hợp lệ hai lần hoặc thử lại.",
        "Mở khóa", "Bật", "Khóa", "Tắt",
        "Dữ liệu và hình ảnh phim: TMDb. Dữ liệu nơi xem: JustWatch qua TMDb.", "Danh sách tùy chỉnh", "Tên danh sách", "Mô tả", "Tạo danh sách", "Xóa",
        "Đề xuất cho tài khoản", "Chưa có đề xuất cho tài khoản.",
        "Mở", "Đóng danh sách", "Chi tiết danh sách", "Đang chờ đồng bộ danh sách trước khi chỉnh sửa.", "Danh sách công khai", "Lưu thay đổi",
        "Nội dung trong danh sách", "Danh sách này chưa có nội dung.", "Gỡ", "Thêm phim và chương trình truyền hình", "Tìm phim hoặc chương trình", "Thêm", "Kho mã nguồn", "Kế hoạch phát triển",
    )
    AppLocale.JAPANESE -> ProfileCopy(
        "プロフィール", "TMDbアカウント", "セッションを確認中…", "ブラウザーでTMDbにログインします。SmartMovieがパスワードを取得することはありません。",
        "TMDbアカウントは一時的に利用できません。",
        "TMDbでログイン", "ブラウザーで承認を完了すると自動的に再接続します。", "デバイスコード", "キャンセル",
        "ログアウト・端末に保持", "ログアウト・データ削除", "再試行", "コンテンツ地域", "2文字の国コード", "デバイスの地域", "保存",
        "成人向けコンテンツ", "初期設定はオフです。18歳以上であることを確認し、端末内だけに保存する6桁PINを作成してください。",
        "18歳以上であることを確認します。", "18歳以上であることを確認してください。",
        "試行回数を超えました。5分後に再試行してください。", "6桁PIN", "PINを確認", "同じ有効な6桁PINを2回入力するか、もう一度お試しください。",
        "ロック解除", "有効にする", "ロック", "無効化",
        "映画データと画像: TMDb。配信情報: TMDb経由のJustWatch。", "カスタムリスト", "リスト名", "説明", "リストを作成", "削除",
        "アカウントへのおすすめ", "おすすめはまだありません。",
        "開く", "リストを閉じる", "リストの詳細", "編集する前にリストの同期を待っています。", "公開リスト", "変更を保存",
        "リスト内の作品", "このリストにはまだ作品がありません。", "削除", "映画とTVシリーズを追加", "作品を検索", "追加", "ソースリポジトリ", "開発ロードマップ",
    )
    AppLocale.KOREAN -> ProfileCopy(
        "프로필", "TMDb 계정", "세션 확인 중…", "브라우저에서 TMDb에 로그인합니다. SmartMovie는 비밀번호를 받지 않습니다.",
        "TMDb 계정을 일시적으로 사용할 수 없습니다.",
        "TMDb로 로그인", "브라우저에서 승인을 완료하면 자동으로 다시 연결됩니다.", "기기 코드", "취소",
        "로그아웃 · 로컬 유지", "로그아웃 · 데이터 삭제", "다시 시도", "콘텐츠 지역", "두 글자 국가 코드", "기기 지역", "저장",
        "성인 콘텐츠", "기본값은 꺼짐입니다. 만 18세 이상임을 확인하고 이 기기에만 저장되는 6자리 PIN을 만드세요.",
        "만 18세 이상임을 확인합니다.", "만 18세 이상임을 확인하세요.",
        "시도 횟수를 초과했습니다. 5분 후 다시 시도하세요.", "6자리 PIN", "PIN 확인", "같은 올바른 6자리 PIN을 두 번 입력하거나 다시 시도하세요.",
        "잠금 해제", "사용", "잠금", "사용 안 함",
        "영화 데이터 및 이미지: TMDb. 시청 가능 정보: TMDb를 통한 JustWatch.", "사용자 목록", "목록 이름", "설명", "목록 만들기", "삭제",
        "계정 추천", "아직 계정 추천이 없습니다.",
        "열기", "목록 닫기", "목록 세부 정보", "편집하기 전에 목록 동기화를 기다리는 중입니다.", "공개 목록", "변경 사항 저장",
        "목록의 콘텐츠", "이 목록에는 아직 콘텐츠가 없습니다.", "제거", "영화 및 TV 시리즈 추가", "콘텐츠 검색", "추가", "소스 저장소", "개발 로드맵",
    )
    AppLocale.CHINESE_SIMPLIFIED -> ProfileCopy(
        "个人资料", "TMDb 账户", "正在检查会话…", "请在浏览器中登录 TMDb。SmartMovie 不会获取您的密码。",
        "TMDb 账户暂时不可用。",
        "使用 TMDb 登录", "在浏览器中完成授权后，SmartMovie 会自动重新连接。", "设备代码", "取消",
        "退出 · 保留本地", "退出 · 删除数据", "重试", "内容地区", "两位国家代码", "设备地区", "保存",
        "成人内容", "默认关闭。请确认您已年满18岁，并设置仅存储在本设备上的六位 PIN。",
        "我确认自己已年满18岁。", "请确认您已年满18岁。",
        "尝试次数过多，请五分钟后重试。", "六位 PIN", "确认 PIN", "请两次输入相同且有效的六位 PIN，或重试。",
        "解锁", "启用", "锁定", "停用",
        "电影数据和图片：TMDb。可观看信息：通过 TMDb 提供的 JustWatch。", "自定义列表", "列表名称", "说明", "创建列表", "删除",
        "账户推荐", "暂无账户推荐。",
        "打开", "关闭列表", "列表详情", "正在等待列表同步后再编辑。", "公开列表", "保存更改",
        "列表中的内容", "此列表中还没有内容。", "移除", "添加电影和电视剧", "搜索内容", "添加", "源代码仓库", "开发路线图",
    )
    AppLocale.CHINESE_TRADITIONAL -> ProfileCopy(
        "個人資料", "TMDb 帳戶", "正在檢查工作階段…", "請在瀏覽器中登入 TMDb。SmartMovie 不會取得您的密碼。",
        "TMDb 帳戶暫時無法使用。",
        "使用 TMDb 登入", "在瀏覽器完成授權後，SmartMovie 會自動重新連線。", "裝置代碼", "取消",
        "登出 · 保留本機", "登出 · 刪除資料", "重試", "內容地區", "兩位國家代碼", "裝置地區", "儲存",
        "成人內容", "預設關閉。請確認您已年滿18歲，並設定只儲存在本裝置的六位 PIN。",
        "我確認自己已年滿18歲。", "請確認您已年滿18歲。",
        "嘗試次數過多，請五分鐘後再試。", "六位 PIN", "確認 PIN", "請輸入兩次相同且有效的六位 PIN，或重試。",
        "解鎖", "啟用", "鎖定", "停用",
        "電影資料與圖片：TMDb。可觀看資訊：由 TMDb 提供的 JustWatch。", "自訂清單", "清單名稱", "說明", "建立清單", "刪除",
        "帳戶推薦", "目前沒有帳戶推薦。",
        "開啟", "關閉清單", "清單詳情", "正在等待清單同步後再編輯。", "公開清單", "儲存變更",
        "清單中的內容", "此清單中還沒有內容。", "移除", "加入電影與電視影集", "搜尋內容", "加入", "原始碼儲存庫", "開發藍圖",
    )
}
