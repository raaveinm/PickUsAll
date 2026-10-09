package com.raaveinm.features.impl_webrtc.signaling

import io.ktor.client.HttpClient
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/*
 * One long-lived WS connection to PicassoBackend's /ws. It decodes the RTC signaling
 * frames itself ([incoming]) and hands EVERY text frame, undecoded, to [rawFrames] -
 * chat and the social graph ride the same socket (one connection per client per
 * server) but their wire types live in :shared, so this module stays ignorant of them.
 * Engine is per-platform (signalingHttpClientEngine) rather than hardcoded CIO:
 * CIO cannot do TLS at all on Kotlin/Native, so a hardcoded CIO client could
 * never open wss:// on iOS - see SignalingHttpEngine.kt.
 */
class SignalingClient {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val client = HttpClient(signalingHttpClientEngine()) { install(WebSockets) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var session: DefaultClientWebSocketSession? = null
    private val _incoming = MutableSharedFlow<SignalingEnvelope>(extraBufferCapacity = 64)
    val incoming: SharedFlow<SignalingEnvelope> = _incoming

    /*
     * Every text frame as received. Buffered generously and SUSPENDING when full, so a
     * consumer that is busy (a sync in progress) slows the socket reader down instead of
     * losing frames - chat frames must never be dropped.
     */
    private val _rawFrames = MutableSharedFlow<String>(extraBufferCapacity = 256)
    val rawFrames: SharedFlow<String> = _rawFrames

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    /* Lets a consumer be subscribed to [rawFrames] BEFORE it connects, so no early frame is missed. */
    val rawSubscriberCount: StateFlow<Int> get() = _rawFrames.subscriptionCount

    /*
     * Idempotent - a second call while already connected is a no-op, since
     * CallManager may be invoked once per ChatViewModel but should only ever
     * hold one signaling connection per app session.
     *
     * [authToken] is the opaque session token from /auth/steam/poll, NOT the
     * steamId: the backend hashes it and looks up a real session, then derives the
     * sender from that. Sending a steamId here (as this used to, matching an older
     * dev-mode stand-in) is rejected with a 401 on the upgrade.
     */
    suspend fun connect(authToken: String, wsUrl: String) {
        if (session != null) return

        println("SignalingClient.connect: dialing $wsUrl")
        val newSession = client.webSocketSession(urlString = wsUrl) {
            header("Authorization", "Bearer $authToken")
        }
        println("SignalingClient.connect: handshake complete")
        session = newSession
        _isConnected.value = true

        scope.launch {
            try {
                for (frame in newSession.incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        _rawFrames.emit(text)
                        runCatching { json.decodeFromString<SignalingEnvelope>(text) }
                            .onSuccess { _incoming.emit(it) }
                    }
                }
            } finally {
                // Only clear the state that is still ours: a close() followed by a quick
                // reconnect must not have the old reader reset the new session's flag.
                if (session === newSession) {
                    session = null
                    _isConnected.value = false
                }
            }
        }
    }

    suspend fun send(envelope: SignalingEnvelope) {
        val current = session ?: return
        current.send(Frame.Text(json.encodeToString(envelope)))
    }

    /**
     * Sends one already-encoded text frame. Returns false - without throwing - when
     * there is no live socket or the send failed, so a caller that keeps its own outbox
     * (chat) can simply leave the message pending and try again after a reconnect.
     */
    suspend fun sendText(text: String): Boolean {
        val current = session ?: return false
        return try {
            current.send(Frame.Text(text))
            true
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    fun close() {
        val current = session
        session = null
        _isConnected.value = false
        scope.launch { current?.close() }
    }
}
