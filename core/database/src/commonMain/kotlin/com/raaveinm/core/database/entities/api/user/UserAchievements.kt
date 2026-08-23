package com.raaveinm.core.database.entities.api.user

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import com.raaveinm.core.database.entities.api.game.GameAchievements

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Entity(
    primaryKeys = ["steamId", "appId", "apiName"],
    foreignKeys = [
        ForeignKey(
            entity = Users::class,
            parentColumns = ["steamId"],
            childColumns = ["steamId"],
            onDelete = ForeignKey.CASCADE // this row IS part of the user's achievement stats
        ),
        ForeignKey(
            // composite FK: matches GameAchievements' own composite PK (appId, apiName)
            entity = GameAchievements::class,
            parentColumns = ["appId", "apiName"],
            childColumns = ["appId", "apiName"],
            onDelete = ForeignKey.CASCADE // unlock record is meaningless without the achievement definition
        )
    ],
    // steamId is covered by the composite PK; (appId, apiName) needs its own for the second FK
    indices = [Index(value = ["appId", "apiName"])]
)
data class UserAchievements(
    val steamId: Long,
    val appId: Int,
    val apiName: String,
    val achieved: Boolean = false,
    val unlockTime: Long?
)
