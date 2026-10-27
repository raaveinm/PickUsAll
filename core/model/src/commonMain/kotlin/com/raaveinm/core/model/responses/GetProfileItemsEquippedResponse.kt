package com.raaveinm.core.model.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 10/26/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//
//* @sample "https://api.steampowered.com/IPlayerService/GetProfileItemsEquipped/v1/?key=${STEAM_API_KEY}&steamid=${STEAMID64}"

@Serializable
data class GetProfileItemsEquippedResponse(
    val response: ProfileItemsEquipped
)

/** What the user has equipped on their Steam profile; only the background slot is read. */
@Serializable
data class ProfileItemsEquipped(
    @SerialName("profile_background")
    val profileBackground: ProfileItem? = null   // absent when no background is equipped
)

/**
 * One equipped community item. Every path is RELATIVE to Steam's community image CDN
 * (e.g. "items/570/<hash>.webm"), except when Steam already sends a full URL.
 */
@Serializable
data class ProfileItem(
    @SerialName("image_large")
    val imageLarge: String? = null,              // still image of the item, always present for backgrounds
    @SerialName("movie_webm")
    val movieWebm: String? = null,               // animated backgrounds only
    @SerialName("movie_mp4")
    val movieMp4: String? = null,                // animated backgrounds only; preferred (Apple has no VP9 webm)
    @SerialName("movie_webm_small")
    val movieWebmSmall: String? = null,
    @SerialName("movie_mp4_small")
    val movieMp4Small: String? = null
)
