package com.raaveinm.core.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 8/21/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//
//* @sample "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=${STEAM_API_KEY}&steamids=&{STEAMID64}"

/**
 *
 *
 *  @param personaState
 *
 * The user's current status:
 * - `0`: Offline (or private profile)
 * - `1`: Online
 * - `2`: Busy
 * - `3`: Away
 * - `4`: Snooze
 * - `5`: Looking to trade
 * - `6`: Looking to play
 *
 **/
@Serializable
data class User(
    @SerialName("steamid")
    val steamId: Long,                          // Steam ID
    @SerialName("communityvisibilitystate")     // Community Visibility State
    val communityVisibilityState: Int,        // communityvisibilitystate 3 = Public, 1 = Private (only 3 returns full detail)
    @SerialName("profilestate")                 // Profile State
    val profileState: Boolean? = null,                // user has ever set up their Steam Community profile
    @SerialName("personaname")
    val personaName: String,                    // Display Name
    @SerialName("commentpermission")
    val commentPermission: Boolean,             // Is comments allowed
    @SerialName("profileurl")
    val profileUrl: String,                     // Steam Profile URL
    @SerialName("avatar")
    val avatar: String,                         // Avatar URL
    @SerialName("avatarmedium")
    val avatarMedium: String,                   // Avatar URL (medium)
    @SerialName("avatarfull")
    val avatarFull: String,                     // Avatar URL (full)
    @SerialName("avatarhash")
    val avatarHash: String,                     // Avatar Hash
    @SerialName("lastlogoff")
    val lastLogOff: Long? = null,
    @SerialName("personastate")
    val personaState: Int,                      // Online State
    @SerialName("realname")
    val realName: String? = null,
    @SerialName("primaryclanid")
    val primaryClanId: String? = null,
    @SerialName("timecreated")
    val timeCreated: Long? = null,
    @SerialName("personastateflags")
    val personaStateFlags: Int? = null,
    @SerialName("loccountrycode")
    val locCountryCode: String? = null,
    @SerialName("locstatecode")
    val locStateCode: String? = null,
    @SerialName("loccityid")
    val locCityId: Int? = null,
    @SerialName("gameextrainfo")                // Currently online game name
    val gameExtraInfo: String? = null,
    @SerialName("gameid")                       // Currently online game id
    val gameId: Int? = null,
)
