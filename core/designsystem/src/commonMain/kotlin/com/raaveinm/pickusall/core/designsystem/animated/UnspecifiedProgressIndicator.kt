package com.raaveinm.pickusall.core.designsystem.animated

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

//
// Created by Kirill "Raaveinm" on 10/27/2026.
//

@Preview
@Composable
fun UnspecifiedProgressIndicator(
    selfSize: Dp = 48.dp,
    color: Color = Color.Cyan
) {
    val degrees = listOf(
        -90f, -60f, -30f, 0f, 30f, 60f, 90f, 120f, 150f, 180f, 210f, 240f
//        0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f
    )
    var currentIndex by remember { mutableStateOf(Random.nextInt(0, degrees.size -1 )) }

    LaunchedEffect(Unit) {
        while (true) {
            currentIndex = (currentIndex + 1) % degrees.size
            delay(100.milliseconds)
        }
    }

    Canvas(Modifier.size(selfSize)) {

        val path = Path().apply {
            addRoundRect(
                roundRect = RoundRect(
                    rect = Rect(
                        offset = Offset(center.x - size.width/26, center.y + size.height/4.5f),
                        Size(size.height/13, size.height/4)
                    ),
                    cornerRadius = CornerRadius(6f)
                )
            )
        }

        degrees.forEachIndexed { index, degree ->
            val distance: Int = (currentIndex - index + degrees.size) % degrees.size
            val alpha: Int = 255 * (degrees.size - 1 - distance) / (degrees.size - 1)

            withTransform(
                transformBlock = { rotate(degrees = degree, pivot = center) },
                drawBlock = { drawPath(path, color.copy(alpha = alpha/255f)) }
            )
        }
    }
}