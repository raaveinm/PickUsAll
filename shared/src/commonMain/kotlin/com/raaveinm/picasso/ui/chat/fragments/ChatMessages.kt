package com.raaveinm.picasso.ui.chat.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.picasso.ui.actions.sideAction
import com.raaveinm.pickusall.core.designsystem.components.Messages
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

private const val MAX_SELECTED_MESSAGES: Int = 100

@Composable
fun ChatMessages(
    modifier: Modifier = Modifier,
    messages: List<MessageData> = emptyList(),
    user: Long?,
    onLoadMore: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(vertical = Dimensions.medium),
    isEditorMode: Boolean = false,
    onEditorModeToggle: () -> Unit = {},
    selectedMessages: MutableList<MessageData>
) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier,
        state = listState,
        reverseLayout = true,
        contentPadding = contentPadding
    ) {
        itemsIndexed(messages) { index, message ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.sideAction(
                    onClick = {
                        if (isEditorMode) {
                            if (selectedMessages.contains(message)) {
                                selectedMessages.remove(message)
                            } else if (selectedMessages.size < MAX_SELECTED_MESSAGES) {
                                selectedMessages.add(message)
                            }
                        }
                    },
                    onSideAction = {
                        onEditorModeToggle()
                        if (!isEditorMode) { selectedMessages.add(message) }
                        else { selectedMessages.clear() }
                    }
                )) {
                AnimatedVisibility (
                    visible = isEditorMode,
                    modifier = Modifier.align(Alignment.CenterVertically).clip(Shapes.circleShape),
                    enter = expandHorizontally(
                        animationSpec = tween(300),
                        expandFrom = Alignment.Start
                    ),
                    exit = shrinkHorizontally()
                ) {
                    val isSelected: Boolean = selectedMessages.contains(message)
                    val animatedFill by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = tween(300)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = Dimensions.small)
                            .clip(Shapes.circleShape)
                            .size(18.dp)
                            .background(animatedFill)
                            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = Shapes.circleShape)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                    Messages(
                        iconLink = message.user.avatarMedium,
                        username = message.user.personaName,
                        textMessage = message.textMessage,
                        timestamp = message.timestamp,
                        isSender = user != null && user == message.user.steamId,
                        modifier = Modifier,
                        previousExisted = index < messages.lastIndex && messages[index + 1].user == message.user,
                        isLast = index == 0 || messages[index - 1].user != message.user
                    )

            }
            if (index == messages.lastIndex) { onLoadMore() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatMessagesPreview() {

    ChatMessages(
        user = Mock.user_1.steamId,
        messages = listOf(
            MessageData(user = Mock.user_1, textMessage = "Mornin'", timestamp = "11:09"),
            MessageData(user = Mock.user_1, textMessage = "<3", timestamp = "11:09"),
            MessageData(user = Mock.user_1, textMessage = "<3", timestamp = "11:09"),
            MessageData(user = Mock.user_2, textMessage = "Mornin' <3", timestamp = "11:10"),
        ),
        selectedMessages = remember { mutableStateListOf() }
    )
}