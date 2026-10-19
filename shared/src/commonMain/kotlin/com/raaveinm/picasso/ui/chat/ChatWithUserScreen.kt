package com.raaveinm.picasso.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.picasso.ui.chat.fragments.ChatMessages
import com.raaveinm.pickusall.core.designsystem.components.AcceptChatRequest
import com.raaveinm.pickusall.core.designsystem.components.ChatTextBar
import com.raaveinm.pickusall.core.designsystem.components.ChatTopBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.chat_message_hint
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ChatWithUserScreen(
    modifier: Modifier = Modifier,
    systemBarModifier: Modifier = Modifier,
    conversation: Conversation?,
    onBack: () -> Unit = {},
    onUserIconClick: (Long) -> Unit = {},
    onLoadMoreHistory: () -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onCallClicked: () -> Unit = {},
    onResumeDm: (Long) -> Unit = {},    // frozen DM: ask the counterpart to be allies again (accepts theirs if pending)
    onDeclineDm: (Long) -> Unit = {},   // frozen DM: leave them a stranger
    onBlockDm: (Long) -> Unit = {},     // frozen DM: block the counterpart
    callState: Boolean = false,
    messageData: List<MessageData>,
    selfSteamId: Long?,
    onCopyMessageData: (List<MessageData>) -> Unit = {},
    onDeleteMessages: (List<Long>) -> Unit = {}
) {
    if (conversation == null) return
    var isEditorMode by remember { mutableStateOf(false) }
    var buttonNotifyTimer by remember { mutableStateOf(false) }
    val selectedMessages: MutableList<MessageData> = remember { mutableStateListOf() }

    val counterpart = (conversation as? Chat)?.chatTitle
    val title = when (conversation) {
        is Chat -> conversation.chatTitle.personaName
        is Palette -> conversation.name
    }
    val icon = when (conversation) {
        is Chat -> conversation.chatTitle.avatarMedium
        is Palette -> conversation.members.firstOrNull()?.avatarMedium
    }

    LaunchedEffect(selectedMessages.size) {
        if (selectedMessages.isEmpty()) isEditorMode = false
    }

    LaunchedEffect(buttonNotifyTimer) {
        if (buttonNotifyTimer) {
            delay(621.milliseconds)
            isEditorMode = false
            buttonNotifyTimer = false
        }
    }

    Box(modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .sizeIn(maxWidth = 512.dp)
                .padding(horizontal = Dimensions.medium)
                .animateContentSize(tween(200)),
        ) {
            AnimatedVisibility (
                visible = isEditorMode,
                modifier = Modifier.align(Alignment.CenterVertically).clip(Shapes.circleShape),
                enter = expandHorizontally(
                    animationSpec = tween(300),
                    expandFrom = Alignment.Start
                ),
                exit = shrinkHorizontally(
                    animationSpec = tween(300),
                    shrinkTowards = Alignment.Start
                )
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = Dimensions.small)
                            .size(48.dp)
                            .clip(Shapes.circleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                onCopyMessageData(selectedMessages)
                                buttonNotifyTimer = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (buttonNotifyTimer) Icons.Default.DoneAll else Icons.Default.CopyAll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = Dimensions.small)
                            .size(48.dp)
                            .clip(Shapes.circleShape)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .clickable {
                                val ownLocalIds: List<Long> = selectedMessages
                                    .filter { it.user.steamId == selfSteamId }
                                    .map { it.localId }
                                if (ownLocalIds.isNotEmpty()) onDeleteMessages(ownLocalIds)
                                selectedMessages.clear()
                                isEditorMode = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            ChatTopBar(
                modifier = systemBarModifier.weight(1f),
                chatName = title,
                chatIcon = icon,
                onGoBackAction = onBack,
                onChatIconAction = { onUserIconClick(counterpart?.steamId ?: conversation.id) }
            )
        }

        if (counterpart != null && !conversation.writable) {
            AcceptChatRequest(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = Dimensions.paddingAboveAverage, bottom = Dimensions.paddingAverage),
                inviteeName = counterpart.personaName,
                inviteeIcon = counterpart.avatarFull,
                onAccept = { onResumeDm(counterpart.steamId) },
                onReject = { onDeclineDm(counterpart.steamId); onBack() },
                onBlock = { onBlockDm(counterpart.steamId); onBack() }
            )
            return@Box
        }

        ChatMessages(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
                .align(Alignment.Center),
            user = selfSteamId ?: 0L,
            messages = messageData,
            onLoadMore = onLoadMoreHistory,
            contentPadding = PaddingValues(top = Dimensions.paddingAboveAverage, bottom = Dimensions.paddingLarge),
            isEditorMode = isEditorMode,
            onEditorModeToggle = { isEditorMode = !isEditorMode },
            selectedMessages = selectedMessages
        )
        val messageFieldState = rememberTextFieldState()
        ChatTextBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .padding(bottom = Dimensions.small)
                .sizeIn(maxWidth = 512.dp, maxHeight = 128.dp)
                .fillMaxWidth()
                .padding(Dimensions.small),
            textFieldState = messageFieldState,
            onKeyboardAction = {
                val text = messageFieldState.text.toString()
                if (text.isNotBlank()) onSendMessage(text)
            },
            hint = stringResource(Res.string.chat_message_hint),
            onCallClicked = onCallClicked,
            callState = callState
        )
    }
}
