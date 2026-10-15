package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Switch(
    modifier: Modifier,
    selected: Int,
    onSelected: (Int) -> Unit,
    options: List<String>
) {
    val cornerRadius = 36.dp
    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        val slotWidth = maxWidth / options.size
        var animationFlag by remember { mutableStateOf(false) }
        var previousSelected by remember { mutableStateOf(selected) }
        var sizeMod by remember { mutableStateOf(1f) }
        LaunchedEffect(selected) {
            animationFlag = true
            if (previousSelected > selected) {
                sizeMod = 1.5f
            } else if (previousSelected < selected) {
                sizeMod = .75f
            }
            previousSelected = selected
            delay(120.milliseconds)
            animationFlag = false
        }
        val offset by animateDpAsState(
            targetValue = selected * slotWidth,
            animationSpec = tween(durationMillis = 240),
            label = "switch_offset"
        )
        val size by animateDpAsState(
            targetValue = if (animationFlag) (slotWidth.value * sizeMod).dp else slotWidth,
            animationSpec = tween(durationMillis = 200),
            label = "switch_toggler_size",
        )

        Box(
            Modifier
                .width(size)
                .defaultMinSize(minHeight = 36.dp + 2 * Dimensions.extraSmall)
                .graphicsLayer { translationX = offset.toPx() }
                .padding(Dimensions.extraSmall)
                .clip(RoundedCornerShape(cornerRadius))
                .background(MaterialTheme.colorScheme.onPrimaryContainer)
        )
        Row {
            options.forEachIndexed { index, option ->
                val contentColor by animateColorAsState(
                    targetValue = if (index == selected) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(durationMillis = 250),
                    label = "switch_content_color_$option"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 36.dp + 2 * Dimensions.extraSmall)
                        .clip(RoundedCornerShape(cornerRadius))
                        .clickable { onSelected(index) }
                        .padding(Dimensions.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = contentColor,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        softWrap = true,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SwitchPreview() {
    Switch(
        modifier = Modifier,
        selected = 2,
        onSelected = {},
        options = listOf("aaa", "bbb","ccc")
    )
}