@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.raaveinm.features.impl_webrtc.rtc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class CallConnectionState { NEW, CONNECTING, CONNECTED, DISCONNECTED, FAILED, CLOSED }

data class LocalIceCandidateData(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val candidate: String
)

/*
 * One PeerConnection + local mic track. Plain data types only in this
 * expect/actual boundary - androidMain/iosMain's actuals are backed by
 * webrtc-kmp, jvmMain's by webrtc-java (Desktop);
 */
expect class WebRtcCallClient() {
    val connectionState: StateFlow<CallConnectionState>
    val localIceCandidates: Flow<LocalIceCandidateData>

    suspend fun createOffer(): String
    suspend fun createAnswer(remoteOfferSdp: String): String
    suspend fun setRemoteAnswer(sdp: String)
    fun addRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String)
    fun close()
}
