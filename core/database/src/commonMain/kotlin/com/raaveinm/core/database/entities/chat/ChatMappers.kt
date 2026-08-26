package com.raaveinm.core.database.entities.chat

import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.Palette

/**
 * `core/model.chat.*` is a UI/domain model (embeds full `User` objects for the
 * screens to render directly), not a network wire DTO — so mapping it into Room's
 * flat, normalized entities means pulling `.steamId` off the embedded users here.
 * The local Room row is created separately; its autoGenerate id is resolved by the
 * caller (repository layer), same for serverId (connection context, not part of
 * the domain model).
 */
fun Conversation.toEntity(serverId: Long): Conversations = Conversations(
    serverId = serverId,
    kind = when (this) {
        is Chat -> "chat"
        is Palette -> "palette"
    },
    lastMessage = lastMessage,
    remoteId = id
)

fun Chat.toEntity(localConversationId: Long): Chats = Chats(
    conversationId = localConversationId,
    chatTitleSteamId = chatTitle.steamId
)

fun Palette.toEntity(localConversationId: Long): Palettes = Palettes(
    conversationId = localConversationId,
    name = name
)

fun Palette.toMemberEntities(localConversationId: Long): List<PaletteMembers> =
    members.map { member ->
        PaletteMembers(
            paletteConversationId = localConversationId,
            userSteamId = member.steamId
        )
    }

// Reverse direction, for reading the chat list back out of Room. listMessageData is
// intentionally left empty here - the list view only needs lastMessage, full message
// history is loaded separately per opened conversation.
fun ChatWithTitle.toDto(): Chat = Chat(
    id = conversation.id,
    chatTitle = titleUser.toDto(),
    lastMessage = conversation.lastMessage
)

fun PaletteWithMembers.toDto(): Palette = Palette(
    id = conversation.id,
    name = palette.name,
    members = members.map { it.toDto() },
    lastMessage = conversation.lastMessage
)
