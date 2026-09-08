package com.lamndt.smartmovie

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lamndt.smartmovie.data.ImageUrlFactory
import com.lamndt.smartmovie.designsystem.SmartMovieTheme
import com.lamndt.smartmovie.feature.home.HomeScreen
import com.lamndt.smartmovie.feature.home.HomeUiState
import com.lamndt.smartmovie.model.HomeFeed
import com.lamndt.smartmovie.model.HomeSection
import com.lamndt.smartmovie.model.ImageConfiguration
import com.lamndt.smartmovie.model.Loadable
import com.lamndt.smartmovie.model.MediaType
import com.lamndt.smartmovie.model.TitleSummary
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule val compose = createComposeRule()
    private val images = ImageUrlFactory { ImageConfiguration.Fallback }
    private val dune = TitleSummary(42, MediaType.MOVIE, "Dune", "Dune", "A deterministic overview.", voteAverage = 8.4)

    @Test
    fun loadedStateShowsHeroShelfAndNavigatesToDetail() {
        var clicked = false
        compose.setContent {
            SmartMovieTheme {
                HomeScreen(
                    HomeUiState(feed = Loadable.Loaded(HomeFeed(MediaType.MOVIE, dune, listOf(HomeSection("popular", "Popular", listOf(dune)))))),
                    images, {}, {}, {}, { clicked = true },
                )
            }
        }

        compose.onNodeWithText("SmartMovie").assertIsDisplayed()
        compose.onNodeWithText("Popular").assertIsDisplayed()
        compose.onNodeWithText("View details").performClick()
        compose.runOnIdle { assertTrue(clicked) }
    }

    @Test
    fun errorStateShowsRetryAction() {
        var retried = false
        compose.setContent {
            SmartMovieTheme {
                HomeScreen(HomeUiState(feed = Loadable.Failed("Offline")), images, {}, {}, { retried = true }, {})
            }
        }

        compose.onNodeWithText("Unable to load Home").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        compose.runOnIdle { assertTrue(retried) }
    }
}
