package com.raaveinm.core.model.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 9/1/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//
//* @sample "https://store.steampowered.com/api/appdetails?appids=1808500&cc=u&l=english"

/**
 * Store details from Steam's app-details response, nested under the requested app ID.
 * Descriptions, supported languages, and reviews may contain HTML. Optional collections
 * default to empty lists; the additional store strings default to empty when Steam omits them.
 */
@Serializable
data class Game(
    @SerialName("steam_appid")
    val steamAppId: Int,
    val name: String,
    val type: String? = null,
    @SerialName("short_description")
    val shortDescription: String? = null,
    @SerialName("about_the_game")
    val aboutGame: String? = null,
    @SerialName("detailed_description")
    val detailedDescription: String? = null,
    @SerialName("supported_languages")
    val supportedLanguages: String? = null,
    @SerialName("header_image")
    val headerImage: String? = null,
    val website: String? = null,
    val platforms: GameApiPlatforms,
    @SerialName("price_overview")
    val priceOverview: GameApiPriceOverview? = null,
    @SerialName("release_date")
    val releaseDate: GameApiReleaseDate? = null,
    val screenshots: List<GameApiScreenshot> = emptyList(),
    val categories: List<GameApiCategory> = emptyList(),
    val dlc: List<Int> = emptyList(),
    @SerialName("controller_support")
    val controllerSupport: String = "",
    val reviews: String = "",
    val background: String = "",
    @SerialName("background_raw")
    val backgroundRaw: String = "",
    val movies: List<GameApiMovies> = emptyList(),
)

@Serializable
data class GameApiPlatforms(
    val windows: Boolean = false,
    val mac: Boolean = false,
    val linux: Boolean = false
)

@Serializable
data class GameApiPriceOverview(
    val currency: String,
    val initial: Int,
    val final: Int,
    @SerialName("discount_percent")
    val discountPercent: Int,
    @SerialName("initial_formatted")
    val initialFormatted: String,
    @SerialName("final_formatted")
    val finalFormatted: String
)

@Serializable
data class GameApiReleaseDate(
    @SerialName("coming_soon")
    val comingSoon: Boolean,
    val date: String
)

@Serializable
data class GameApiScreenshot(
    val id: Int,
    @SerialName("path_thumbnail")
    val pathThumbnail: String,
    @SerialName("path_full")
    val pathFull: String
)

@Serializable
data class GameApiCategory(
    val id: Int,
    val description: String
)

///////////////////////////////////////////////
// Store trailers
///////////////////////////////////////////////

@Serializable
data class GameApiMovies(
    val id: Int,
    val name: String,
    val thumbnail: String,
    val highlight: Boolean = false,
    val webm: GameApiMovieFiles? = null,
    val mp4: GameApiMovieFiles? = null,
    @SerialName("dash_av1")
    val dashAv1: String? = null,
    @SerialName("dash_h264")
    val dashH264: String? = null,
    @SerialName("hls_h264")
    val hlsH264: String? = null,
)

/** Downloadable trailer URLs keyed by quality; the JSON key "480" is not a Kotlin identifier. */
@Serializable
data class GameApiMovieFiles(
    @SerialName("480")
    val video480: String? = null,
    val max: String? = null,
)
