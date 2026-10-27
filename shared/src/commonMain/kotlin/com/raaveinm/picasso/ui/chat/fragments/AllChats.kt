package com.raaveinm.picasso.ui.chat.fragments

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.picasso.ui.actions.sideAction
import com.raaveinm.pickusall.core.designsystem.components.ChatPreview
import com.raaveinm.pickusall.core.designsystem.components.DropDownMenu
import com.raaveinm.pickusall.core.designsystem.obj.NoPreviousMessages
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.chat_menu_delete_chat
import pickusall.shared.generated.resources.chat_menu_invite_to_group
import pickusall.shared.generated.resources.chat_menu_no_groups
import pickusall.shared.generated.resources.chat_menu_steam_profile
import pickusall.shared.generated.resources.chat_menu_text_user
import pickusall.shared.generated.resources.chat_more_actions_label
import pickusall.shared.generated.resources.chat_open_label

private enum class ChatMenuPage { CLOSED, ACTIONS, GROUPS }

@Composable
fun AllChats(
    modifier: Modifier = Modifier,
    conversations: List<Conversation>,
    onChatClick: (Long) -> Unit = {},
    onGroupClick: (Long) -> Unit = {},
    onInviteToGroup: (paletteId: Long, steamId: Long) -> Unit = { _, _ -> },
    selectedChatId: Long? = 4
) {
    val palettes: List<Palette> = conversations.filterIsInstance<Palette>()

    LazyColumn(
        modifier = modifier.sizeIn(maxWidth = 512.dp),
        verticalArrangement = Arrangement.spacedBy(Dimensions.extraSmall),
        contentPadding = PaddingValues(bottom = PlatformSpecificDim.contentPaddingAdvanced)
    ) {
        conversations.forEach { conversation ->
            item(key = conversation.id) {
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
                    is Chat -> ChatRow(
                        chat = conversation,
                        palettes = palettes,
                        onChatClick = onChatClick,
                        onInviteToGroup = onInviteToGroup,
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

///////////////////////////////////////////////
/// Chat row with its dropdown menu
///////////////////////////////////////////////

@Composable
private fun ChatRow(
    chat: Chat,
    palettes: List<Palette>,
    onChatClick: (Long) -> Unit,
    onInviteToGroup: (paletteId: Long, steamId: Long) -> Unit,
    surfaceColor: Color,
    headlineColor: Color,
    tintColor: Color
) {
    var menuPage: ChatMenuPage by remember { mutableStateOf(ChatMenuPage.CLOSED) }
    val uriHandler = LocalUriHandler.current
    val title: String = chat.chatTitle.personaName

    val inviteLabel: String = stringResource(Res.string.chat_menu_invite_to_group)
    val profileLabel: String = stringResource(Res.string.chat_menu_steam_profile)
    val textLabel: String = stringResource(Res.string.chat_menu_text_user)
    val deleteLabel: String = stringResource(Res.string.chat_menu_delete_chat)
    val noGroupsLabel: String = stringResource(Res.string.chat_menu_no_groups)

    val actions: List<Pair<String, () -> Unit>> = buildList {
        if (chat.writable) add(inviteLabel to { menuPage = ChatMenuPage.GROUPS })
        add(profileLabel to {
            menuPage = ChatMenuPage.CLOSED
            uriHandler.openUri(chat.chatTitle.profileUrl)
        })
        add(textLabel to {
            menuPage = ChatMenuPage.CLOSED
            onChatClick(chat.id)
        })
        add(deleteLabel to {
            menuPage = ChatMenuPage.CLOSED
            //TODO(delete chat: the server has no endpoint for removing a conversation yet)
        })
    }

    val invitable: List<Palette> = palettes.filter { palette ->
        palette.members.none { member -> member.steamId == chat.chatTitle.steamId }
    }

    Box {
        ChatPreview(
            chatTitle = title,
            iconLink = chat.chatTitle.avatarMedium,
            lastMessage = chat.lastMessage ?: NoPreviousMessages.messages.random(),
            modifier = Modifier.sideAction(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClickLabel = stringResource(Res.string.chat_open_label, title),
                onSideActionLabel = stringResource(Res.string.chat_more_actions_label, title),
                onSideAction = { menuPage = ChatMenuPage.ACTIONS },
                onClick = { onChatClick(chat.id) }
            ),
            surfaceColor = surfaceColor,
            headlineColor = headlineColor,
            tintColor = tintColor
        )

        DropDownMenu(
            expanded = menuPage != ChatMenuPage.CLOSED,
            onDismissRequest = { menuPage = ChatMenuPage.CLOSED },
            items = when (menuPage) {
                ChatMenuPage.GROUPS -> if (invitable.isEmpty()) listOf(noGroupsLabel) else invitable.map { it.name }
                else -> actions.map { it.first }
            },
            onItemSelected = { index ->
                when (menuPage) {
                    ChatMenuPage.GROUPS -> {
                        menuPage = ChatMenuPage.CLOSED
                        invitable.getOrNull(index)?.let { palette ->
                            onInviteToGroup(palette.id, chat.chatTitle.steamId)
                        }
                    }
                    else -> actions[index].second()
                }
            }
        )
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
