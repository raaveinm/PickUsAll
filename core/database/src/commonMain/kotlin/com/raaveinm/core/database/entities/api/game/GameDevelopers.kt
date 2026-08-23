package com.raaveinm.core.database.entities.api.game

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Games::class,
            parentColumns = ["steamAppId"],
            childColumns = ["steamAppId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("steamAppId")]
)
data class GameDevelopers(
    @PrimaryKey(autoGenerate = true) val developerId: Int = 0,
    val steamAppId: Int,
    val developerName: String
)
