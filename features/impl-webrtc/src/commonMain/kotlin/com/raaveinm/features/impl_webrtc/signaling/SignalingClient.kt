package com.raaveinm.features.impl_webrtc.signaling

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/*
 * One long-lived WS connection to PicassoBackend's /ws, carrying only RTC
 * signaling frames - chat has its own (still TODO'd) wire path in ChatRepository.
 * CIO is used as the engine because it's the only one available on every KMP
 * target this module builds for (android/ios/jvm), unlike okhttp/darwin.
 */
class SignalingClient {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val client = HttpClient(CIO) { install(WebSockets) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var session: DefaultClientWebSocketSession? = null
    private val _incoming = MutableSharedFlow<SignalingEnvelope>(extraBufferCapacity = 64)
    val incoming: SharedFlow<SignalingEnvelope> = _incoming

    /*
     * Idempotent - a second call while already connected is a no-op, since
     * CallManager may be invoked once per ChatViewModel but should only ever
     * hold one signaling connection per app session.
     */
    suspend fun connect(steamId: Long, wsUrl: String) {
        if (session != null) return

        println("SignalingClient.connect: dialing $wsUrl as $steamId")
        val newSession = client.webSocketSession(urlString = wsUrl) {
            /* Matches the backend's dev-mode auth: the token IS the steamId. */
            header("Authorization", "Bearer $steamId")
        }
        println("SignalingClient.connect: handshake complete")
        session = newSession

        scope.launch {
            try {
                for (frame in newSession.incoming) {
                    if (frame is Frame.Text) {
                        runCatching { json.decodeFromString<SignalingEnvelope>(frame.readText()) }
                            .onSuccess { _incoming.emit(it) }
                    }
                }
            } finally {
                session = null
            }
        }
    }

    suspend fun send(envelope: SignalingEnvelope) {
        val current = session ?: return
        current.send(Frame.Text(json.encodeToString(envelope)))
    }

    fun close() {
        val current = session
        session = null
        scope.launch { current?.close() }
    }
}
