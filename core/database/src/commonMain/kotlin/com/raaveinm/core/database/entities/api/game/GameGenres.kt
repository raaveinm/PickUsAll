package com.raaveinm.core.database.entities.api.game

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved (they aren't :D)
//

@Entity(
    primaryKeys = ["steamAppId", "genreId"],
    foreignKeys = [
        ForeignKey(
            entity = Games::class,
            parentColumns = ["steamAppId"],
            childColumns = ["steamAppId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Genres::class,
            parentColumns = ["genreId"],
            childColumns = ["genreId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("genreId")]
)
data class GameGenres(
    val steamAppId: Int,
    val genreId: String
)
