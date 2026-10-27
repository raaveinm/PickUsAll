package com.raaveinm.pickusall.core.designsystem.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState

//
// Created by Kirill "Raaveinm" on 10/26/26.
//

@Composable
fun AnimatedBackground(
    videoUrl: String,
    posterUrl: String?,
    modifier: Modifier = Modifier
) {
    val playerState = rememberVideoPlayerState()

    LaunchedEffect(videoUrl) {
        playerState.volume = 0f
        playerState.loop = true
        playerState.openUri(videoUrl)
    }

    Box(modifier = modifier) {
        if (posterUrl != null) {
            ImageUtility(
                modifier = Modifier.matchParentSize(),
                imageUrl = posterUrl,
                blurPadding = 0.dp,
                errorColor = MaterialTheme.colorScheme.surface
            )
        }
        VideoPlayerSurface(
            playerState = playerState,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
