package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/24/2026.
//

private const val HOVER_FADE_MS: Int = 120
private const val HOVER_ALPHA: Float = .12f

@Composable
fun DropDownMenu(
    modifier: Modifier = Modifier,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    scrollbarLane: Dp = Dimensions.medium,
    itemHeight: Dp = 40.dp,
    maxVisibleItems: Int = 4
) {
    val scrollState = rememberScrollState()
    val isScrollable: Boolean = items.size > maxVisibleItems
    val spacing: Dp = Dimensions.extraSmall
    val maxListHeight: Dp = itemHeight * maxVisibleItems + spacing * (maxVisibleItems - 1)

    Box(
        modifier = modifier
            .clip(Shapes.roundedAverage)
            .background(color = MaterialTheme.colorScheme.surface)
            .border(
                color = MaterialTheme.colorScheme.surfaceVariant,
                width = 1.dp,
                shape = Shapes.roundedAverage,
            )
            .padding(Dimensions.extraSmall)
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 128.dp)
                .width(IntrinsicSize.Max)
                .heightIn(max = maxListHeight)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            items.forEachIndexed { index, label ->
                DropDownMenuItem(
                    label = label,
                    endPadding = if (isScrollable) scrollbarLane else Dimensions.none,
                    onClick = { onItemSelected(index) },
                    itemHeight = itemHeight
                )
            }
        }

        if (isScrollable) {
            ScrollBar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(maxListHeight),
                state = scrollState,
            )
        }
    }
}

///////////////////////////////////////////////
/// Item
///////////////////////////////////////////////

@Composable
private fun DropDownMenuItem(
    label: String, endPadding: Dp,
    onClick: () -> Unit,
    itemHeight: Dp = 40.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered: Boolean by interactionSource.collectIsHoveredAsState()
    val contentColor: Color = MaterialTheme.colorScheme.onSurface
    val background: Color by animateColorAsState(
        targetValue = if (isHovered) contentColor.copy(alpha = HOVER_ALPHA) else Color.Transparent,
        animationSpec = tween(durationMillis = HOVER_FADE_MS),
        label = "drop_down_item_hover"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .clip(Shapes.roundedSmall)
            .background(background)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick
            )
            .padding(start = Dimensions.sMedium, end = endPadding + Dimensions.sMedium),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun DropDownMenuScrollPreview() {
    DropDownMenu(
        items = List(9) { "option ${it + 1}" },
        onItemSelected = {}
    )
}
