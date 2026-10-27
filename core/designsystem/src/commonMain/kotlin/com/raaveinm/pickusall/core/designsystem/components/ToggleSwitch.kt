package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.raaveinm.pickusall.core.designsystem.animated.UnspecifiedProgressIndicator
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/27/2026.
//

@Preview
@Composable
fun ToggleSwitch(
    isChecked: Boolean = false,
    isInProgression: Boolean = false,
    onToggle: () -> Unit = {},
    disabledBackgroundColor: Color = MaterialTheme.colorScheme.onSurface,
    enabledBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    disabledForegroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    enabledForegroundColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    tintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    size: Size = Size(width = 60f, height = 32f)
) {
    val progress: Float by animateFloatAsState(
        targetValue = if (isChecked) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "toggle_progress"
    )

    val backgroundColor: Color = lerp(enabledBackgroundColor, disabledBackgroundColor, progress)
    val foregroundColor: Color = lerp(enabledForegroundColor, disabledForegroundColor, progress)

    BoxWithConstraints(
        modifier = Modifier
            .size(width = size.width.dp, height = size.height.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .border(width = 2.dp, tintColor, shape = RoundedCornerShape(size = 16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        val y = maxHeight
        val x = maxWidth
        val padding = 4.dp
        val thumb = maxHeight - (padding * 2)

        val leftEdge: Dp = lerp(padding, x - padding - thumb, EaseIn.transform(progress))
        val rightEdge: Dp = lerp(padding + thumb, x - padding, EaseOut.transform(progress))

        Box(Modifier
            .offset(x = leftEdge, y = padding)
            .size(width = rightEdge - leftEdge, height = thumb)
            .clip(Shapes.circleShape)
            .background(color = foregroundColor),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isInProgression,
                modifier = Modifier,
                enter = fadeIn(),
                exit = fadeOut(),
                label = "fade_animation",
                content = {
                    UnspecifiedProgressIndicator(
                        selfSize = y/2,
                        color = backgroundColor
                    )
                }
            )
        }
    }
}