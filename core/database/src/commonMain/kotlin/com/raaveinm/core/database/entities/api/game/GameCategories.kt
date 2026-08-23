package com.raaveinm.core.database.entities.api.game

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Entity(
    primaryKeys = ["steamAppId", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = Games::class,
            parentColumns = ["steamAppId"],
            childColumns = ["steamAppId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Categories::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class GameCategories(
    val steamAppId: Int,
    val categoryId: Int
)
