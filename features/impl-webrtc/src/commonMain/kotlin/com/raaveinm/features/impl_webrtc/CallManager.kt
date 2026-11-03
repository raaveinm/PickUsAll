package com.raaveinm.features.impl_webrtc

import com.raaveinm.features.impl_webrtc.rtc.CallConnectionState
import com.raaveinm.features.impl_webrtc.rtc.CallPeer
import com.raaveinm.features.impl_webrtc.rtc.WebRtcCallPeer
import com.raaveinm.features.impl_webrtc.signaling.CallSignaling
import com.raaveinm.features.impl_webrtc.signaling.HangupPayload
import com.raaveinm.features.impl_webrtc.signaling.IceCandidatePayload
import com.raaveinm.features.impl_webrtc.signaling.IceCandidateWireData
import com.raaveinm.features.impl_webrtc.signaling.SdpPayload
import com.raaveinm.features.impl_webrtc.signaling.SignalingEnvelope
import com.raaveinm.features.impl_webrtc.signaling.SignalingMessageType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val MAX_BUFFERED_ICE_CANDIDATES: Int = 256
private val INCOMING_CALL_TIMEOUT: Duration = 45.seconds

/** Owns one pending or active DM call. Incoming offers require consent before acquiring media. */
class CallManager(
    private val signalingClient: CallSignaling,
    private val webRtcClientFactory: () -> CallPeer = { WebRtcCallPeer() },
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default +
            CoroutineExceptionHandler { _, error -> println("CallManager: unhandled error: $error") },
    ),
) {
    private val json: Json = Json { ignoreUnknownKeys = true }
    private val mutex: Mutex = Mutex()
    private var nextCallId: Long = 0L
    private var session: CallSession? = null

    private val _incomingCall: MutableStateFlow<IncomingCall?> = MutableStateFlow(null)
    val incomingCall: StateFlow<IncomingCall?> = _incomingCall.asStateFlow()
    private val _isInCall: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInCall: StateFlow<Boolean> = _isInCall.asStateFlow()

    // All session mutations use mutex. Media setup runs outside it so hangup can cancel setup.
    private class CallSession(
        val identity: IncomingCall,
        val offerSdp: String?,
        val scope: CoroutineScope,
    ) {
        var peer: CallPeer? = null
        var remoteDescriptionReady: Boolean = false
        var answerReceived: Boolean = false
        val candidates: ArrayDeque<IceCandidateWireData> = ArrayDeque()
    }

    init {
        signalingClient.incoming
            .onEach { envelope -> mutex.withLock { handleIncoming(envelope) } }
            .launchIn(scope)
        signalingClient.isConnected
            .onEach { connected ->
                if (!connected) mutex.withLock {
                    if (!signalingClient.isConnected.value) teardown()
                }
            }
            .launchIn(scope)
    }

    ///////////////////////////////////////////////
    // User actions
    ///////////////////////////////////////////////

    /** Drops pending consent and any live media when the account or server disconnects. */
    fun disconnectSignaling() {
        signalingClient.close()
        scope.launch { mutex.withLock { teardown() } }
    }

    fun startCall(conversationId: Long, peerSteamId: Long) {
        scope.launch {
            mutex.withLock {
                if (session != null || !signalingClient.isConnected.value) return@withLock
                val current: CallSession = newSession(conversationId, peerSteamId, offerSdp = null)
                session = current
                _isInCall.value = true
                startMedia(current)
            }
        }
    }

    /** Only the still-pending offer with this local ID may acquire the microphone. */
    fun acceptIncomingCall(callId: String) {
        scope.launch {
            mutex.withLock {
                val current: CallSession = session ?: return@withLock
                if (_incomingCall.value?.id != callId) return@withLock
                _incomingCall.value = null
                _isInCall.value = true
                startMedia(current)
            }
        }
    }

    fun declineIncomingCall(callId: String) {
        scope.launch {
            mutex.withLock {
                val current: CallSession = session ?: return@withLock
                if (_incomingCall.value?.id != callId) return@withLock
                sendHangup(current.identity)
                teardown()
            }
        }
    }

    fun endCall() {
        scope.launch {
            mutex.withLock {
                session?.let { sendHangup(it.identity) }
                teardown()
            }
        }
    }

    ///////////////////////////////////////////////
    // Session and media lifetime
    ///////////////////////////////////////////////

    private fun newSession(conversationId: Long, peerSteamId: Long, offerSdp: String?): CallSession {
        nextCallId += 1L
        return CallSession(
            identity = IncomingCall(nextCallId.toString(), conversationId, peerSteamId),
            offerSdp = offerSdp,
            scope = CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job])),
        )
    }

    private fun startMedia(current: CallSession) {
        current.scope.launch {
            try {
                val peer: CallPeer = mutex.withLock {
                    if (session !== current) return@launch
                    webRtcClientFactory().also {
                        current.peer = it
                        observeMedia(current, it)
                    }
                }
                val offerSdp: String? = current.offerSdp
                val localSdp: String = if (offerSdp == null) peer.createOffer() else peer.createAnswer(offerSdp)
                mutex.withLock {
                    if (session !== current) return@launch
                    if (offerSdp != null) remoteDescriptionReady(current)
                }
                signalingClient.send(
                    SignalingEnvelope(
                        type = if (offerSdp == null) SignalingMessageType.SDP_OFFER else SignalingMessageType.SDP_ANSWER,
                        sdp = SdpPayload(
                            conversationId = current.identity.conversationId.toString(),
                            toSteamId = current.identity.peerSteamId.toString(),
                            sdp = localSdp,
                        ),
                    ),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutex.withLock { failSession(current, error) }
            }
        }
    }

    private fun observeMedia(current: CallSession, peer: CallPeer) {
        // Subscribe before creating the SDP: native clients can emit ICE during that operation.
        current.scope.launch(start = CoroutineStart.UNDISPATCHED) {
            peer.localIceCandidates.collect { candidate ->
                try {
                    val isCurrent: Boolean = mutex.withLock { session === current }
                    if (!isCurrent) return@collect
                    signalingClient.send(
                        SignalingEnvelope(
                            type = SignalingMessageType.ICE_CANDIDATE,
                            iceCandidate = IceCandidatePayload(
                                conversationId = current.identity.conversationId.toString(),
                                toSteamId = current.identity.peerSteamId.toString(),
                                candidate = json.encodeToString(
                                    IceCandidateWireData(candidate.sdpMid, candidate.sdpMLineIndex, candidate.candidate),
                                ),
                            ),
                        ),
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    mutex.withLock { failSession(current, error) }
                }
            }
        }
        current.scope.launch(start = CoroutineStart.UNDISPATCHED) {
            peer.connectionState.collect { state ->
                if (state == CallConnectionState.FAILED || state == CallConnectionState.CLOSED) {
                    mutex.withLock {
                        if (session === current) {
                            sendHangup(current.identity)
                            teardown()
                        }
                    }
                }
            }
        }
    }

    private fun teardown() {
        val previous: CallSession? = session
        session = null
        _incomingCall.value = null
        _isInCall.value = false
        previous?.scope?.cancel()
        runCatching { previous?.peer?.close() }
            .onFailure { error -> println("CallManager: media close failed: $error") }
    }

    private fun failSession(current: CallSession, error: Exception) {
        if (session !== current) return
        println("CallManager: media setup failed: $error")
        sendHangup(current.identity)
        teardown()
    }

    private fun sendHangup(identity: IncomingCall) {
        scope.launch {
            try {
                signalingClient.send(
                    SignalingEnvelope(
                        type = SignalingMessageType.CALL_HANGUP,
                        callHangup = HangupPayload(
                            conversationId = identity.conversationId.toString(),
                            toSteamId = identity.peerSteamId.toString(),
                        ),
                    ),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                println("CallManager: hangup could not be sent: $error")
            }
        }
    }

    ///////////////////////////////////////////////
    // Remote signaling (called while holding mutex)
    ///////////////////////////////////////////////

    private fun handleIncoming(envelope: SignalingEnvelope) {
        if (!signalingClient.isConnected.value) return
        when (envelope.type) {
            SignalingMessageType.SDP_OFFER -> handleRemoteOffer(envelope.sdp ?: return)
            SignalingMessageType.SDP_ANSWER -> handleRemoteAnswer(envelope.sdp ?: return)
            SignalingMessageType.ICE_CANDIDATE -> handleRemoteIceCandidate(envelope.iceCandidate ?: return)
            SignalingMessageType.CALL_HANGUP -> {
                val hangup: HangupPayload = envelope.callHangup ?: return
                if (matchesSession(hangup.conversationId, hangup.fromSteamId)) teardown()
            }
        }
    }

    private fun handleRemoteOffer(sdp: SdpPayload) {
        val peerSteamId: Long = sdp.fromSteamId?.toLongOrNull() ?: return
        val conversationId: Long = sdp.conversationId.toLongOrNull() ?: return
        if (sdp.sdp.isBlank()) return
        if (session != null) {
            if (!matchesSession(sdp.conversationId, sdp.fromSteamId)) {
                sendHangup(IncomingCall(id = "", conversationId = conversationId, peerSteamId = peerSteamId))
            }
            return
        }
        val current: CallSession = newSession(conversationId, peerSteamId, sdp.sdp)
        session = current
        _incomingCall.value = current.identity
        current.scope.launch {
            delay(INCOMING_CALL_TIMEOUT)
            mutex.withLock {
                if (session === current && _incomingCall.value?.id == current.identity.id) {
                    sendHangup(current.identity)
                    teardown()
                }
            }
        }
    }

    private fun handleRemoteAnswer(sdp: SdpPayload) {
        val current: CallSession = session ?: return
        val peer: CallPeer = current.peer ?: return
        if (current.offerSdp != null || current.answerReceived || !matchesSession(sdp.conversationId, sdp.fromSteamId)) return
        current.answerReceived = true
        current.scope.launch {
            try {
                peer.setRemoteAnswer(sdp.sdp)
                mutex.withLock {
                    if (session === current) remoteDescriptionReady(current)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutex.withLock { failSession(current, error) }
            }
        }
    }

    private fun handleRemoteIceCandidate(payload: IceCandidatePayload) {
        val current: CallSession = session ?: return
        if (!matchesSession(payload.conversationId, payload.fromSteamId)) return
        val wire: IceCandidateWireData = runCatching {
            json.decodeFromString<IceCandidateWireData>(payload.candidate)
        }.getOrNull() ?: return
        if (!current.remoteDescriptionReady) {
            if (current.candidates.size < MAX_BUFFERED_ICE_CANDIDATES) current.candidates.addLast(wire)
        } else {
            try {
                current.peer?.addRemoteIceCandidate(wire.sdpMid, wire.sdpMLineIndex, wire.candidate)
            } catch (error: Exception) {
                failSession(current, error)
            }
        }
    }

    private fun remoteDescriptionReady(current: CallSession) {
        current.remoteDescriptionReady = true
        while (current.candidates.isNotEmpty()) {
            val wire: IceCandidateWireData = current.candidates.removeFirst()
            current.peer?.addRemoteIceCandidate(wire.sdpMid, wire.sdpMLineIndex, wire.candidate)
        }
    }

    private fun matchesSession(conversationId: String, fromSteamId: String?): Boolean {
        val current: IncomingCall = session?.identity ?: return false
        return conversationId.toLongOrNull() == current.conversationId &&
            fromSteamId?.toLongOrNull() == current.peerSteamId
    }
}
