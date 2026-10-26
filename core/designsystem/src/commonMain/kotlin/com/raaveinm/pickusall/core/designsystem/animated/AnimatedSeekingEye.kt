package com.raaveinm.pickusall.core.designsystem.animated

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

//
// Created by Kirill "Raaveinm" on 10/26/2026.
//

@Preview
@Composable
fun AnimatedSeekingEye(
    @Suppress("ModifierParameter") modifier: Modifier = Modifier.size(128.dp),
    eyeballColor: Color = Color(0xFFFFFFFF),
    eyeLidColor: Color = Color(0xFFBDBDBD)
) {
    var targetX by remember { mutableFloatStateOf(1f) }
    var targetY by remember { mutableFloatStateOf(1f) }
    var blinkUpper by remember { mutableFloatStateOf(1f) }
    var blinkLower by remember { mutableFloatStateOf(2f) }

    LaunchedEffect(Unit) {
        while (true) {
            targetY = Random.nextFloat() * (1.3f - .7f) + .7f
            targetX = Random.nextFloat() * (1.5f - .3f) + .3f
            delay(1500.milliseconds)
        }
    }

    val animatedCenterX by animateFloatAsState(targetX, tween(500), label = "x")
    val animatedCenterY by animateFloatAsState(targetY, tween(600), label = "y")

    LaunchedEffect(Unit) {
        while (true) {
            blinkUpper = if (blinkUpper == 0f) 1f else 0f
            blinkLower = if (blinkLower == 2f) 1f else 2f
            val delayTime = if (blinkUpper == 0f) 5_000.milliseconds else 200.milliseconds
            delay(duration = delayTime)
        }
    }

    val animatedBlinkUp by animateFloatAsState(blinkUpper, tween(120), label = "x")
    val animatedBlinkLow by animateFloatAsState(blinkLower, tween(120), label = "y")

    Canvas(modifier) {
        val x = size.width
        val y = size.height
        val eyeCurve = Path().apply {
            moveTo(0f,y/2)
            quadraticTo(
                x1 = x/2,
                y1 = y/2*animatedBlinkUp,
                x2 = x,
                y2 = y/2
            )
            moveTo(x, y/2)
            quadraticTo(
                x1 = x/2,
                y1 = y/animatedBlinkLow,
                x2 = 0f,
                y2 = y/2
            )
        }

        clipPath(path = eyeCurve) {
            drawCircle(
                color = eyeballColor,
                radius = min(x,y)/10,
                center = Offset(x/2 * animatedCenterX, y/2 * animatedCenterY)
            )
        }
        drawPath(
            path = eyeCurve,
            color = eyeLidColor,
            style = Stroke(width = 10f)
        )
    }
}