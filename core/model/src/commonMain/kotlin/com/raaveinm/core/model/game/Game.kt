package com.raaveinm.core.model.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 9/1/26.
//

@Serializable
data class Game(
    @SerialName("steam_appid")
    val steamAppId: Int,
    val name: String,
    val type: String?,
    @SerialName("short_description")
    val shortDescription: String?,
    @SerialName("about_the_game")
    val aboutGame: String?,
    @SerialName("detailed_description")
    val detailedDescription: String?,
    @SerialName("supported_languages")
    val supportedLanguages: String?,
    @SerialName("header_image")
    val headerImage: String?,
    val website: String?,
    val platforms: GameApiPlatforms,
    @SerialName("price_overview")
    val priceOverview: GameApiPriceOverview? = null,
    @SerialName("release_date")
    val releaseDate: GameApiReleaseDate? = null,
    val screenshots: List<GameApiScreenshot> = emptyList(),
    val categories: List<GameApiCategory> = emptyList(),
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
