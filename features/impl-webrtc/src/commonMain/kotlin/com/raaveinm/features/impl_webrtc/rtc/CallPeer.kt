package com.raaveinm.features.impl_webrtc.rtc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CallPeer {
    val connectionState: StateFlow<CallConnectionState>
    val localIceCandidates: Flow<LocalIceCandidateData>

    suspend fun createOffer(): String
    suspend fun createAnswer(remoteOfferSdp: String): String
    suspend fun setRemoteAnswer(sdp: String)
    fun addRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String)
    fun close()
}

internal class WebRtcCallPeer(
    private val client: WebRtcCallClient = WebRtcCallClient(),
) : CallPeer {
    override val connectionState: StateFlow<CallConnectionState> get() = client.connectionState
    override val localIceCandidates: Flow<LocalIceCandidateData> get() = client.localIceCandidates

    override suspend fun createOffer(): String = client.createOffer()
    override suspend fun createAnswer(remoteOfferSdp: String): String = client.createAnswer(remoteOfferSdp)
    override suspend fun setRemoteAnswer(sdp: String) = client.setRemoteAnswer(sdp)
    override fun addRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String) =
        client.addRemoteIceCandidate(sdpMid, sdpMLineIndex, candidate)
    override fun close() = client.close()
}
