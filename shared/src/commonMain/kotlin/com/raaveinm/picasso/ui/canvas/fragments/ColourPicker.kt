package com.raaveinm.picasso.ui.canvas.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.game.GameQueueItem
import com.raaveinm.pickusall.core.designsystem.components.GameCard
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.add_game_to_complete

@Composable
fun ColourPicker(
    modifier: Modifier = Modifier,
    gameQueue: List<GameQueueItem>,
    onReorder: (orderedGameIds: List<Int>) -> Unit = {},
    onAddToQueue: (gameId: Int, priority: Int) -> Unit = { _, _ -> }
) {
    var items by remember { mutableStateOf(gameQueue) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    var showAddCard by remember { mutableStateOf(false) }
    val transition = animateFloatAsState(
        targetValue = if (showAddCard) 24f else 0f,
        animationSpec = tween(300)
    )

    LaunchedEffect(gameQueue) {
        if (draggingIndex == null) items = gameQueue
    }

    LazyColumn(
        modifier.blur(transition.value.dp),
        state = listState,
        contentPadding = PaddingValues(vertical = Dimensions.paddingAboveAverage),
        verticalArrangement = Arrangement.spacedBy(Dimensions.sMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(items, key = { _, item -> item.gameId }) { index, item ->
            val isDragging = index == draggingIndex

            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .sizeIn(maxWidth = 1024.dp)
                    .fillMaxWidth()
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = if (isDragging) dragOffsetY else 0f },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                    GameCard(
                        modifier = Modifier.sizeIn(maxHeight = 128.dp),
                        gameId = item.gameId,
                        text = null,
                        enableGlowEffect = false
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f).padding(start = Dimensions.sMedium),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "drag_handle",
                        modifier = Modifier
                            .padding(end = Dimensions.sMedium)
                            .pointerInput(item.gameId) {
                                detectDragGestures(
                                    onDragStart = {
                                        draggingIndex = index
                                        dragOffsetY = 0f
                                    },
                                    onDragEnd = {
                                        draggingIndex = null
                                        dragOffsetY = 0f
                                        onReorder(items.map { it.gameId })
                                    },
                                    onDragCancel = {
                                        draggingIndex = null
                                        dragOffsetY = 0f
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y
                                    val current = draggingIndex ?: return@detectDragGestures
                                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                                    val currentLayout = visibleItems
                                        .find { it.index == current } ?: return@detectDragGestures
                                    val draggedCenter =
                                        currentLayout.offset + currentLayout.size / 2 + dragOffsetY

                                    val target = visibleItems.find { info ->
                                        info.index != current &&
                                                draggedCenter >= info.offset &&
                                                draggedCenter <= info.offset + info.size
                                    } ?: when {
                                        draggedCenter <= visibleItems.first().offset ->
                                            visibleItems.firstOrNull { it.index != current }
                                        draggedCenter >= visibleItems.last().let { it.offset + it.size } ->
                                            visibleItems.lastOrNull { it.index != current }
                                        else -> null
                                    }
                                    if (target != null) {
                                        val targetIndex = target.index.coerceIn(0, items.lastIndex)
                                        items = items.toMutableList().apply {
                                            add(targetIndex, removeAt(current))
                                        }
                                        dragOffsetY += (currentLayout.offset - target.offset).toFloat()
                                        draggingIndex = targetIndex
                                    }
                                }
                            }
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .sizeIn(maxWidth = 1024.dp)
                    .fillMaxWidth()
                    .clickable { showAddCard = true },
                shape = RoundedCornerShape(Shapes.smallShape),
                colors = CardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .84f),
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.surface
                ),
                content = {
                    Column {
                        Column(
                            Modifier.padding(vertical = Dimensions.small),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "add_game"
                            )
                            Text(
                                text = stringResource(Res.string.add_game_to_complete),
                                modifier = Modifier.fillMaxWidth().padding(4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )

                        }
                    }
                }
            )
        }
    }

    AnimatedVisibility(
        visible = showAddCard,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { showAddCard = false },
            contentAlignment = Alignment.Center
        ) {
            AddGameCard(
                modifier = Modifier.sizeIn(minWidth = 128.dp, maxWidth = 256.dp),
                onDismiss = { showAddCard = false },
                onSubmit = { gameId, priority ->
                    onAddToQueue(gameId, priority)
                    showAddCard = false
                }
            )
        }
    }
}
