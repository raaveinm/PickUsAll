package com.raaveinm.core.database.entities.api.game

import androidx.room3.Entity
import androidx.room3.PrimaryKey

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved (they aren't :D)
//

@Entity
data class Genres(
    @PrimaryKey val genreId: String,
    val description: String
)
