package com.raaveinm.picasso.data.repository

//
// Created by Kirill "Raaveinm" on 9/4/26.
//

import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.entities.chat.MessageData
import com.raaveinm.core.model.chat.MessageStatus
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Outbox pattern - the durable Room write happens first
class ChatRepository(
    private val chatDao: ChatDao
) {
    @OptIn(ExperimentalTime::class)
    suspend fun sendMessage(conversationId: Long, senderSteamId: Long, text: String): Long {
        val localId = chatDao.insertMessage(
            MessageData(
                conversationId = conversationId,
                senderSteamId = senderSteamId,
                textMessage = text,
                timestamp = Clock.System.now().epochSeconds,
                status = MessageStatus.PENDING
            )
        )
        attemptSend(localId)
        return localId
    }

    // Also the entry point a future retry mechanism (reconnect-triggered, or a
    // periodic sweep over still-PENDING rows) would call for a message that's
    // already durably in Room - no re-insert needed, just another delivery attempt.
    suspend fun attemptSend(localMessageId: Long) {
        try {
            sendToServer(localMessageId)
            chatDao.updateMessageStatus(localMessageId, MessageStatus.SENT)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            // Throwable, not Exception: sendToServer() is currently a TODO() stub,
            // which throws NotImplementedError - an Error, not an Exception. Catching
            // only Exception here would let that crash the app instead of correctly
            // landing the message as FAILED (true right now: nothing was sent).
            chatDao.updateMessageStatus(localMessageId, MessageStatus.FAILED)
        }
    }

    // TODO(): no chat wire protocol yet - features/chat and picassobackend aren't
    // built (see pipeline.md). Once the WS client exists, this should:
    //  - resolve conversationId -> the owning server + Conversations.remoteId, to
    //    know where to send (see Conversations.remoteId in Database docs/CLAUDE.md)
    //  - send (remoteId, localMessageId as an idempotency key, text) so a retried
    //    send after a lost ack doesn't create a server-side duplicate
    //  - NOT send senderSteamId as data to be trusted - the server must derive the
    //    sender from the authenticated connection/session (see decisions.md)
    private suspend fun sendToServer(localMessageId: Long): Unit = TODO(
        "chat network protocol not implemented yet - see features/chat in pipeline.md"
    )
}
