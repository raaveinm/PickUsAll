package com.raaveinm.core.database.entities.chat

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import com.raaveinm.core.database.entities.api.user.Users

data class ChatWithTitle(
    @Embedded val conversation: Conversations,
    @Embedded val chat: Chats,
    @Relation(parentColumns = ["chatTitleSteamId"], entityColumns = ["steamId"])
    val titleUser: Users
)

data class MessageWithSender(
    @Embedded val message: MessageData,
    @Relation(parentColumns = ["senderSteamId"], entityColumns = ["steamId"])
    val sender: Users
)

data class PaletteWithMembers(
    @Embedded val conversation: Conversations,
    @Embedded val palette: Palettes,
    @Relation(
        parentColumns = ["conversationId"],
        entityColumns = ["steamId"],
        associateBy = Junction(
            value = PaletteMembers::class,
            parentColumns = ["paletteConversationId"],
            entityColumns = ["userSteamId"]
        )
    )
    val members: List<Users>
)

///////////////////////////////////////////////
// Sync
///////////////////////////////////////////////

/** What POST /sync is told about a conversation: the highest message id this device holds (null = none). */
data class ConversationCursor(
    val remoteId: Long,
    val newestMessageRemoteId: Long?
)

/** An unsent outbox row, joined to the conversation's server-side id (the only id the server knows). */
data class PendingOutgoing(
    val id: Long,
    val clientMessageId: String,
    val conversationRemoteId: Long,
    val textMessage: String
)

/**
 * A conversation exactly as the server describes it, flattened to what Room stores.
 * [isDm] rather than a kind string so the "chat" (client) vs "dm" (server) spelling
 * is mapped once, at the network boundary, and never leaks into here.
 */
data class ServerConversation(
    val remoteId: Long,
    val isDm: Boolean,
    val name: String?,
    val members: List<Long>,
    val writable: Boolean
)

/** A message exactly as the server stores it. [createdAtMs] is the server's clock, not the sender's. */
data class ServerMessage(
    val remoteId: Long,
    val senderSteamId: Long,
    val clientMessageId: String,
    val body: String,
    val createdAtMs: Long
)
