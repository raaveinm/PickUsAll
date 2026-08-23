package com.raaveinm.core.database.entities.api.game

import androidx.room3.Entity
import androidx.room3.PrimaryKey

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Entity
data class Categories(
    @PrimaryKey val categoryId: Int,
    val description: String
)
