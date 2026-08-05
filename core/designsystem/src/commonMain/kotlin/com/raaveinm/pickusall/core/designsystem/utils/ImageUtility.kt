package com.raaveinm.pickusall.core.designsystem.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

//
// Created by Kirill "Raaveinm" on 8/4/26.
//

@Composable
fun ImageUtility(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    enableGlowEffect: Boolean = false,
    blurPadding: Dp = 12.dp,
    fadeEdgeWidth: Dp = 24.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (enableGlowEffect) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = contentScale,
                alignment = Alignment.Center,
                filterQuality = filterQuality,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()

                        val fadePx = fadeEdgeWidth.toPx()
                        if (size.width > 0f && size.height > 0f && fadePx > 0f) {
                            val xFraction = (fadePx / size.width).coerceAtMost(0.5f)
                            val yFraction = (fadePx / size.height).coerceAtMost(0.5f)

                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0f to Color.Transparent,
                                    xFraction to Color.Black,
                                    1f - xFraction to Color.Black,
                                    1f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )

                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    yFraction to Color.Black,
                                    1f - yFraction to Color.Black,
                                    1f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
                    .blur(24.dp)
            )
        }

        Box(
            if (enableGlowEffect) Modifier.padding(blurPadding) else Modifier
        ) { // return@Box
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = contentScale,
                alignment = Alignment.Center,
                alpha = alpha,
                colorFilter = colorFilter,
                filterQuality = filterQuality,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp)
            )
        }
    }
}
