package com.raaveinm.core.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 9/6/26.
//

///////////////////////////////////////////////
// URLs (For Reference)
///////////////////////////////////////////////
// Friend list: https://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=${STEAM_API_KEY}&steamid=${userId}&relationship=friend

/**
 * A single entry of the Steam friend list - the relation only, no profile data.
 * The profile behind [steamId] has to be fetched separately from GetPlayerSummaries.
 */
@Serializable
data class Friend(
    @SerialName("steamid")
    val steamId: Long,                          // Steam ID of the friend
    val relationship: String? = null,           // "friend" for everything this app asks for
    @SerialName("friend_since")
    val friendSince: Long? = null               // Timestamp, 0 for very old relations
)
