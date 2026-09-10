@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.raaveinm.features.impl_webrtc.rtc

import dev.onvoid.webrtc.CreateSessionDescriptionObserver
import dev.onvoid.webrtc.PeerConnectionFactory
import dev.onvoid.webrtc.PeerConnectionObserver
import dev.onvoid.webrtc.RTCAnswerOptions
import dev.onvoid.webrtc.RTCConfiguration
import dev.onvoid.webrtc.RTCIceCandidate
import dev.onvoid.webrtc.RTCOfferOptions
import dev.onvoid.webrtc.RTCPeerConnection
import dev.onvoid.webrtc.RTCPeerConnectionState
import dev.onvoid.webrtc.RTCSdpType
import dev.onvoid.webrtc.RTCSessionDescription
import dev.onvoid.webrtc.SetSessionDescriptionObserver
import dev.onvoid.webrtc.media.audio.AudioOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
 * Real implementation for Desktop, backed by dev.onvoid.webrtc:webrtc-java - a
 * JNI wrapper around the same native libwebrtc used by mobile, giving Desktop
 * an actual PeerConnection instead of the previous no-op stub. webrtc-java's
 * enum names for connection state match CallConnectionState's 1:1 by design.
 */
actual class WebRtcCallClient actual constructor() {
    private val factory = PeerConnectionFactory()

    private val _connectionState = MutableStateFlow(CallConnectionState.NEW)
    actual val connectionState: StateFlow<CallConnectionState> = _connectionState

    private val _localIceCandidates = MutableSharedFlow<LocalIceCandidateData>(extraBufferCapacity = 64)
    actual val localIceCandidates: Flow<LocalIceCandidateData> = _localIceCandidates

    /*
     * No STUN/TURN servers configured - same known limitation as the mobile
     * actual (see WebRtcCallClient.mobile.kt): works on the same network / NAT-
     * friendly paths only, not general WAN, until the backend can deliver ICE
     * server config (docs/WEBRTC.md #7.1).
     */
    private val peerConnection: RTCPeerConnection = factory.createPeerConnection(
        RTCConfiguration(),
        object : PeerConnectionObserver {
            override fun onIceCandidate(candidate: RTCIceCandidate) {
                _localIceCandidates.tryEmit(
                    LocalIceCandidateData(candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp)
                )
            }

            override fun onConnectionChange(state: RTCPeerConnectionState) {
                _connectionState.value = CallConnectionState.valueOf(state.name)
            }
        }
    )

    init {
        val audioSource = factory.createAudioSource(AudioOptions())
        val audioTrack = factory.createAudioTrack("audio0", audioSource)
        peerConnection.addTrack(audioTrack, listOf("stream0"))
    }

    actual suspend fun createOffer(): String {
        val offer = createOfferDescription()
        setLocalDescription(offer)
        return offer.sdp
    }

    actual suspend fun createAnswer(remoteOfferSdp: String): String {
        setRemoteDescription(RTCSessionDescription(RTCSdpType.OFFER, remoteOfferSdp))
        val answer = createAnswerDescription()
        setLocalDescription(answer)
        return answer.sdp
    }

    actual suspend fun setRemoteAnswer(sdp: String) {
        setRemoteDescription(RTCSessionDescription(RTCSdpType.ANSWER, sdp))
    }

    actual fun addRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String) {
        peerConnection.addIceCandidate(RTCIceCandidate(sdpMid, sdpMLineIndex, candidate))
    }

    actual fun close() {
        peerConnection.close()
        factory.dispose()
        _connectionState.value = CallConnectionState.CLOSED
    }

    private suspend fun createOfferDescription(): RTCSessionDescription =
        suspendCancellableCoroutine { cont ->
            peerConnection.createOffer(
                RTCOfferOptions(),
                object : CreateSessionDescriptionObserver {
                    override fun onSuccess(description: RTCSessionDescription) =
                        cont.resume(description)

                    override fun onFailure(error: String) =
                        cont.resumeWithException(RuntimeException(error))
                })
        }

    private suspend fun createAnswerDescription(): RTCSessionDescription =
        suspendCancellableCoroutine { cont ->
            peerConnection.createAnswer(
                RTCAnswerOptions(),
                object : CreateSessionDescriptionObserver {
                    override fun onSuccess(description: RTCSessionDescription) =
                        cont.resume(description)

                    override fun onFailure(error: String) =
                        cont.resumeWithException(RuntimeException(error))
                })
        }

    private suspend fun setLocalDescription(description: RTCSessionDescription): Unit =
        suspendCancellableCoroutine { cont ->
            peerConnection.setLocalDescription(description, object : SetSessionDescriptionObserver {
                override fun onSuccess() = cont.resume(Unit)
                override fun onFailure(error: String) =
                    cont.resumeWithException(RuntimeException(error))
            })
        }

    private suspend fun setRemoteDescription(description: RTCSessionDescription): Unit =
        suspendCancellableCoroutine { cont ->
            peerConnection.setRemoteDescription(
                description,
                object : SetSessionDescriptionObserver {
                    override fun onSuccess() = cont.resume(Unit)
                    override fun onFailure(error: String) =
                        cont.resumeWithException(RuntimeException(error))
                })
        }
}
