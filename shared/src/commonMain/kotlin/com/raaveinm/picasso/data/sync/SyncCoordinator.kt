package com.raaveinm.picasso.data.sync

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.ContactsRepository
import com.raaveinm.picasso.data.repository.ProfileHydrator
import com.raaveinm.picasso.data.server.ServerContextSource
import com.raaveinm.picasso.data.server.ChatEnvelope
import com.raaveinm.picasso.data.server.ChatFrameType
import com.raaveinm.picasso.data.server.ChatTransport
import com.raaveinm.picasso.data.server.ServerContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds

enum class ConnectionState {
    /** Signed out, no server, or waiting to retry. */
    DISCONNECTED,
    /** Dialing the socket. */
    CONNECTING,
    /** Connected; catching up with the server (sync, contacts, resend). */
    SYNCING,
    /** Caught up; live frames flow. */
    LIVE
}

class SyncCoordinator(
    private val activeServer: ServerContextSource,
    private val socket: ChatTransport,
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
    private val profiles: ProfileHydrator,
    private val onNothingToConnect: () -> Unit = {},
    private val isSessionRejected: suspend (ServerContext) -> Boolean = { false }, // asks the server whether a failed connect was a 401
    private val onSessionRejected: suspend (ServerContext) -> Unit = {},           // forgets that context's session, which makes the context go null
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    val state: StateFlow<ConnectionState> = _state

    /* The server a connection loop is running against, null once it has fully stopped. */
    private val serving = MutableStateFlow<ServerContext?>(null)

    private val _sessionRejected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emits when the server refused the stored session and it was dropped (the user is signed out). */
    val sessionRejected: SharedFlow<Unit> = _sessionRejected

    private var job: Job? = null

    /** Idempotent. Called once from the app shell; safe to call again. */
    fun start() {
        if (job != null) return
        job = scope.launch {
            activeServer.context.collectLatest { context ->
                if (context == null) {
                    _state.value = ConnectionState.DISCONNECTED
                    onNothingToConnect()
                } else {
                    serving.value = context
                    try {
                        keepConnected(context)
                    } finally {
                        socket.close()
                        _state.value = ConnectionState.DISCONNECTED
                        serving.value = null
                    }
                }
            }
        }
    }

    /**
     * Suspends until no connection loop is running - after a sign-out, that is the point
     * from which nothing this coordinator started can still write into Room.
     */
    suspend fun awaitIdle() {
        serving.first { it == null }
    }

    private suspend fun keepConnected(context: ServerContext) {
        var backoff = INITIAL_BACKOFF_SECONDS
        while (true) {
            try {
                runConnection(context)
                backoff = INITIAL_BACKOFF_SECONDS
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("SyncCoordinator: connection to ${context.baseUrl} failed: $e")
                /*
                 * A refused session never heals by retrying (the server lost or revoked it),
                 * so stop and sign out. The WS handshake error doesn't carry the status in a
                 * usable form, hence the separate REST probe; an unreachable server answers
                 * false here and keeps the ordinary backoff.
                 */
                if (dropRejectedSession(context)) return
            }
            _state.value = ConnectionState.DISCONNECTED
            delay(backoff.seconds)
            backoff = (backoff * 2).coerceAtMost(MAX_BACKOFF_SECONDS)
        }
    }

    /* True once the session was found rejected and forgotten; any failure on the way leaves it alone. */
    private suspend fun dropRejectedSession(context: ServerContext): Boolean = try {
        if (isSessionRejected(context)) {
            println("SyncCoordinator: ${context.baseUrl} rejected the session, signing out")
            // Before forgetting it: the cleared session cancels this very coroutine.
            _sessionRejected.tryEmit(Unit)
            onSessionRejected(context)
            true
        } else {
            false
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        println("SyncCoordinator: could not drop the rejected session: $e")
        false
    }

    private suspend fun runConnection(context: ServerContext) = coroutineScope {
        val gate = Mutex()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            socket.frames.collect { envelope ->
                gate.withLock { dispatch(context, envelope) }
            }
        }
        socket.awaitSubscriber()

        _state.value = ConnectionState.CONNECTING
        socket.connect(context)

        _state.value = ConnectionState.SYNCING
        gate.withLock {
            val synced = chatRepository.sync(context)
            contactsRepository.refresh(context)
            profiles.hydrateStubs()
            if (!synced) println("SyncCoordinator: sync did not complete; continuing with live frames only")
        }
        chatRepository.resendPending(context)
        _state.value = ConnectionState.LIVE

        socket.isConnected.first { connected -> !connected }
        collector.cancel()
    }

    private suspend fun dispatch(context: ServerContext, envelope: ChatEnvelope) {
        try {
            when (envelope.type) {
                ChatFrameType.CHAT_ACK -> envelope.chatAck?.let { chatRepository.onAck(context, it) }
                ChatFrameType.CHAT_NACK -> envelope.chatNack?.let { chatRepository.onNack(it) }
                ChatFrameType.CHAT_MESSAGE_OUT -> envelope.chatMessageOut?.let { chatRepository.onIncoming(context, it) }
                ChatFrameType.MESSAGE_DELETED -> envelope.messageDeleted?.let { chatRepository.onDeleted(context, it) }
                ChatFrameType.CONVERSATION_ADDED,
                ChatFrameType.CONVERSATION_UPDATED -> envelope.conversation?.let {
                    chatRepository.onConversation(context, it)
                    profiles.hydrateStubs()
                }
                ChatFrameType.CONTACT_REQUEST,
                ChatFrameType.CONTACT_UPDATED,
                ChatFrameType.PALETTE_INVITE -> {
                    contactsRepository.onFrame(context, envelope)
                    profiles.hydrateStubs()
                }
                // RTC frames (sdp_*, ice_candidate, call_hangup) belong to CallManager, which reads the same socket.
                else -> Unit
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("SyncCoordinator: dropped frame '${envelope.type}': $e")
        }
    }

    private companion object {
        const val INITIAL_BACKOFF_SECONDS = 2
        const val MAX_BACKOFF_SECONDS = 30
    }
}
