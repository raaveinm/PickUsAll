package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    nestedModifier: Modifier = Modifier,
    fabModifier: Modifier = Modifier,
    selectedId: Int,
    onItemClick: (Int) -> Unit = {},
) {
    val elements = listOf<@Composable () -> Unit>(
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Default.Apps,
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Outlined.Forum,
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Outlined.Pets,
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    )

    Row(
        modifier = modifier.clickable(interactionSource = MutableInteractionSource(), indication = null){},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            nestedModifier
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = Dimensions.medium),
        ) {
            val slotWidth: Dp = 48.dp
            val indicatorSize: Dp = 40.dp
            val indicatorInset: Dp = (slotWidth - indicatorSize) / 2
            val selectedSlot: Int = selectedId.coerceIn(elements.indices)
            var animationFlag by remember { mutableStateOf(false) }
            var previousSelected by remember { mutableStateOf(selectedSlot) }
            var sizeMod by remember { mutableStateOf(1f) }

            LaunchedEffect(selectedSlot) {
                if (previousSelected == selectedSlot) return@LaunchedEffect
                animationFlag = true
                sizeMod = if (previousSelected > selectedSlot) 1.25f else .75f
                previousSelected = selectedSlot
                delay(120.milliseconds)
                animationFlag = false
            }
            val offset: Dp by animateDpAsState(
                targetValue = selectedSlot * slotWidth + indicatorInset,
                animationSpec = tween(durationMillis = 240),
                label = "navbar_offset",
            )
            val indicatorWidth: Dp by animateDpAsState(
                targetValue = if (animationFlag) indicatorSize * sizeMod else indicatorSize,
                animationSpec = tween(durationMillis = 200),
                label = "navbar_indicator_size",
            )

            if (selectedId in elements.indices) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(indicatorWidth)
                        .height(indicatorSize)
                        .graphicsLayer { translationX = offset.toPx() }
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                )
            }
            Row {
                elements.forEachIndexed { index, content ->
                    Box(
                        Modifier
                            .padding(vertical = Dimensions.small)
                            .size(slotWidth)
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { onItemClick(index) },
                        contentAlignment = Alignment.Center
                    ) { content() }
                }
            }
        }

        FloatingActionButton(
            onClick = { onItemClick(3) },
            modifier = fabModifier.padding(start = Dimensions.large),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "burger_menu_fab"
            )
        }
    }
}

@Preview
@Composable
fun NavBarPreview() {
    var id by remember { mutableStateOf(0) }
    NavBar(
        selectedId = id,
        onItemClick = { id = it }
    )
}
