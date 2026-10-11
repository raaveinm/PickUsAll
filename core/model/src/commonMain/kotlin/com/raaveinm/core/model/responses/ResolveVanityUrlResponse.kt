package com.raaveinm.core.model.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 10/11/2026.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//
//* @sample "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=${STEAM_API_KEY}&vanityurl=raaveinm&url_type=1"

@Serializable
data class ResolveVanityUrlResponse(
    val response: ResolveVanityUrlBody
)

/**
 * Steam's answer to "which steamId does this custom profile URL belong to".
 *
 * @param success
 *
 * - `1`: resolved, [steamId] is set
 * - `42`: no match - [message] is "No match", [steamId] is absent
 *
 **/
@Serializable
data class ResolveVanityUrlBody(
    @SerialName("success")
    val success: Int,                           // 1 = found, 42 = no match (not a JSON boolean)
    @SerialName("steamid")
    val steamId: String? = null,                // SteamID64 as a decimal STRING; absent unless success == 1
    @SerialName("message")
    val message: String? = null                 // "No match" on failure; absent on success
)
