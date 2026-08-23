package com.raaveinm.core.database.entities.api.game

import androidx.room3.Entity
import androidx.room3.ForeignKey
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
            childColumns = ["gameAppId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CommunityContent(
    @PrimaryKey val gameAppId: Int,
    val imageUrl: String,
    val inLibrary: Boolean,
    val gameTags: String
)
