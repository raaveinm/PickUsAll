package com.raaveinm.picasso.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.picasso.AppConfig
import com.raaveinm.picasso.ui.chat.fragments.ChatMessages
import com.raaveinm.pickusall.core.designsystem.components.ChatTextBar
import com.raaveinm.pickusall.core.designsystem.components.ChatTopBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.app_name

@Composable
fun ChatWithUserScreen(
    modifier: Modifier = Modifier,
    conversation: Conversation?,
    onBack: () -> Unit = {},
    onUserIconClick: (Long) -> Unit = {},
    onLoadMoreHistory: () -> Unit = {},
    onSendMessage: (String) -> Unit = {},
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

    Box(
        modifier.fillMaxSize()
    ) {
        ChatTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .sizeIn(maxWidth = 512.dp)
                .padding(horizontal = Dimensions.medium),
            chatName = title,
            chatIcon = icon,
            onGoBackAction = onBack,
            onChatIconAction = { onUserIconClick(counterpart?.steamId ?: conversation.id) }
        )
        ChatMessages(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
                .align(Alignment.Center),
            user = AppConfig.USER_ID,
            messages = messageData,
            onLoadMore = onLoadMoreHistory,
            contentPadding = PaddingValues(top = Dimensions.paddingAboveAverage, bottom = Dimensions.paddingLarge)
        )
        val messageFieldState = rememberTextFieldState()
        ChatTextBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .padding(bottom = Dimensions.paddingAverage)
                .sizeIn(maxWidth = 512.dp, maxHeight = 128.dp)
                .fillMaxWidth()
                .padding(Dimensions.small),
            textFieldState = messageFieldState,
            onKeyboardAction = {
                val text = messageFieldState.text.toString()
                if (text.isNotBlank()) onSendMessage(text)
            },
            hint = stringResource(Res.string.app_name)
        )
    }
}
