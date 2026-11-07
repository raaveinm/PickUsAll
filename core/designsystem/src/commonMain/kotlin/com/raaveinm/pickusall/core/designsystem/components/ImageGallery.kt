package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.pickusall.core.designsystem.animated.UnspecifiedProgressIndicator
import com.raaveinm.pickusall.core.designsystem.obj.MediaGalleryItem
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.utils.ImageUtility
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.media_open_fullscreen
import pickusall.core.designsystem.generated.resources.media_play
import pickusall.core.designsystem.generated.resources.media_stop
import pickusall.core.designsystem.generated.resources.media_unavailable
import kotlin.time.Duration.Companion.milliseconds

//
// Created by Kirill "Raaveinm" on 11/07/2026.
//

private const val GALLERY_ASPECT_RATIO: Float = 16f / 9f
private const val GALLERY_SIDE_FRACTION: Float = 0.14f

@Composable
fun ImageGallery(
    items: List<MediaGalleryItem>,
    modifier: Modifier = Modifier,
    verticalScrollEnabled: Boolean = false,
    onItemClicked: (Int) -> Unit = { _ -> },
) {
    if (items.isEmpty()) return

    val state: PagerState = rememberPagerState { items.size }
    val flingBehavior: TargetedFlingBehavior = PagerDefaults.flingBehavior(state = state)
    val hasNeighbors: Boolean = items.size > 1
    var playbackResetKey: Int by remember { mutableIntStateOf(0) }
    val openItem: (Int) -> Unit = { index ->
        playbackResetKey += 1
        onItemClicked(index)
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().scrollable(
            state = state,
            orientation = Orientation.Vertical,
            enabled = verticalScrollEnabled && hasNeighbors,
            reverseDirection = true,
            flingBehavior = flingBehavior,
        ),
    ) {
        val sidePadding: Dp = if (hasNeighbors) maxWidth * GALLERY_SIDE_FRACTION else 0.dp
        HorizontalPager(
            state = state,
            modifier = Modifier.fillMaxWidth().clipToBounds(),
            contentPadding = PaddingValues(horizontal = sidePadding),
            pageSpacing = Dimensions.small.coerceAtMost(sidePadding / 2f),
            flingBehavior = flingBehavior,
            userScrollEnabled = hasNeighbors,
        ) { page ->
            val pageModifier: Modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(GALLERY_ASPECT_RATIO)
                .clip(Shapes.roundedSmoother)
            when (val item: MediaGalleryItem = items[page]) {
                is MediaGalleryItem.Image -> ImageUtility(
                    imageUrl = item.url,
                    modifier = pageModifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { openItem(page) },
                    contentScale = ContentScale.Fit,
                )
                is MediaGalleryItem.Movie -> GalleryMoviePage(
                    movie = item,
                    isCentered = page == state.currentPage,
                    playbackResetKey = playbackResetKey,
                    onOpen = { openItem(page) },
                    modifier = pageModifier,
                )
            }
        }
    }
}

///////////////////////////////////////////////
/// Trailer playback
///////////////////////////////////////////////

@Composable
private fun GalleryMoviePage(
    movie: MediaGalleryItem.Movie,
    isCentered: Boolean,
    playbackResetKey: Int,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sourceIndex: Int? by remember(movie, isCentered, playbackResetKey) { mutableStateOf(null) }
    var playbackFailed: Boolean by remember(movie, isCentered, playbackResetKey) { mutableStateOf(false) }
    val activeSource: Int? = sourceIndex

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onOpen,
        ),
        contentAlignment = Alignment.Center,
    ) {
        movie.thumbnailUrl?.let { url ->
            ImageUtility(
                imageUrl = url,
                contentDescription = movie.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Fit,
            )
        }

        if (activeSource != null) {
            key(activeSource) {
                GalleryMoviePlayback(
                    url = movie.videoUrls[activeSource],
                    onClose = { sourceIndex = null },
                    onOpen = onOpen,
                    onSourceFailed = {
                        sourceIndex = (activeSource + 1).takeIf { it < movie.videoUrls.size }
                        playbackFailed = sourceIndex == null
                    },
                    modifier = Modifier.matchParentSize(),
                )
            }
        } else if (isCentered && movie.videoUrls.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = {
                        playbackFailed = false
                        sourceIndex = 0
                    },
                    colors = IconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .64f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, stringResource(Res.string.media_play))
                }
                if (playbackFailed) {
                    Text(
                        text = stringResource(Res.string.media_unavailable),
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            .padding(Dimensions.small),
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryMoviePlayback(
    url: String,
    onClose: () -> Unit,
    onOpen: () -> Unit,
    onSourceFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val player: VideoPlayerState = rememberVideoPlayerState()
    val scope: CoroutineScope = rememberCoroutineScope()
    DisposableEffect(player) {
        player.onPlaybackEnded = { scope.launch { onClose() } }
        onDispose { player.onPlaybackEnded = null }
    }
    LaunchedEffect(player, url) {
        try { player.openUri(url)
        } catch (exception: CancellationException) { throw exception
        } catch (_: Exception) { onSourceFailed(); return@LaunchedEffect }
        snapshotFlow { player.error != null }.first { it }
        onSourceFailed()
    }

    Box(modifier = modifier) {
        var isHidden: Boolean by remember { mutableStateOf(false) }
        LaunchedEffect(isHidden) {
            if (!isHidden) {
                delay(2100.milliseconds)
                isHidden = true
            }
        }
        VideoPlayerSurface(
            playerState = player,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit,
        )
        if (!player.hasMedia || player.isLoading) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                UnspecifiedProgressIndicator(selfSize = 24.dp)
            }
        }
        Box(Modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null) { isHidden = !isHidden }.zIndex(1.2f))
        AnimatedVisibility(
            visible = !isHidden,
            enter = fadeIn(animationSpec = tween(durationMillis = 400)),
            exit = fadeOut(animationSpec = tween(durationMillis = 400)),
            modifier = Modifier.align(Alignment.BottomEnd).padding(Dimensions.small).zIndex(1.3f)
        ) {
            val buttonColors: IconButtonColors = IconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .64f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface,
            )
            Row {
                FilledIconButton(onClick = onOpen, colors = buttonColors) {
                    Icon(Icons.Default.Fullscreen, stringResource(Res.string.media_open_fullscreen))
                }
                FilledIconButton(onClick = onClose, colors = buttonColors) {
                    Icon(Icons.Default.Stop, stringResource(Res.string.media_stop))
                }
            }
        }
    }
}
