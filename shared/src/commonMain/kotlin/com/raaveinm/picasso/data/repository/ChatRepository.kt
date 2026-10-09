package com.raaveinm.picasso.data.repository

//
// Created by Kirill "Raaveinm" on 9/4/26.
//

import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.database.entities.chat.MessageData
import com.raaveinm.core.model.chat.MessageStatus
import com.raaveinm.picasso.data.server.ServerContextSource
import com.raaveinm.picasso.data.server.ApiResult
import com.raaveinm.picasso.data.server.ChatAck
import com.raaveinm.picasso.data.server.ChatEnvelope
import com.raaveinm.picasso.data.server.ChatFrameType
import com.raaveinm.picasso.data.server.ChatNack
import com.raaveinm.picasso.data.server.ChatTransport
import com.raaveinm.picasso.data.server.ConversationsApi
import com.raaveinm.picasso.data.server.IncomingChat
import com.raaveinm.picasso.data.server.MessageDeleted
import com.raaveinm.picasso.data.server.OutgoingChat
import com.raaveinm.picasso.data.server.PERMANENT_NACK_CODES
import com.raaveinm.picasso.data.server.SYNC_PAGE_SIZE
import com.raaveinm.picasso.data.server.ServerContext
import com.raaveinm.picasso.data.server.SyncCursor
import com.raaveinm.picasso.data.server.SyncRequest
import com.raaveinm.picasso.data.server.WireConversation
import com.raaveinm.picasso.data.server.toServerConversation
import com.raaveinm.picasso.data.server.toServerMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Something the user should hear about that did not come from a call they are waiting on. */
sealed interface ChatEvent {
    /** The server refused a send for good; the message is now FAILED. [code] is the server's reason. */
    data class SendRejected(val code: String) : ChatEvent
}

/** What came of an attempt to open a conversation. */
sealed interface ConversationResult {
    /** The conversation is in Room now; [localId] is what the screens navigate by. */
    data class Ready(val localId: Long) : ConversationResult
    /** 403: not allies (stranger, removed, blocked and unknown are deliberately one answer). */
    data object NotAllowed : ConversationResult
    /** Invalid input, or the server refused for a reason the user can fix (e.g. a bad palette name). */
    data class Invalid(val message: String?) : ConversationResult
    data object NoServer : ConversationResult
    /** Network failure - nothing was decided, trying again is safe. */
    data object Offline : ConversationResult
    data class Failed(val status: Int, val message: String?) : ConversationResult
}

/**
 * Chat, with the server as the single source of truth and Room as the cache the screens
 * observe. Same rule as the other repositories: nothing here returns messages, it only
 * writes into Room and the DAO flows re-emit.
 *
 * Sending is an outbox (chat-sync-contract.md): [sendMessage] durably inserts a PENDING
 * row first - that is what makes the message appear - and only then offers it to the
 * socket. A row leaves PENDING in exactly two ways: the server acknowledges it
 * ([onAck], or the next sync carrying it back), or refuses it for good ([onNack]).
 * Being offline is NOT a failure: the row simply waits and is resent after the next
 * reconnect, which is safe because the server dedups on the clientMessageId.
 */
