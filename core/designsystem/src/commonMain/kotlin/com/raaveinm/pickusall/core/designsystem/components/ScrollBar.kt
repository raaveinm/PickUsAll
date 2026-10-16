package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollIndicatorState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

//
// Created by Kirill "Raaveinm" on 10/16/2026.
//

private val SCROLL_BAR_THICKNESS: Dp = 4.dp
private val SCROLL_BAR_TOUCH_TARGET: Dp = 24.dp
private val SCROLL_BAR_MIN_THUMB_LENGTH: Dp = 24.dp
private const val SCROLL_BAR_FADE_IN_MS: Int = 150
private const val SCROLL_BAR_FADE_OUT_MS: Int = 400
private const val SCROLL_BAR_HIDE_DELAY_MS: Long = 800L
private const val SCROLL_BAR_GRABBABLE_ALPHA: Float = .05f

/**
 * Scroll indicator with a draggable thumb, one implementation for every scrollable container.
 *
 * It only needs a [ScrollableState], so the same call works for `LazyColumn`/`LazyRow`
 * (`LazyListState`), `LazyVerticalGrid` (`LazyGridState`), `LazyVerticalStaggeredGrid`
 * (`LazyStaggeredGridState`) and plain `verticalScroll` (`ScrollState`). Position and size come from
 * [ScrollableState.scrollIndicatorState]; for lazy layouts the content size is an estimate, so the
 * thumb may shift slightly while items of different heights are measured.
 *
 * The bar draws over the content: put it and the list into one [Box] and align it to an edge.
 *
 * ```kotlin
 * val listState: LazyListState = rememberLazyListState()
 * Box(Modifier.fillMaxSize()) {
 *     LazyColumn(state = listState) { /* items */ }
 *     ScrollBar(
 *         modifier = Modifier.align(Alignment.CenterEnd),
 *         state = listState,
 *     )
 * }
 * ```
 *
 * **Dragging.** The interaction surface is a strip [touchTargetThickness] wide (the visible thumb
 * is only [thickness] wide and sits in the middle of it). Dragging inside the thumb moves the
 * content 1:1 with the finger/cursor; dragging from anywhere else on the strip first centres the
 * thumb under the pointer. The strip takes part in hit-testing only while the bar is visible and
 * there is something to scroll, so a faded-out bar never steals touches from the list underneath.
 *
 * @param state the very same state object that is passed to the list/grid
 * @param orientation must match the scroll direction of the container
 * @param reverseLayout `true` when the container uses `reverseLayout = true` (e.g. chat history):
 * the indicator's offset is always measured from the visual start, so the thumb has to be mirrored
 * @param autoHide fade the bar out when the container is idle; `false` keeps it permanently visible
 * @param interactive `false` makes the bar a passive indicator (no drag, strip is [thickness] wide)
 * @param touchTargetThickness cross-axis size of the draggable strip
 */
@Composable
fun ScrollBar(
    modifier: Modifier = Modifier,
    state: ScrollableState,
    orientation: Orientation = Orientation.Vertical,
    reverseLayout: Boolean = false,
    autoHide: Boolean = true,
    interactive: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = .64f),
    thickness: Dp = SCROLL_BAR_THICKNESS,
    touchTargetThickness: Dp = SCROLL_BAR_TOUCH_TARGET,
    minThumbLength: Dp = SCROLL_BAR_MIN_THUMB_LENGTH,
) {
    val isVertical: Boolean = orientation == Orientation.Vertical
    val scope = rememberCoroutineScope()
    val visibility: Animatable<Float, *> = remember { Animatable(if (autoHide) 0f else 1f) }
    var isDragging: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(state, autoHide) {
        if (!autoHide) {
            visibility.snapTo(1f)
            return@LaunchedEffect
        }
        snapshotFlow { state.isScrollInProgress || isDragging }.collectLatest { isActive: Boolean ->
            if (isActive) {
                visibility.animateTo(1f, tween(SCROLL_BAR_FADE_IN_MS))
            } else {
                delay(SCROLL_BAR_HIDE_DELAY_MS.milliseconds)
                visibility.animateTo(0f, tween(SCROLL_BAR_FADE_OUT_MS))
            }
        }
    }

    val isGrabbable: Boolean by remember(interactive) {
        derivedStateOf {
            interactive
                    && (state.canScrollForward || state.canScrollBackward)
                    && (isDragging || visibility.value > SCROLL_BAR_GRABBABLE_ALPHA)
        }
    }

    val stripThickness: Dp = if (interactive) maxOf(touchTargetThickness, thickness) else thickness

    Spacer(
        modifier = modifier
            .then(
                if (isVertical) Modifier.fillMaxHeight().width(stripThickness)
                else Modifier.fillMaxWidth().height(stripThickness)
            )
            .then(
                if (!isGrabbable) Modifier
                else Modifier.pointerInput(state, isVertical, reverseLayout, minThumbLength) {
                    val trackLength: () -> Float = {
                        (if (isVertical) size.height else size.width).toFloat()
                    }
                    var grabOffset = 0f

                    detectDragGestures(
                        onDragStart = { position: Offset ->
                            isDragging = true
                            scope.launch { state.stopScroll() }

                            val pointer: Float = if (isVertical) position.y else position.x
                            val thumb: ThumbMetrics? = state.scrollIndicatorState
                                ?.thumbMetrics(trackLength(), minThumbLength.toPx(), reverseLayout)
                            grabOffset = when {
                                thumb == null -> 0f
                                pointer in thumb.start..(thumb.start + thumb.length) -> pointer - thumb.start
                                else -> thumb.length / 2f
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ ->
                            change.consume()
                            val indicator: ScrollIndicatorState =
                                state.scrollIndicatorState ?: return@detectDragGestures
                            val thumb: ThumbMetrics =
                                indicator.thumbMetrics(trackLength(), minThumbLength.toPx(), reverseLayout)
                                    ?: return@detectDragGestures
                            val freeTrack: Float = thumb.trackLength - thumb.length
                            if (freeTrack <= 0f) return@detectDragGestures

                            val pointer: Float = if (isVertical) change.position.y else change.position.x
                            val progress: Float = ((pointer - grabOffset) / freeTrack).coerceIn(0f, 1f)
                            val rawProgress: Float = if (reverseLayout) 1f - progress else progress
                            val targetOffset: Float = rawProgress * thumb.scrollRange
                            state.dispatchRawDelta(targetOffset - indicator.scrollOffset.toFloat())
                        },
                    )
                }
            )
            .drawBehind {
                val indicator: ScrollIndicatorState = state.scrollIndicatorState ?: return@drawBehind
                if (visibility.value <= 0f) return@drawBehind
                val trackLength: Float = if (isVertical) size.height else size.width
                val thumb: ThumbMetrics = indicator
                    .thumbMetrics(trackLength, minThumbLength.toPx(), reverseLayout)
                    ?: return@drawBehind
                val crossSize: Float = if (isVertical) size.width else size.height
                val crossStart: Float = (crossSize - thickness.toPx()) / 2f

                drawRoundRect(
                    color = color,
                    topLeft = if (isVertical) Offset(crossStart, thumb.start) else Offset(thumb.start, crossStart),
                    size = if (isVertical) Size(thickness.toPx(), thumb.length) else Size(thumb.length, thickness.toPx()),
                    cornerRadius = CornerRadius(thickness.toPx() / 2f),
                    alpha = visibility.value,
                )
            },
    )
}

///////////////////////////////////////////////
/// Thumb geometry
///////////////////////////////////////////////

/**
 * Where the thumb sits on the track, in pixels along the scroll axis.
 *
 * @param length thumb length
 * @param start thumb offset from the track start (already mirrored for `reverseLayout`)
 * @param scrollRange how far the content can scroll (`contentSize - viewportSize`)
 * @param trackLength length of the strip the thumb travels on
 */
private class ThumbMetrics(
    val length: Float,
    val start: Float,
    val scrollRange: Float,
    val trackLength: Float,
)

// Shared by drawing and dragging so the thumb you see is the thumb you grab.
// null = nothing to show: sizes not measured yet (Int.MAX_VALUE) or the content fits the viewport.
private fun ScrollIndicatorState.thumbMetrics(
    trackLength: Float,
    minThumbLength: Float,
    reverseLayout: Boolean,
): ThumbMetrics? {
    val viewport: Int = viewportSize
    val content: Int = contentSize
    val offset: Int = scrollOffset
    if (viewport == Int.MAX_VALUE || content == Int.MAX_VALUE || offset == Int.MAX_VALUE) return null
    if (content <= viewport || trackLength <= 0f) return null

    val length: Float = (trackLength * viewport.toFloat() / content.toFloat())
        .coerceIn(minThumbLength.coerceAtMost(trackLength), trackLength)
    val scrollRange: Float = (content - viewport).toFloat()
    val rawProgress: Float = (offset.toFloat() / scrollRange).coerceIn(0f, 1f)
    val progress: Float = if (reverseLayout) 1f - rawProgress else rawProgress

    return ThumbMetrics(
        length = length,
        start = (trackLength - length) * progress,
        scrollRange = scrollRange,
        trackLength = trackLength,
    )
}

///////////////////////////////////////////////
/// Preview
///////////////////////////////////////////////

@Preview
@Composable
fun ScrollBarPreview() {
    val listState = rememberLazyListState()
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState) {
            items(count = 40) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.Gray.copy(alpha = if (it % 2 == 0) .2f else .1f))
                )
            }
        }
        ScrollBar(
            modifier = Modifier.align(Alignment.CenterEnd),
            state = listState,
            autoHide = false,
        )
    }
}
