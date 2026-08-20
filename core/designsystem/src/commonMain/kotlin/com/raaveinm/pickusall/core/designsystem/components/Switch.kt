package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun Switch(
    modifier: Modifier,
    selected: Int,
    onSelected: (Int) -> Unit,
    options: List<String>
) {
    val cornerRadius = 36.dp
    Row(
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selected
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else Color(0x000000FF),
                animationSpec = tween(durationMillis = 250)
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                animationSpec = tween(durationMillis = 250)
            )
            Box(
                modifier = Modifier
                    .padding(Dimensions.extraSmall)
                    .weight(1f)
                    .defaultMinSize(minHeight = 40.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(backgroundColor)
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    color = contentColor,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    softWrap = true,
                    maxLines = 2
                )
            }
        }
    }
}

@Preview
@Composable
fun SwitchPreview() {
    Switch(
        modifier = Modifier,
        selected = 0,
        onSelected = {},
        options = listOf("aaa", "bbb","ccc")
    )
}