class ChatRepository(
    private val chatDao: ChatDao,
    private val serverDao: ServerDao,
    private val activeServer: ServerContextSource,
    private val conversationsApi: ConversationsApi,
    private val socket: ChatTransport
) {
    private val _events = MutableSharedFlow<ChatEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<ChatEvent> = _events

    ///////////////////////////////////////////////
    // Sending
    ///////////////////////////////////////////////

    /**
     * [conversationId] is the LOCAL id (what the screens hold). [senderSteamId] is only used
     * to author the local row - it is never sent: the server stamps the sender from the
     * authenticated connection.
     */
    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    suspend fun sendMessage(conversationId: Long, senderSteamId: Long, text: String): Long {
        chatDao.ensureUsers(listOf(senderSteamId))
        val localId = chatDao.insertMessage(
            MessageData(
                conversationId = conversationId,
                senderSteamId = senderSteamId,
                textMessage = text,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                status = MessageStatus.PENDING,
                clientMessageId = Uuid.random().toString()
            )
        )
        chatDao.refreshLastMessage(conversationId)
        attemptSend(localId)
        return localId
    }

    suspend fun attemptSend(localMessageId: Long) {
        val row = chatDao.getMessage(localMessageId) ?: return
        if (row.status != MessageStatus.PENDING || row.remoteId != null) return
        val conversationRemoteId = chatDao.remoteIdOf(row.conversationId) ?: return
        socket.send(
            ChatEnvelope(
                type = ChatFrameType.CHAT_MESSAGE,
                chatMessage = OutgoingChat(
                    conversationId = conversationRemoteId.toString(),
                    clientMessageId = row.clientMessageId,
                    body = row.textMessage
                )
            )
        )
    }

    /** After a reconnect: everything still PENDING goes out again, oldest first. */
    suspend fun resendPending(context: ServerContext) {
        for (pending in chatDao.getPendingOutgoing(context.serverId)) {
            val sent = socket.send(
                ChatEnvelope(
                    type = ChatFrameType.CHAT_MESSAGE,
                    chatMessage = OutgoingChat(
                        conversationId = pending.conversationRemoteId.toString(),
                        clientMessageId = pending.clientMessageId,
                        body = pending.textMessage
                    )
                )
            )
            if (!sent) return
        }
    }

    /** The user's "try again" on a FAILED message. */
    suspend fun retryFailed(localMessageId: Long) {
        chatDao.requeueFailed(localMessageId)
        attemptSend(localMessageId)
    }

    /**
     * Deletes a message everywhere. One that never reached the server is just dropped
     * locally; a confirmed one is deleted on the server first (sender only) and removed
     * locally only once the server agrees - the other devices learn it from the
     * `message_deleted` frame or the next sync. Returns false if it could not be done.
     */
    suspend fun deleteMessage(localMessageId: Long): Boolean {
        val row = chatDao.getMessage(localMessageId) ?: return true
        val remoteId = row.remoteId
        if (remoteId == null) {
            chatDao.deleteUnsent(localMessageId)
            chatDao.refreshLastMessage(row.conversationId)
            return true
        }
        val context = activeServer.current() ?: return false
        val conversationRemoteId = chatDao.remoteIdOf(row.conversationId) ?: return false
        return when (conversationsApi.deleteMessage(context, conversationRemoteId, remoteId)) {
            is ApiResult.Ok -> {
                chatDao.deleteByRemoteId(row.conversationId, remoteId)
                chatDao.refreshLastMessage(row.conversationId)
                true
            }
            else -> false
        }
    }

    ///////////////////////////////////////////////
    // Conversations
    ///////////////////////////////////////////////

    /** Get-or-create the dm with [peerSteamId] on the active server. Needs the two to be mutual allies. */
    suspend fun createDm(peerSteamId: Long): ConversationResult {
        val context = activeServer.current() ?: return ConversationResult.NoServer
        return store(context, conversationsApi.createDm(context, peerSteamId))
    }

    /** Creates a palette; everyone in [inviteSteamIds] gets a pending invitation (they must accept). */
    suspend fun createPalette(name: String, inviteSteamIds: List<Long>): ConversationResult {
        val context = activeServer.current() ?: return ConversationResult.NoServer
        return store(context, conversationsApi.createPalette(context, name, inviteSteamIds))
    }

    /** Any member may invite one of their own allies. The invitee becomes a member only after accepting. */
    suspend fun invite(conversationId: Long, steamId: Long): ConversationResult {
        val context = activeServer.current() ?: return ConversationResult.NoServer
        val remoteId = chatDao.remoteIdOf(conversationId) ?: return ConversationResult.Invalid("unknown conversation")
        return store(context, conversationsApi.invite(context, remoteId, steamId))
    }

    /** Accepts a pending palette invitation. */
    suspend fun acceptPaletteInvite(conversationRemoteId: Long): ConversationResult {
        val context = activeServer.current() ?: return ConversationResult.NoServer
        return store(context, conversationsApi.acceptInvite(context, conversationRemoteId))
    }

    private suspend fun store(context: ServerContext, result: ApiResult<WireConversation>): ConversationResult =
        when (result) {
            is ApiResult.Ok -> {
                val conversation = result.value.toServerConversation()
                if (conversation == null) {
                    ConversationResult.Failed(result.status, "unrecognised conversation from the server")
                } else {
                    ConversationResult.Ready(
                        chatDao.upsertServerConversation(context.serverId, conversation, context.selfSteamId)
                    )
                }
            }
            is ApiResult.Rejected -> when (result.status) {
                403 -> ConversationResult.NotAllowed
                404, 422 -> ConversationResult.Invalid(result.message)
                else -> ConversationResult.Failed(result.status, result.message)
            }
            is ApiResult.Unavailable -> ConversationResult.Offline
        }

    ///////////////////////////////////////////////
    // Sync
    ///////////////////////////////////////////////

    /**
     * POST /sync: tells the server what this device holds (the highest message id per
     * conversation) and folds the answer into Room - new and removed conversations, the
     * messages missed while offline, and silent deletions. `delta` appends; `reset` (first
     * load, or too much missed) replaces the cached confirmed range with the newest page.
     *
     * Returns false if the server could not be reached or refused, leaving Room as it was.
     */
    suspend fun sync(context: ServerContext): Boolean {
        val cursors = chatDao.getCursors(context.serverId)
            // a conversation with nothing confirmed is simply left out: the server treats it as unknown to us
            .filter { it.newestMessageRemoteId != null }
            .map { SyncCursor(it.remoteId.toString(), it.newestMessageRemoteId.toString()) }
        val request = SyncRequest(
            cursors = cursors,
            deletedSince = serverDao.getDeletedCursor(context.serverId)?.toString(),
            limit = SYNC_PAGE_SIZE
        )

        val response = when (val result = conversationsApi.sync(context, request)) {
            is ApiResult.Ok -> result.value
            is ApiResult.Rejected, is ApiResult.Unavailable -> return false
        }

        val keep = mutableListOf<Long>()
        for (entry in response.conversations) {
            val conversation = entry.conversation.toServerConversation() ?: continue
            keep += conversation.remoteId
            val localId = chatDao.upsertServerConversation(context.serverId, conversation, context.selfSteamId)
            val messages = entry.messages.mapNotNull { it.toServerMessage() }
            if (entry.mode == "reset") {
                chatDao.resetConversationMessages(localId, messages, historyExhausted = !entry.hasMoreBefore)
            } else {
                chatDao.applyServerMessages(localId, messages)
            }
        }
        // Whatever the server no longer lists is gone for this user (left, removed, deleted).
        chatDao.deleteConversationsExcept(context.serverId, keep)

        for (deleted in response.deleted) {
            val conversationRemoteId = deleted.conversationId.toLongOrNull() ?: continue
            val messageRemoteId = deleted.messageId.toLongOrNull() ?: continue
            val localId = chatDao.localIdOf(context.serverId, conversationRemoteId) ?: continue
            chatDao.deleteByRemoteId(localId, messageRemoteId)
            chatDao.refreshLastMessage(localId)
        }
        response.deletedCursor?.toLongOrNull()?.let { serverDao.updateDeletedCursor(context.serverId, it) }
        return true
    }

    /**
     * Scroll-up past what is cached: fetches the page just older than the oldest confirmed
     * message. When the server says there is nothing older the conversation is marked
     * exhausted and scroll-up stops asking. A failed fetch changes nothing, so the next
     * scroll simply asks again. Returns false if nothing was added because of a failure.
     */
    suspend fun loadOlder(conversationId: Long): Boolean {
        if (chatDao.isHistoryExhausted(conversationId) == true) return true
        val context = activeServer.current() ?: return false
        val conversationRemoteId = chatDao.remoteIdOf(conversationId) ?: return false
        val before = chatDao.oldestRemoteId(conversationId) ?: return false

        return when (val result = conversationsApi.history(context, conversationRemoteId, before)) {
            is ApiResult.Ok -> {
                chatDao.applyServerMessages(conversationId, result.value.messages.mapNotNull { it.toServerMessage() })
                chatDao.setHistoryExhausted(conversationId, !result.value.hasMoreBefore)
                true
            }
            is ApiResult.Rejected, is ApiResult.Unavailable -> false
        }
    }

    ///////////////////////////////////////////////
    // Live frames
    ///////////////////////////////////////////////

    /** The server stored our message. Resolves the PENDING row (or inserts it, if the row is somehow gone). */
    suspend fun onAck(context: ServerContext, ack: ChatAck) = applyMessage(context, ack.message.conversationId, ack.message)

    /** Someone else's message, or this user's message sent from another device. */
    suspend fun onIncoming(context: ServerContext, incoming: IncomingChat) =
        applyMessage(context, incoming.message.conversationId, incoming.message)

    private suspend fun applyMessage(
        context: ServerContext,
        conversationRemoteId: String,
        wire: com.raaveinm.picasso.data.server.WireMessage
    ) {
        val message = wire.toServerMessage() ?: return
        val remoteId = conversationRemoteId.toLongOrNull() ?: return
        var localId = chatDao.localIdOf(context.serverId, remoteId)
        if (localId == null) {
            // A message for a conversation we have not heard of: a full sync is the one reliable way to learn it.
            sync(context)
            localId = chatDao.localIdOf(context.serverId, remoteId) ?: return
        }
        chatDao.applyServerMessages(localId, listOf(message))
    }

    /** The server refused a send. A permanent refusal fails the row; a transient one leaves it PENDING for a retry. */
    suspend fun onNack(nack: ChatNack) {
        if (nack.code !in PERMANENT_NACK_CODES) return
        chatDao.updateStatusByClientId(nack.clientMessageId, MessageStatus.FAILED)
        _events.tryEmit(ChatEvent.SendRejected(nack.code))
    }

    suspend fun onDeleted(context: ServerContext, deleted: MessageDeleted) {
        val conversationRemoteId = deleted.conversationId.toLongOrNull() ?: return
        val messageRemoteId = deleted.messageId.toLongOrNull() ?: return
        val localId = chatDao.localIdOf(context.serverId, conversationRemoteId) ?: return
        chatDao.deleteByRemoteId(localId, messageRemoteId)
        chatDao.refreshLastMessage(localId)
    }

    suspend fun onConversation(context: ServerContext, wire: WireConversation) {
        val conversation = wire.toServerConversation() ?: return
        chatDao.upsertServerConversation(context.serverId, conversation, context.selfSteamId)
    }
}
