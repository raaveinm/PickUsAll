package com.raaveinm.picasso.ui.navigation

//
// Created by Kirill "Raaveinm" on 11/02/2026.
// Copyright (c) 2026 Retrograde Mercury. All you need is Love
//

sealed interface SideWindow {
    data class WebAuth(
        val url: String,
        val loginAttemptId: String,
    ) : SideWindow

    data class IncomingCall(
        val callId: String,
        val peerSteamId: Long,
    ) : SideWindow

    data class Browser(
        val url: String,
    ) : SideWindow
}
