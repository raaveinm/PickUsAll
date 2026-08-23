package com.raaveinm.core.database.entities.api.game

import androidx.room3.Entity
import androidx.room3.ForeignKey

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved (they aren't :D)
//

@Entity(
    primaryKeys = ["appId", "apiName"],
    foreignKeys = [
        ForeignKey(
            entity = Games::class,
            parentColumns = ["steamAppId"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
    // appId is covered by the composite PK, no extra index needed
)
data class GameAchievements(
    val appId: Int,
    val apiName: String,
    val name: String?,
    val description: String?
)
