package com.raaveinm.picasso.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.picasso.ui.chat.fragments.ChatMessages
import com.raaveinm.pickusall.core.designsystem.components.ChatTopBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun ChatWithUserScreen(
    modifier: Modifier = Modifier,
    chat: Chat?,
    onBack: () -> Unit = {},
    onUserIconClick: (Long) -> Unit = {},
    messageData: List<MessageData>
) {
    val user = chat?.chatTitle

    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user != null) {
            ChatTopBar(
                modifier = Modifier.sizeIn(maxWidth = 512.dp).padding(horizontal = Dimensions.medium),
                chatName = user.personaName,
                chatIcon = user.avatarMedium,
                onGoBackAction = onBack,
                onChatIconAction = { onUserIconClick(user.steamId) }
            )
            ChatMessages(
                modifier = Modifier.fillMaxSize(),
                user = user,
                messages = messageData
            )
        }
    }
}
