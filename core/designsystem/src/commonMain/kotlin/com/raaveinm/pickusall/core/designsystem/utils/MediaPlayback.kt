package com.raaveinm.pickusall.core.designsystem.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.animated.UnspecifiedProgressIndicator
import com.raaveinm.pickusall.core.designsystem.obj.MediaGalleryItem
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.media_close
import pickusall.core.designsystem.generated.resources.media_image
import pickusall.core.designsystem.generated.resources.media_next
import pickusall.core.designsystem.generated.resources.media_pause
import pickusall.core.designsystem.generated.resources.media_play
import pickusall.core.designsystem.generated.resources.media_previous
import pickusall.core.designsystem.generated.resources.media_retry
import pickusall.core.designsystem.generated.resources.media_unavailable

//
// Created by Kirill "Raaveinm" on 11/07/2026.
//

private const val MEDIA_CONTROLS_HIDE_DELAY_MS: Long = 3200L
private const val MEDIA_CONTROLS_FADE_DURATION_MS: Int = 400

/**
 * Displays one item at its original aspect ratio. Only the active page creates a video player;
 * adjacent pages retain their posters. Removing the page releases its native player automatically.
 */
@Composable
fun MediaPlayback(
    media: MediaGalleryItem,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    positionLabel: String? = null,
    onActionNext: (() -> Unit)? = null,
    onActionPrevious: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    var controlsVisible: Boolean by remember(media, isActive) { mutableStateOf(true) }
    var interactionCount: Int by remember(media) { mutableIntStateOf(0) }
    val showControls: () -> Unit = {
        controlsVisible = true
        interactionCount += 1
    }
    LaunchedEffect(media, isActive, controlsVisible, interactionCount) {
        if (isActive && controlsVisible) {
            delay(MEDIA_CONTROLS_HIDE_DELAY_MS)
            controlsVisible = false
        }
    }
    val buttonColors: IconButtonColors = IconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .64f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        disabledContentColor = MaterialTheme.colorScheme.onSurface,
    )

    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { controlsVisible = !controlsVisible; interactionCount += 1 },
    ) {
        when (media) {
            is MediaGalleryItem.Image -> ImageUtility(
                imageUrl = media.url,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            is MediaGalleryItem.Movie -> key(media) {
                MoviePlayback(
                    movie = media,
                    isActive = isActive,
                    controlsVisible = controlsVisible,
                    buttonColors = buttonColors,
                    onInteraction = showControls,
                )
            }
            else -> Text(
                text = stringResource(Res.string.media_unavailable),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(MEDIA_CONTROLS_FADE_DURATION_MS)),
            exit = fadeOut(tween(MEDIA_CONTROLS_FADE_DURATION_MS)),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = .64f))
                    .padding(Dimensions.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.small),
            ) {
                Text(
                    text = (media as? MediaGalleryItem.Movie)?.title?.takeIf { it.isNotBlank() }
                        ?: stringResource(Res.string.media_image),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                positionLabel?.let { Text(text = it) }
                if (onDismiss != null) {
                    FilledIconButton(onClick = onDismiss, colors = buttonColors) {
                        Icon(Icons.Default.Close, stringResource(Res.string.media_close))
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = controlsVisible && onActionPrevious != null,
            enter = fadeIn(tween(MEDIA_CONTROLS_FADE_DURATION_MS)),
            exit = fadeOut(tween(MEDIA_CONTROLS_FADE_DURATION_MS)),
            modifier = Modifier.align(Alignment.CenterStart).padding(Dimensions.small),
        ) {
            FilledIconButton(onClick = { onActionPrevious?.invoke() }, colors = buttonColors) {
                Icon(Icons.Default.ChevronLeft, stringResource(Res.string.media_previous))
            }
        }
        AnimatedVisibility(
            visible = controlsVisible && onActionNext != null,
            enter = fadeIn(tween(MEDIA_CONTROLS_FADE_DURATION_MS)),
            exit = fadeOut(tween(MEDIA_CONTROLS_FADE_DURATION_MS)),
            modifier = Modifier.align(Alignment.CenterEnd).padding(Dimensions.small),
        ) {
            FilledIconButton(onClick = { onActionNext?.invoke() }, colors = buttonColors) {
                Icon(Icons.Default.ChevronRight, stringResource(Res.string.media_next))
            }
        }
    }
}

///////////////////////////////////////////////
/// Movie playback
///////////////////////////////////////////////

@Composable
private fun MoviePlayback(
    movie: MediaGalleryItem.Movie,
    isActive: Boolean,
    controlsVisible: Boolean,
    buttonColors: IconButtonColors,
    onInteraction: () -> Unit,
) {
    var sourceIndex: Int by remember(isActive) { mutableIntStateOf(0) }
    var attempt: Int by remember(isActive) { mutableIntStateOf(0) }
    var failed: Boolean by remember(isActive) { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        movie.thumbnailUrl?.let { url ->
            ImageUtility(
                imageUrl = url,
                contentDescription = movie.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Fit,
            )
        }
        if (isActive && !failed && movie.videoUrls.isNotEmpty()) {
            key(sourceIndex, attempt) {
                MoviePlayer(
                    url = movie.videoUrls[sourceIndex],
                    controlsVisible = controlsVisible,
                    buttonColors = buttonColors,
                    onInteraction = onInteraction,
                    onSourceFailed = {
                        if (sourceIndex < movie.videoUrls.lastIndex) sourceIndex += 1
                        else failed = true
                    },
                )
            }
        }
        if (isActive && (failed || movie.videoUrls.isEmpty())) {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = .85f))
                    .padding(Dimensions.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.media_unavailable))
                if (movie.videoUrls.isNotEmpty()) {
                    Button(onClick = {
                        sourceIndex = 0
                        attempt += 1
                        failed = false
                        onInteraction()
                    }) { Text(stringResource(Res.string.media_retry)) }
                }
            }
        }
    }
}

@Composable
private fun MoviePlayer(
    url: String,
    controlsVisible: Boolean,
    buttonColors: IconButtonColors,
    onInteraction: () -> Unit,
    onSourceFailed: () -> Unit,
) {
    val player: VideoPlayerState = rememberVideoPlayerState()
    LaunchedEffect(player, url) {
        player.volume = .85f
        player.loop = true
        try {
            player.openUri(url)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            onSourceFailed()
            return@LaunchedEffect
        }
        snapshotFlow { player.error != null }.first { it }
        onSourceFailed()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(
            if (player.hasMedia) MaterialTheme.colorScheme.surface else Color.Transparent,
        ),
        contentAlignment = Alignment.Center,
    ) {
        VideoPlayerSurface(
            playerState = player,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit,
        )
        if (!player.hasMedia || player.isLoading) {
            UnspecifiedProgressIndicator(selfSize = 32.dp)
        }
        AnimatedVisibility(
            visible = controlsVisible && player.hasMedia && !player.isLoading,
            enter = fadeIn(tween(MEDIA_CONTROLS_FADE_DURATION_MS)),
            exit = fadeOut(tween(MEDIA_CONTROLS_FADE_DURATION_MS)),
        ) {
            FilledIconButton(
                onClick = {
                    if (player.isPlaying) player.pause() else player.play()
                    onInteraction()
                },
                colors = buttonColors,
            ) {
                Icon(
                    imageVector = if (player.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        if (player.isPlaying) Res.string.media_pause else Res.string.media_play,
                    ),
                )
            }
        }
    }
}
