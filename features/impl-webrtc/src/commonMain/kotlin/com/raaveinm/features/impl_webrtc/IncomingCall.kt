package com.raaveinm.features.impl_webrtc

//
// Created by Kirill "Raaveinm" on 11/03/2026.
// Copyright (c) 2026 Retrograde Mercury. I'm freak
//

data class IncomingCall(
    val id: String,             // Unique local consent identity; never transmitted to the server.
    val conversationId: Long,   // Backend conversation ID used by SDP, ICE and hangup frames.
    val peerSteamId: Long,      // Caller stamped into fromSteamId by the backend.
)
