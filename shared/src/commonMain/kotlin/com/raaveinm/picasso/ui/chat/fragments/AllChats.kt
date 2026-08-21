package com.raaveinm.picasso.ui.chat.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.pickusall.core.designsystem.components.ChatPreview
import com.raaveinm.pickusall.core.designsystem.obj.NoPreviousMessages
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun AllChats(
    modifier: Modifier = Modifier,
    chats: List<Chat>,
) {
    LazyColumn(
        modifier = modifier.sizeIn(maxWidth = 512.dp),
        verticalArrangement = Arrangement.spacedBy(Dimensions.small)
    ) {
        chats.forEach { chat ->
            item {
                ChatPreview(
                    chatTitle = chat.chatTitle,
                    iconLink = chat.iconLink,
                    lastMessage = chat.lastMessage?: NoPreviousMessages.messages.random(),
                    modifier = Modifier,
                    onClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
fun AllChatsPreview() {
    AllChats(
        chats = Mock.chatList,
        modifier = Modifier
    )
}