package com.raaveinm.features.impl_webrtc.rtc

import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.PeerConnectionState
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import com.shepeliev.webrtckmp.audioTracks
import com.shepeliev.webrtckmp.onConnectionStateChange
import com.shepeliev.webrtckmp.onIceCandidate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/*
 * Real implementation, shared by androidMain and iosMain via the mobileMain
 * intermediate source set - webrtc-kmp's PeerConnection/MediaDevices/IceCandidate
 * are genuinely the same Kotlin API on both platforms.
 */
actual class WebRtcCallClient actual constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /*
     * No STUN/TURN servers configured - the backend has no mechanism yet to
     * deliver ICE server config (docs/WEBRTC.md #7.1 on the backend side). This
     * works on the same network / NAT-friendly paths only, not general WAN.
     */
    private val peerConnection = PeerConnection(RtcConfiguration())

    private val _connectionState = MutableStateFlow(CallConnectionState.NEW)
    actual val connectionState: StateFlow<CallConnectionState> = _connectionState

    actual val localIceCandidates: Flow<LocalIceCandidateData> = peerConnection.onIceCandidate
        .map { LocalIceCandidateData(it.sdpMid, it.sdpMLineIndex, it.candidate) }

    /* createOffer/createAnswer wait on this so the mic track is always in the SDP. */
    private val localMediaReady = CompletableDeferred<Unit>()

    init {
        peerConnection.onConnectionStateChange
            .onEach { _connectionState.value = it.toCallConnectionState() }
            .launchIn(scope)

        scope.launch {
            val localStream = MediaDevices.getUserMedia(audio = true)
            localStream.audioTracks.forEach { peerConnection.addTrack(it, localStream) }
            localMediaReady.complete(Unit)
        }
    }

    actual suspend fun createOffer(): String {
        localMediaReady.await()
        val description = peerConnection.createOffer(OfferAnswerOptions())
        peerConnection.setLocalDescription(description)
        return description.sdp
    }

    actual suspend fun createAnswer(remoteOfferSdp: String): String {
        localMediaReady.await()
        peerConnection.setRemoteDescription(SessionDescription(SessionDescriptionType.Offer, remoteOfferSdp))
        val description = peerConnection.createAnswer(OfferAnswerOptions())
        peerConnection.setLocalDescription(description)
        return description.sdp
    }

    actual suspend fun setRemoteAnswer(sdp: String) {
        peerConnection.setRemoteDescription(SessionDescription(SessionDescriptionType.Answer, sdp))
    }

    actual fun addRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String) {
        scope.launch {
            peerConnection.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, candidate))
        }
    }

    actual fun close() {
        peerConnection.close()
        _connectionState.value = CallConnectionState.CLOSED
    }
}

private fun PeerConnectionState.toCallConnectionState(): CallConnectionState = when (this) {
    PeerConnectionState.New -> CallConnectionState.NEW
    PeerConnectionState.Connecting -> CallConnectionState.CONNECTING
    PeerConnectionState.Connected -> CallConnectionState.CONNECTED
    PeerConnectionState.Disconnected -> CallConnectionState.DISCONNECTED
    PeerConnectionState.Failed -> CallConnectionState.FAILED
    PeerConnectionState.Closed -> CallConnectionState.CLOSED
}
