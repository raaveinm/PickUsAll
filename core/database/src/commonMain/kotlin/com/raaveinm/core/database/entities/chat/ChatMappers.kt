package com.raaveinm.core.database.entities.chat

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

// No MessageData mapper yet: the domain MessageData.timestamp is a display-ready
// string (e.g. "11:09" in mocks), not the epoch Long the Room entity stores — that
// mismatch is real, not just a rename, and papering over it here would be guessing.
// Revisit once real chat network payloads (with an actual epoch) exist.
