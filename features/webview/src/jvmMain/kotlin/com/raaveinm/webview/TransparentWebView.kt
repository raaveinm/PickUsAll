package com.raaveinm.webview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

@Composable
internal fun TransparentWebView(
    modifier: Modifier,
    frame: ImageBitmap?,
    onResize: (IntSize, Float) -> Unit,
    onPointer: (MouseEvent, Int, Int) -> Unit,
    onKey: (KeyEvent) -> Unit,
    onFocus: (Boolean) -> Unit,
) {
    val density: Float = LocalDensity.current.density
    val focusRequester: FocusRequester = remember { FocusRequester() }
    Canvas(
        modifier = modifier
            .onSizeChanged { size -> onResize(size, density) }
            .focusRequester(focusRequester)
            .onFocusChanged { state -> onFocus(state.isFocused) }
            .onPreviewKeyEvent { event ->
                event.awtEventOrNull?.let { nativeEvent -> onKey(nativeEvent) }
                event.awtEventOrNull != null
            }
            .focusable()
            .pointerInput(density) {
                awaitPointerEventScope {
                    while (true) {
                        val event: PointerEvent = awaitPointerEvent()
                        val nativeEvent: MouseEvent = event.awtEventOrNull ?: continue
                        val position = event.changes.firstOrNull()?.position ?: continue
                        if (event.type == PointerEventType.Press) focusRequester.requestFocus()
                        onPointer(nativeEvent, (position.x / density).toInt(), (position.y / density).toInt())
                        event.changes.forEach { change -> change.consume() }
                    }
                }
            },
    ) {
        frame?.let { image ->
            drawImage(image = image, dstSize = IntSize(size.width.toInt(), size.height.toInt()))
        }
    }
}
