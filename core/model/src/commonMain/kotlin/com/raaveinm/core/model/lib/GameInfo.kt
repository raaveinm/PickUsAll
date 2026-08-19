package com.raaveinm.core.model.lib

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 8/18/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

///////////////////////////////////////////////
// URLs (For Reference)
///////////////////////////////////////////////
// Owned Games: https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=${STEAM_API_KEY}&steamid=${userId}&include_appinfo=true&include_played_free_games=true&format=json

@Serializable
data class GameInfo(
    @SerialName("appid")
    val appId: Int,                             // Steam Game ID
    val name: String,                           // Game Name
    @SerialName("playtime_2weeks")
    val playtime2Weeks: Short = 0,              // Playtime in 2 weeks (absent unless played recently)
    @SerialName("playtime_forever")
    val playtimeForever: Int,                   // Playtime for all time
    @SerialName("img_icon_url")
    val imgIconUrl: String,                     // Desktop Icon (can be empty string)
    @SerialName("has_community_visible_stats")
    val hasCommunityVisibleStats: Boolean = false, // Community Stats? (absent when false)
    @SerialName("playtime_windows_forever")
    val playtimeWindowsForever: Int?,           // \
    @SerialName("playtime_mac_forever")
    val playtimeMacForever: Int?,               //  | Platform
    @SerialName("playtime_linux_forever")
    val playtimeLinuxForever: Int?,             //  | Specific Playtime
    @SerialName("playtime_deck_forever")
    val playtimeDeckForever: Int?,              // /
    @SerialName("rtime_last_played")
    val rtimeLastPlayed: Int,                   // Timestamp
    @SerialName("playtime_disconnected")
    val playtimeDisconnected: Int?              // Playtime accrued while Steam client was offline
)
