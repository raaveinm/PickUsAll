package com.raaveinm.picasso.ui.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.picasso.ui.chat.fragments.AllChats
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel

@Preview
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = ChatViewModel(),
    onChatClick: (Long) -> Unit = {},
    onGroupClick: (Long) -> Unit = {}
) {
    val state by viewModel.chatsUiState.collectAsState()

    Row(modifier) {
        AllChats(
            modifier = Modifier,
            conversations = state.conversations,
            onChatClick = onChatClick,
            onGroupClick = onGroupClick
        )
    }
}
