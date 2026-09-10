package com.raaveinm.features.impl_webrtc.signaling

import kotlinx.serialization.Serializable

/*
 * Mirrors PicassoBackend's EnvelopeDto/SdpDto/IceCandidateDto/CallHangupDto wire
 * shape exactly (src/dto/Envelope.hpp) - field names are the JSON contract, not
 * cosmetic. All ids are strings on the wire, matching the backend's DTO_FIELD(String, ...).
 */
@Serializable
data class SignalingEnvelope(
    val type: String,
    val sdp: SdpPayload? = null,
    val iceCandidate: IceCandidatePayload? = null,
    val callHangup: HangupPayload? = null
)

@Serializable
data class SdpPayload(
    val conversationId: String,
    val toSteamId: String,
    val fromSteamId: String? = null,
    val sdp: String
)

/*
 * webrtc-kmp's IceCandidate carries sdpMid/sdpMLineIndex/candidate as three
 * separate fields, but the backend's IceCandidateDto.candidate is one opaque
 * string it never parses - so the three fields are packed into that string as
 * nested JSON (see IceCandidateWireData) rather than needing a backend change.
 */
@Serializable
data class IceCandidatePayload(
    val conversationId: String,
    val toSteamId: String,
    val fromSteamId: String? = null,
    val candidate: String
)

@Serializable
data class IceCandidateWireData(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val candidate: String
)

@Serializable
data class HangupPayload(
    val conversationId: String,
    val toSteamId: String,
    val fromSteamId: String? = null
)

object SignalingMessageType {
    const val SDP_OFFER = "sdp_offer"
    const val SDP_ANSWER = "sdp_answer"
    const val ICE_CANDIDATE = "ice_candidate"
    const val CALL_HANGUP = "call_hangup"
}
