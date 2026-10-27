package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface,
    foregroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    tintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    size: Size = Size(width = 60f, height = 32f)
) {
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
        val size = maxHeight - (padding * 2)

        val position by animateDpAsState(
            targetValue = if (isChecked) x - size - padding else padding,
            label = "position_animation"
        )

        Box(Modifier
            .offset(x = position, y = y - size - padding)
            .size(size)
            .clip(Shapes.circleShape)
            .background(color = foregroundColor)
        ) {
            AnimatedVisibility(
                visible = isInProgression,
                modifier = Modifier,
                enter = fadeIn(),
                exit = fadeOut(),
                label = "fade_animation",
                content = {

                }
            )
        }
    }
}