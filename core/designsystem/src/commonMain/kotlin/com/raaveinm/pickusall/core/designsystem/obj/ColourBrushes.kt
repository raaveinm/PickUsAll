package com.raaveinm.pickusall.core.designsystem.obj

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Brush

//
// Created by Kirill "Raaveinm" on 10/19/2026.
//

object ColourBrushes {
    val ColorScheme.backgroundGradientBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(surface, surface, inverseOnSurface)
        )
}