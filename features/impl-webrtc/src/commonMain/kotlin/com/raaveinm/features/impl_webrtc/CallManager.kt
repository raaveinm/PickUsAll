package com.raaveinm.features.impl_webrtc

import com.raaveinm.features.impl_webrtc.rtc.LocalIceCandidateData
import com.raaveinm.features.impl_webrtc.rtc.WebRtcCallClient
import com.raaveinm.features.impl_webrtc.signaling.HangupPayload
import com.raaveinm.features.impl_webrtc.signaling.IceCandidatePayload
import com.raaveinm.features.impl_webrtc.signaling.IceCandidateWireData
import com.raaveinm.features.impl_webrtc.signaling.SdpPayload
import com.raaveinm.features.impl_webrtc.signaling.SignalingClient
import com.raaveinm.features.impl_webrtc.signaling.SignalingEnvelope
import com.raaveinm.features.impl_webrtc.signaling.SignalingMessageType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*
 * Orchestrates the pipeline: both clients connect over the same signaling
 * WebSocket, an offer/answer exchange happens by steamId, ICE candidates
 * trickle both ways, and the resulting PeerConnection is the P2P audio call -
 * the server never sees anything past this class. Holds at most one active
 * call at a time (mesh-not-SFU design, 1:1 DM calling only - see brainstorm/decisions.md).
 */
