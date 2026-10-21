package com.raaveinm.picasso.ui.chat.fragments

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.pickusall.core.designsystem.components.ChatPreview
import com.raaveinm.pickusall.core.designsystem.obj.NoPreviousMessages
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim

@Composable
fun AllChats(
    modifier: Modifier = Modifier,
    conversations: List<Conversation>,
    onChatClick: (Long) -> Unit = {},
    onGroupClick: (Long) -> Unit = {},
    selectedChatId: Long? = 4
) {
    LazyColumn(
        modifier = modifier.sizeIn(maxWidth = 512.dp),
        verticalArrangement = Arrangement.spacedBy(Dimensions.extraSmall),
        contentPadding = PaddingValues(bottom = PlatformSpecificDim.contentPaddingAdvanced)
    ) {
        conversations.forEach { conversation ->
            item {
                val condition = selectedChatId == conversation.id
                val containerColor by animateColorAsState(
                    targetValue =
                        if (condition) MaterialTheme.colorScheme.inverseOnSurface
                        else MaterialTheme.colorScheme.secondaryContainer,
                    animationSpec = tween(durationMillis = 240),
                    label = "container_color"
                )
                val headlineColor by animateColorAsState(
                    targetValue =
                        if (condition) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                    animationSpec = tween(durationMillis = 240),
                    label = "headline_color"
                )
                val tintColor by animateColorAsState(
                    targetValue =
                        if (condition) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .86f),
                    animationSpec = tween(durationMillis = 240),
                    label = "tint_color"
                )

                when (conversation) {
                    is Chat -> ChatPreview(
                        chatTitle = conversation.chatTitle.personaName,
                        iconLink = conversation.chatTitle.avatarMedium,
                        lastMessage = conversation.lastMessage ?: NoPreviousMessages.messages.random(),
                        modifier = Modifier,
                        onClick = { onChatClick(conversation.id) },
                        surfaceColor = containerColor,
                        headlineColor = headlineColor,
                        tintColor = tintColor
                    )
                    is Palette -> ChatPreview(
                        chatTitle = conversation.name,
                        iconLink = conversation.members.firstOrNull()?.avatarMedium ?: "",
                        lastMessage = conversation.lastMessage ?: NoPreviousMessages.messages.random(),
                        modifier = Modifier,
                        onClick = { onGroupClick(conversation.id) },
                        surfaceColor = containerColor,
                        headlineColor = headlineColor,
                        tintColor = tintColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AllChatsPreview() {
    AllChats(
        conversations = Mock.conversationList,
        modifier = Modifier
    )
}
