package com.raaveinm.picasso.ui.media

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.raaveinm.pickusall.core.designsystem.obj.MediaGalleryItem
import com.raaveinm.pickusall.core.designsystem.utils.MediaPlayback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.dismiss
import pickusall.shared.generated.resources.media_empty
import pickusall.shared.generated.resources.media_position

//
// Created on 11/07/2026.
// Copyright (c) 2026 Retrograde Mercury. I'm drinking coffee.
//

/** Opens the selected item and keeps navigation within this presentation's collection. */
@Composable
internal fun MediaLibraryViewer(
    items: List<MediaGalleryItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedId: Int = 0,
) {
    val focusRequester: FocusRequester = remember { FocusRequester() }
    val scope: CoroutineScope = rememberCoroutineScope()
    val state: PagerState = rememberPagerState(
        initialPage = initialSelectedId.coerceIn(0, items.lastIndex.coerceAtLeast(0)),
        pageCount = { items.size },
    )
    val navigate: (Int) -> Unit = { index ->
        if (index in items.indices) scope.launch { state.animateScrollToPage(index) }
    }

    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    Box(
        modifier = modifier.fillMaxSize()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.Escape -> { onDismiss(); true }
                    Key.DirectionLeft -> { navigate(state.targetPage - 1); true }
                    Key.DirectionRight -> { navigate(state.targetPage + 1); true }
                    else -> false
                }
            }
            .focusable(),
    ) {
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(Res.string.media_empty))
                Button(onClick = onDismiss) { Text(stringResource(Res.string.dismiss)) }
            }
        } else {
            HorizontalPager(
                state = state,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                MediaPlayback(
                    media = items[page],
                    isActive = page == state.currentPage,
                    positionLabel = stringResource(Res.string.media_position, page + 1, items.size),
                    onActionNext = if (page < items.lastIndex) ({ navigate(page + 1) }) else null,
                    onActionPrevious = if (page > 0) ({ navigate(page - 1) }) else null,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}
