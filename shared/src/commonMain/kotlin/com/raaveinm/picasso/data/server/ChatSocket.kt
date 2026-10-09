package com.raaveinm.picasso.data.server

import com.raaveinm.features.impl_webrtc.signaling.SignalingClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json

//
// Created by Kirill "Raaveinm" on 10/9/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/**
 * What the sync layer needs from the socket. An interface so the reconnect procedure and
 * the outbox can be driven by a scripted transport in tests.
 */
interface ChatTransport {
    val isConnected: StateFlow<Boolean>

    /** Every well-formed envelope received; RTC frames decode too and are dropped by `type`. */
    val frames: Flow<ChatEnvelope>

    /** Suspends until something is collecting [frames], so a caller can subscribe BEFORE connecting. */
    suspend fun awaitSubscriber()

    /** Throws if the handshake fails; a no-op when already connected. */
    suspend fun connect(context: ServerContext)

    /**
     * Sends one frame. False means "not delivered" - no live socket, or the write failed - and
     * is not an error: the outbox keeps the message PENDING and the sync layer resends after
     * the next reconnect.
     */
    suspend fun send(envelope: ChatEnvelope): Boolean

    fun close()
}

/**
 * Chat's view of the one WebSocket a client keeps per server. The socket itself is
 * [SignalingClient]'s - calls and chat share it - this just speaks the chat half of
 * the envelope over it.
 */
class ChatSocket(private val signaling: SignalingClient) : ChatTransport {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override val isConnected: StateFlow<Boolean> = signaling.isConnected

    override val frames: Flow<ChatEnvelope> = signaling.rawFrames.mapNotNull { text ->
        runCatching { json.decodeFromString<ChatEnvelope>(text) }.getOrNull()
    }

    override suspend fun awaitSubscriber() {
        signaling.rawSubscriberCount.first { it > 0 }
    }

    override suspend fun connect(context: ServerContext) = signaling.connect(context.token, context.wsUrl)

    override suspend fun send(envelope: ChatEnvelope): Boolean = signaling.sendText(json.encodeToString(envelope))

    override fun close() = signaling.close()
}