class CallManager(
    private val signalingClient: SignalingClient,
    private val webRtcClientFactory: () -> WebRtcCallClient = { WebRtcCallClient() }
) {
    /*
     * The handler is a blanket safety net, not just for connectSignaling: a dead
     * signaling socket mid-call, a denied mic permission, etc. must degrade calling
     * only - not take the whole app down with an uncaught coroutine exception.
     */
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default +
            CoroutineExceptionHandler { _, e -> println("CallManager: unhandled error: $e") }
    )
    private val json = Json { ignoreUnknownKeys = true }

    private var activeCall: WebRtcCallClient? = null
    private var activeConversationId: Long? = null
    private var activePeerSteamId: Long? = null
    private var signalingConnected = false

    private val _isInCall = MutableStateFlow(false)
    val isInCall: StateFlow<Boolean> = _isInCall

    /*
     * Idempotent - safe to call once per ChatViewModel even across recompositions.
     * Fire-and-forget by design: a caller (e.g. ChatViewModel.init) must be able to
     * kick this off without the rest of the app depending on - or crashing from -
     * whether the signaling server happens to be reachable right now.
     */
    fun connectSignaling(steamId: Long, wsUrl: String) {
        if (signalingConnected) return
        signalingConnected = true
        scope.launch {
            try {
                signalingClient.connect(steamId, wsUrl)
                signalingClient.incoming
                    .onEach(::handleIncoming)
                    .launchIn(scope)
            } catch (e: Exception) {
                signalingConnected = false
                println("CallManager.connectSignaling failed: $e")
            }
        }
    }

    /* Caller/offer path. */
    fun startCall(conversationId: Long, peerSteamId: Long) {
        if (activeCall != null) return

        activeConversationId = conversationId
        activePeerSteamId = peerSteamId
        val call = webRtcClientFactory()
        activeCall = call
        _isInCall.value = true
        forwardLocalIceCandidates(call)
        logConnectionState(call)

        scope.launch {
            val offerSdp = call.createOffer()
            println("CallManager: sending sdp_offer to $peerSteamId")
            signalingClient.send(
                SignalingEnvelope(
                    type = SignalingMessageType.SDP_OFFER,
                    sdp = SdpPayload(conversationId.toString(), peerSteamId.toString(), sdp = offerSdp)
                )
            )
        }
    }

    /* Local toggle-off: notifies the peer immediately, then tears down locally. */
    fun endCall() {
        val conversationId = activeConversationId
        val peer = activePeerSteamId
        if (conversationId != null && peer != null) {
            scope.launch {
                signalingClient.send(
                    SignalingEnvelope(
                        type = SignalingMessageType.CALL_HANGUP,
                        callHangup = HangupPayload(conversationId.toString(), peer.toString())
                    )
                )
            }
        }
        teardown()
    }

    private fun teardown() {
        activeCall?.close()
        activeCall = null
        activeConversationId = null
        activePeerSteamId = null
        _isInCall.value = false
    }

    private fun forwardLocalIceCandidates(call: WebRtcCallClient) {
        call.localIceCandidates
            .onEach { sendLocalCandidate(it) }
            .launchIn(scope)
    }

    private fun logConnectionState(call: WebRtcCallClient) {
        call.connectionState
            .onEach { println("CallManager: connectionState = $it") }
            .launchIn(scope)
    }

    private suspend fun sendLocalCandidate(candidate: LocalIceCandidateData) {
        val conversationId = activeConversationId ?: return
        val peer = activePeerSteamId ?: return
        val wireData = json.encodeToString(
            IceCandidateWireData(candidate.sdpMid, candidate.sdpMLineIndex, candidate.candidate)
        )
        signalingClient.send(
            SignalingEnvelope(
                type = SignalingMessageType.ICE_CANDIDATE,
                iceCandidate = IceCandidatePayload(conversationId.toString(), peer.toString(), candidate = wireData)
            )
        )
    }

    private suspend fun handleIncoming(envelope: SignalingEnvelope) {
        when (envelope.type) {
            SignalingMessageType.SDP_OFFER -> handleRemoteOffer(envelope)
            SignalingMessageType.SDP_ANSWER -> handleRemoteAnswer(envelope)
            SignalingMessageType.ICE_CANDIDATE -> handleRemoteIceCandidate(envelope)
            SignalingMessageType.CALL_HANGUP -> handleRemoteHangup(envelope)
        }
    }

    /* Callee/answer path - no explicit accept step, the offer itself is the ring. */
    private suspend fun handleRemoteOffer(envelope: SignalingEnvelope) {
        val sdp = envelope.sdp ?: return
        val fromSteamId = sdp.fromSteamId?.toLongOrNull() ?: return
        val conversationId = sdp.conversationId.toLongOrNull() ?: return

        /* No call-waiting / switching in minimal core: busy just drops the offer. */
        if (activeCall != null) return

        println("CallManager: received sdp_offer from $fromSteamId")
        activeConversationId = conversationId
        activePeerSteamId = fromSteamId
        val call = webRtcClientFactory()
        activeCall = call
        _isInCall.value = true
        forwardLocalIceCandidates(call)
        logConnectionState(call)

        val answerSdp = call.createAnswer(sdp.sdp)
        println("CallManager: sending sdp_answer to $fromSteamId")
        signalingClient.send(
            SignalingEnvelope(
                type = SignalingMessageType.SDP_ANSWER,
                sdp = SdpPayload(conversationId.toString(), fromSteamId.toString(), sdp = answerSdp)
            )
        )
    }

    private suspend fun handleRemoteAnswer(envelope: SignalingEnvelope) {
        val sdp = envelope.sdp ?: return
        val call = activeCall ?: return
        println("CallManager: received sdp_answer")
        call.setRemoteAnswer(sdp.sdp)
    }

    private fun handleRemoteIceCandidate(envelope: SignalingEnvelope) {
        val payload = envelope.iceCandidate ?: return
        val call = activeCall ?: return
        val wire = runCatching { json.decodeFromString<IceCandidateWireData>(payload.candidate) }.getOrNull()
            ?: return
        call.addRemoteIceCandidate(wire.sdpMid, wire.sdpMLineIndex, wire.candidate)
    }

    private fun handleRemoteHangup(envelope: SignalingEnvelope) {
        val hangup = envelope.callHangup ?: return
        if (hangup.conversationId.toLongOrNull() != activeConversationId) return
        teardown()
    }
}
