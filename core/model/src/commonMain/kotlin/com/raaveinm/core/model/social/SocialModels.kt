package com.raaveinm.core.model.social

import com.raaveinm.core.model.user.User

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

/**
 * UI-facing shapes for the contact graph. [user] is null until the Steam profile of
 * [steamId] has been fetched (a request can arrive from someone not yet in the cache).
 */
data class ContactEntry(
    val steamId: Long,
    val level: ContactLevel,
    val sinceEpochMs: Long,
    val user: User?
)

data class ContactRequestEntry(
    val steamId: Long,
    val createdAtEpochMs: Long,
    val user: User?
)

data class PaletteInviteEntry(
    val conversationRemoteId: Long,
    val name: String,
    val inviterSteamId: Long,
    val createdAtEpochMs: Long,
    val inviter: User?
)
