package com.raaveinm.picasso.ui.chat.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
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

@Composable
fun AllChats(
    modifier: Modifier = Modifier,
    conversations: List<Conversation>,
    onChatClick: (Long) -> Unit = {},
    onGroupClick: (Long) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.sizeIn(maxWidth = 512.dp),
        verticalArrangement = Arrangement.spacedBy(Dimensions.small)
    ) {
        conversations.forEach { conversation ->
            item {
                when (conversation) {
                    is Chat -> ChatPreview(
                        chatTitle = conversation.chatTitle.personaName,
                        iconLink = conversation.chatTitle.avatarMedium,
                        lastMessage = conversation.lastMessage ?: NoPreviousMessages.messages.random(),
                        modifier = Modifier,
                        onClick = { onChatClick(conversation.id) }
                    )
                    is Palette -> ChatPreview(
                        chatTitle = conversation.name,
                        iconLink = conversation.members.firstOrNull()?.avatarMedium ?: "",
                        lastMessage = conversation.lastMessage ?: NoPreviousMessages.messages.random(),
                        modifier = Modifier,
                        onClick = { onGroupClick(conversation.id) }
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
