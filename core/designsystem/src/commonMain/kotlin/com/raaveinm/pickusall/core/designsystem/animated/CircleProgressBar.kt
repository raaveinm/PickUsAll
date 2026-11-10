package com.raaveinm.pickusall.core.designsystem.animated

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

//
// Created by Kirill "Raaveinm" on 11/09/2026.
//

/**
 * @param modifier have no idea what is it for (really I am UwU)
 * @param colors colors changing behavior depended on progress level. Colors are set in list from
 * the lowest progress to the highest
 * @param progress its naming says for itself. In values from `0f` to `1f`
 */

@Preview
@Composable
fun CircleProgressBar(
    @Suppress("ModifierParameter") modifier: Modifier = Modifier.size(256.dp),
    colors: List<Color> = listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.primary),
    progress: Float = 0.5f
) {
    Canvas(modifier) {
        val excludePath = Path().apply {
            addOval(Rect(Offset(center.x, center.y),size.height/4))
        }

        val clipPath = Path().apply {
            addOval(Rect(Offset(center.x, center.y),size.height/2))
            op(this, excludePath, PathOperation.Difference)
        }
        clipPath(clipPath) {
            rotate(degrees = -90f, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(colors),
                    startAngle = 0f,
                    sweepAngle = 360 * progress,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    style = Fill
                )
            }
        }
    }
}