package com.raaveinm.pickusall.core.designsystem.obj

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

//
// Created by Kirill "Raaveinm" on 10/19/2026.
//

object ColorBrushes {
    val ColorScheme.backgroundGradientBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(surface, surface, inverseOnSurface)
        )

    val ColorScheme.dissolveWithSurface: Brush
        get() = Brush.verticalGradient(
            startY = 8f, colors = listOf(surface, Color.Transparent)
    )
    val ColorScheme.dissolveWithInverseOnSurface: Brush
        get() = Brush.verticalGradient(
            startY = 8f, colors = listOf(Color.Transparent, inverseOnSurface)
        )
}