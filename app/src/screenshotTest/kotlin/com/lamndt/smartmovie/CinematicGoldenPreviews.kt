package com.lamndt.smartmovie

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.lamndt.smartmovie.data.ImageUrlFactory
import com.lamndt.smartmovie.designsystem.CinemaBackground
import com.lamndt.smartmovie.designsystem.SmartMovieTheme
import com.lamndt.smartmovie.feature.home.HomeScreen
import com.lamndt.smartmovie.feature.home.HomeUiState
import com.lamndt.smartmovie.model.HomeFeed
import com.lamndt.smartmovie.model.HomeSection
import com.lamndt.smartmovie.model.ImageConfiguration
import com.lamndt.smartmovie.model.Loadable
import com.lamndt.smartmovie.model.MediaType
import com.lamndt.smartmovie.model.TitleSummary

@PreviewTest
@Preview(name = "phone_compact", widthDp = 390, heightDp = 844, locale = "en")
@Composable
fun PhoneHomeGolden() = GoldenTheme {
    HomeScreen(HomeUiState(feed = Loadable.Loaded(goldenFeed)), goldenImages, {}, {}, {}, {})
}

@PreviewTest
@Preview(name = "tablet_expanded", widthDp = 1280, heightDp = 800, locale = "en")
@Composable
fun TabletHomeGolden() = GoldenTheme {
    HomeScreen(HomeUiState(feed = Loadable.Loaded(goldenFeed)), goldenImages, {}, {}, {}, {})
}

@PreviewTest
@Preview(name = "tv_1080p", widthDp = 1920, heightDp = 1080, locale = "en")
@Composable
fun TvHomeGolden() = GoldenTheme {
    TvHomeContent(goldenFeed, goldenImages, MediaType.MOVIE, {}, {})
}

@Composable
private fun GoldenTheme(content: @Composable () -> Unit) {
    SmartMovieTheme { CinemaBackground(content = content) }
}

private val goldenImages = ImageUrlFactory { ImageConfiguration.Fallback }
private val hero = TitleSummary(
    id = 438631,
    mediaType = MediaType.MOVIE,
    title = "Dune: Part Two",
    originalTitle = "Dune: Part Two",
    overview = "Paul Atreides unites with Chani and the Fremen while seeking a path through love, loyalty and destiny.",
    releaseDate = "2024-02-27",
    voteAverage = 8.3,
)
private val companionTitles = listOf(
    hero,
    TitleSummary(550, MediaType.MOVIE, "Fight Club", "Fight Club", "An insomniac discovers an underground world.", releaseDate = "1999-10-15", voteAverage = 8.4),
    TitleSummary(1399, MediaType.TV, "Game of Thrones", "Game of Thrones", "Great houses compete for a throne.", releaseDate = "2011-04-17", voteAverage = 8.5),
    TitleSummary(157336, MediaType.MOVIE, "Interstellar", "Interstellar", "Explorers travel through a wormhole.", releaseDate = "2014-11-05", voteAverage = 8.5),
)
private val goldenFeed = HomeFeed(
    mediaType = MediaType.MOVIE,
    hero = hero,
    sections = listOf(
        HomeSection("trending", "Trending", companionTitles),
        HomeSection("popular", "Popular", companionTitles.reversed()),
    ),
)
