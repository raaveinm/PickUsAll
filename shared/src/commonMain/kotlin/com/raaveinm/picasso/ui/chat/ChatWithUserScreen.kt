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
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.picasso.AppConfig
import com.raaveinm.picasso.ui.chat.fragments.ChatMessages
import com.raaveinm.pickusall.core.designsystem.components.ChatTopBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun ChatWithUserScreen(
    modifier: Modifier = Modifier,
    conversation: Conversation?,
    onBack: () -> Unit = {},
    onUserIconClick: (Long) -> Unit = {},
    onLoadMoreHistory: () -> Unit = {},
    messageData: List<MessageData>
) {
    if (conversation == null) return

    val counterpart = (conversation as? Chat)?.chatTitle
    val title = when (conversation) {
        is Chat -> conversation.chatTitle.personaName
        is Palette -> conversation.name
    }
    val icon = when (conversation) {
        is Chat -> conversation.chatTitle.avatarMedium
        is Palette -> conversation.members.firstOrNull()?.avatarMedium
    }

    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChatTopBar(
            modifier = Modifier.sizeIn(maxWidth = 512.dp).padding(horizontal = Dimensions.medium),
            chatName = title,
            chatIcon = icon,
            onGoBackAction = onBack,
            onChatIconAction = { onUserIconClick(counterpart?.steamId ?: conversation.id) }
        )
        ChatMessages(
            modifier = Modifier.fillMaxSize(),
            user = AppConfig.USER_ID,
            messages = messageData,
            onLoadMore = onLoadMoreHistory
        )
    }
}
