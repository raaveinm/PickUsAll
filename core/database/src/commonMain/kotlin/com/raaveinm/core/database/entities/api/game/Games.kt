package com.raaveinm.core.database.entities.api.game

import androidx.room3.Entity
import androidx.room3.PrimaryKey

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved (they aren't :D)
//

@Entity
data class Games(
    @PrimaryKey val steamAppId: Int,
    val name: String,
    val type: String?,
    val shortDescription: String?,
    val aboutGame: String?,
    val detailedDescription: String?,
    val supportedLanguages: String?,
    val headerImage: String?,
    val webSite: String?,
    val platformWindows: Boolean?,
    val platformMac: Boolean?,
    val platformLinux: Boolean?,
    val priceCurrency: String?,
    val priceInitial: Int?,
    val priceFinal: Int?,
    val priceDiscountPercent: Int?,
    val priceInitialFormatted: String?,
    val priceFinalFormatted: String?,
    val releaseComingSoon: Boolean?,
    val releaseDate: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)