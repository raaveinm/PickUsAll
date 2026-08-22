package com.raaveinm.picasso.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.picasso.ui.chat.fragments.ChatMessages
import com.raaveinm.pickusall.core.designsystem.components.ChatTopBar

@Composable
fun ChatWithUserScreen(
    modifier: Modifier = Modifier,
    chatId: Long,
    onBack: () -> Unit = {},
    onUserIconClick: (Long) -> Unit = {}
) {
    val user = Mock.chatList.firstOrNull { it.id == chatId }?.chatTitle

    Column(modifier.fillMaxSize()) {
        if (user != null) {
            ChatTopBar(
                chatName = user.personaName,
                chatIcon = user.avatarMedium,
                onGoBackAction = onBack,
                onChatIconAction = { onUserIconClick(user.steamId) }
            )
            ChatMessages(
                modifier = Modifier.fillMaxSize(),
                user = user,
                messages = emptyList()
            )
        }
    }
}
