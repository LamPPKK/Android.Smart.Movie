package com.lamndt.smartmovie.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lamndt.smartmovie.data.ImageUrlFactory
import com.lamndt.smartmovie.designsystem.CinemaColors
import com.lamndt.smartmovie.designsystem.LoadingMessage
import com.lamndt.smartmovie.designsystem.MediaTypeSelector
import com.lamndt.smartmovie.designsystem.PosterCard
import com.lamndt.smartmovie.designsystem.R
import com.lamndt.smartmovie.designsystem.RatingBadge
import com.lamndt.smartmovie.designsystem.RemoteArtwork
import com.lamndt.smartmovie.designsystem.SectionTitle
import com.lamndt.smartmovie.designsystem.StateMessage
import com.lamndt.smartmovie.designsystem.isWindowWidthAtLeast
import com.lamndt.smartmovie.model.CatalogRepository
import com.lamndt.smartmovie.model.HomeFeed
import com.lamndt.smartmovie.model.ImageKind
import com.lamndt.smartmovie.model.Loadable
import com.lamndt.smartmovie.model.MediaType
import com.lamndt.smartmovie.model.TitleSummary

@Composable
fun HomeRoute(
    catalog: CatalogRepository,
    images: ImageUrlFactory,
    language: String,
    onTitleClick: (TitleSummary) -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(catalog, language)),
) {
    val state by homeViewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state, images, homeViewModel::selectMediaType, homeViewModel::refresh, onTitleClick, modifier)
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    images: ImageUrlFactory,
    onMediaType: (MediaType) -> Unit,
    onRetry: () -> Unit,
    onTitleClick: (TitleSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = if (isWindowWidthAtLeast(840)) 40.dp else 20.dp
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = horizontalPadding, vertical = 18.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(14.dp))
                MediaTypeSelector(state.mediaType, onMediaType, Modifier.width(330.dp).fillMaxWidth())
            }
        }
        when (val feed = state.feed) {
            Loadable.Idle, Loadable.Loading -> item { LoadingMessage(Modifier.height(480.dp)) }
            is Loadable.Failed -> item {
                StateMessage(
                    stringResource(R.string.unable_home),
                    message = feed.message.ifBlank { stringResource(R.string.check_connection) },
                    retry = onRetry,
                )
            }
            is Loadable.Loaded -> {
                homeFeed(feed.value, images, onTitleClick, horizontalPadding)
                val trending = (state.trending as? Loadable.Loaded<List<TitleSummary>>)?.value.orEmpty()
                if (trending.isNotEmpty()) item {
                    SectionTitle(stringResource(R.string.trending), Modifier.padding(horizontal = horizontalPadding))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = horizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(trending, key = { it.libraryKey }) { title ->
                            PosterCard(title, images.url(title.posterPath, ImageKind.POSTER), { onTitleClick(title) })
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.homeFeed(
    feed: HomeFeed,
    images: ImageUrlFactory,
    onTitleClick: (TitleSummary) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
) {
    feed.hero?.let { hero ->
        item { HeroCard(hero, images, onTitleClick, Modifier.padding(horizontal = horizontalPadding)) }
    }
    items(feed.sections, key = { it.id }) { section ->
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionTitle(section.title, Modifier.padding(horizontal = horizontalPadding))
            LazyRow(
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(section.items, key = { it.libraryKey }) { title ->
                    PosterCard(title, images.url(title.posterPath, ImageKind.POSTER), { onTitleClick(title) })
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    title: TitleSummary,
    images: ImageUrlFactory,
    onClick: (TitleSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isWindowWidthAtLeast(840)) 520.dp else 460.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(CinemaColors.Elevated),
    ) {
        RemoteArtwork(images.url(title.backdropPath, ImageKind.BACKDROP), title.displayTitle, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f), Color.Black.copy(alpha = 0.96f))),
            ),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(if (title.mediaType == MediaType.MOVIE) R.string.featured_film else R.string.featured_series),
                color = CinemaColors.Accent,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
            )
            Text(title.displayTitle, style = MaterialTheme.typography.displayMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                RatingBadge(title.voteAverage)
                title.releaseYear?.let { Text(it, color = CinemaColors.Muted) }
            }
            Text(title.overview, color = CinemaColors.Muted, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Button(
                onClick = { onClick(title) },
                colors = ButtonDefaults.buttonColors(containerColor = CinemaColors.Accent),
                modifier = Modifier.height(52.dp),
            ) {
                Text(stringResource(R.string.view_details))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
