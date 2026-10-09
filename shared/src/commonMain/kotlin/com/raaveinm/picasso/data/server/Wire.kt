package com.raaveinm.picasso.data.server

import com.raaveinm.core.database.entities.chat.ServerConversation
import com.raaveinm.core.database.entities.chat.ServerMessage
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 10/9/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/*
 * The wire shapes of picassobackend (brainstorm/chat-sync-contract.md). Field names ARE
 * the JSON contract. Every id is a STRING on the wire - SteamIDs (~7.6e16) don't fit a
 * double, and one rule beats two - and every time is epoch MILLISECONDS.
 *
 * These are transport types only. They are converted to the Room-facing shapes
 * (ServerConversation / ServerMessage) at this boundary, which is also the one place
 * the server's "dm" is spelled "chat" for the client.
 */

///////////////////////////////////////////////
// Conversations and messages
///////////////////////////////////////////////

@Serializable
data class WireConversation(
    val id: String,
    val kind: String,                       // "dm" | "palette"
    val name: String? = null,               // Null for a dm
    val members: List<String> = emptyList(),
    val invited: List<String> = emptyList(),// Pending invitees (palette only). Not members
    val writable: Boolean = true,           // false = a frozen dm: readable, but sends are rejected
    val createdAt: Long = 0
)

@Serializable
data class WireMessage(
    val id: String,
    val conversationId: String,
    val senderSteamId: String,
    val clientMessageId: String,
    val body: String,
    val createdAt: Long
)

/** Null when the server sent something this build can't represent - skipped rather than guessed at. */
fun WireConversation.toServerConversation(): ServerConversation? {
    val remoteId = id.toLongOrNull() ?: return null
    val isDm = when (kind) {
        "dm" -> true
        "palette" -> false
        else -> return null
    }
    return ServerConversation(
        remoteId = remoteId,
        isDm = isDm,
        name = name,
        members = members.mapNotNull { it.toLongOrNull() },
        writable = writable
    )
}

fun WireMessage.toServerMessage(): ServerMessage? {
    val remoteId = id.toLongOrNull() ?: return null
    val sender = senderSteamId.toLongOrNull() ?: return null
    return ServerMessage(
        remoteId = remoteId,
        senderSteamId = sender,
        clientMessageId = clientMessageId,
        body = body,
        createdAtMs = createdAt
    )
}

///////////////////////////////////////////////
// REST bodies
///////////////////////////////////////////////

/** POST /conversations. A dm uses [peerSteamId]; a palette uses [name] and [inviteSteamIds]. */
@Serializable
data class CreateConversationRequest(
    val kind: String,
    val peerSteamId: String? = null,
    val name: String? = null,
    val inviteSteamIds: List<String>? = null
)

@Serializable
data class SteamIdBody(val steamId: String)

@Serializable
data class LevelBody(val level: String)

///////////////////////////////////////////////
// Contacts
///////////////////////////////////////////////

@Serializable
data class WireContact(val steamId: String, val level: String, val since: Long = 0)

/** For an incoming request [steamId] is the sender, for an outgoing one the target. */
@Serializable
data class WireRequest(val steamId: String, val createdAt: Long = 0)

@Serializable
data class WireInvite(
    val conversationId: String,
    val name: String = "",
    val inviterSteamId: String,
    val createdAt: Long = 0
)

@Serializable
data class WireContacts(
    val contacts: List<WireContact> = emptyList(),
    val incoming: List<WireRequest> = emptyList(),
    val outgoing: List<WireRequest> = emptyList(),
    val paletteInvites: List<WireInvite> = emptyList()
)

///////////////////////////////////////////////
// Sync
///////////////////////////////////////////////

/** What this device holds: [after] is the highest message id it has for [conversationId]. */
@Serializable
data class SyncCursor(val conversationId: String, val after: String)

@Serializable
data class SyncRequest(
    val cursors: List<SyncCursor>,
    val deletedSince: String? = null,
    val limit: Int = SYNC_PAGE_SIZE
)

@Serializable
data class SyncConversation(
    val conversation: WireConversation,
    val mode: String,
    val hasMoreBefore: Boolean = false,
    val messages: List<WireMessage> = emptyList()
)

@Serializable
data class SyncResponse(
    val serverTime: String = "0",
    val deletedCursor: String? = null,
    val conversations: List<SyncConversation> = emptyList(),
    val deleted: List<MessageDeleted> = emptyList()
)

@Serializable
data class HistoryPage(
    val messages: List<WireMessage> = emptyList(),
    val hasMoreBefore: Boolean = false
)

const val SYNC_PAGE_SIZE = 100

///////////////////////////////////////////////
// WebSocket frames
///////////////////////////////////////////////

/**
 * The chat half of the server's `EnvelopeDto`: `type` says which one payload field is
 * set. The RTC fields (sdp, iceCandidate, callHangup) are decoded separately by
 * `SignalingClient` from the same socket, so they are absent here and ignored on read.
 */
@Serializable
data class ChatEnvelope(
    val type: String,
    val chatMessage: OutgoingChat? = null,
    val chatAck: ChatAck? = null,
    val chatNack: ChatNack? = null,
    val chatMessageOut: IncomingChat? = null,
    val messageDeleted: MessageDeleted? = null,
    val contactRequest: WireRequest? = null,
    val contactUpdated: ContactUpdated? = null,
    val paletteInvite: PaletteInviteEvent? = null,
    val conversation: WireConversation? = null
)

/** client -> server. There is deliberately no sender: the server stamps it from the connection. */
@Serializable
data class OutgoingChat(val conversationId: String, val clientMessageId: String, val body: String)

/** server -> the originating socket only. [duplicate] = this was a retry of a message already stored. */
@Serializable
data class ChatAck(
    val conversationId: String? = null,
    val clientMessageId: String,
    val message: WireMessage,
    val duplicate: Boolean = false
)

@Serializable
data class ChatNack(
    val conversationId: String? = null,
    val clientMessageId: String,
    /** not_member | not_allowed | too_long | invalid (permanent) or rate_limited | internal (retry). */
    val code: String
)

/** server -> every OTHER socket of every member, including the sender's other devices. */
@Serializable
data class IncomingChat(val message: WireMessage)

@Serializable
data class MessageDeleted(val conversationId: String, val messageId: String)

/** [steamId] is the OTHER person; [level] null = stranger (also how a block looks to the blocked). */
@Serializable
data class ContactUpdated(val steamId: String, val level: String? = null)

/** [state] is "pending", or "resolved" once accepted/declined on another device. */
@Serializable
data class PaletteInviteEvent(
    val conversationId: String,
    val name: String = "",
    val inviterSteamId: String = "",
    val createdAt: Long = 0,
    val state: String = "pending"
)

object ChatFrameType {
    const val CHAT_MESSAGE = "chat_message"
    const val CHAT_ACK = "chat_ack"
    const val CHAT_NACK = "chat_nack"
    const val CHAT_MESSAGE_OUT = "chat_message_out"
    const val MESSAGE_DELETED = "message_deleted"
    const val CONVERSATION_ADDED = "conversation_added"
    const val CONVERSATION_UPDATED = "conversation_updated"
    const val CONTACT_REQUEST = "contact_request"
    const val CONTACT_UPDATED = "contact_updated"
    const val PALETTE_INVITE = "palette_invite"
}

val PERMANENT_NACK_CODES = setOf("not_member", "not_allowed", "too_long", "invalid")
