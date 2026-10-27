package com.raaveinm.pickusall.core.designsystem.animated

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

//
// Created by Kirill "Raaveinm" on 10/27/2026.
//

@Preview
@Composable
fun UnspecifiedProgressIndicator(
    selfSize: Dp = 64.dp,
    widthAngular: Float = 6f,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val strokes = listOf(
        -90f, -60f, -30f, 0f, 30f, 60f, 90f, 120f, 150f, 180f, 210f, 240f
    )
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            currentIndex = (currentIndex + 1) % strokes.size
            delay(100.milliseconds)
        }
    }

    Canvas(Modifier.size(selfSize)) {
        val excludePath = Path().apply {
            addOval(Rect(Offset(center.x, center.y),size.height/4))
        }

        val clipPath = Path().apply {
            addOval(Rect(Offset(center.x, center.y),size.height/2))
            op(this, excludePath, PathOperation.Difference)
        }

        clipPath(clipPath) {
            strokes.forEachIndexed { index, angular ->
                val distance: Int = (currentIndex - index + strokes.size) % strokes.size
                val alpha: Int = 255 * (strokes.size - 1 - distance) / (strokes.size - 1)
                drawArc(
                    color = color.copy(alpha = alpha/255f),
                    startAngle = angular - widthAngular/2,
                    sweepAngle = widthAngular,
                    useCenter = true
                )
            }
        }
    }
}