package com.raaveinm.core.database.entities.api.game

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

//
// Created by Kirill "Raaveinm" on 9/3/26.
//

data class GameWithDetails(
    @Embedded val game: Games,
    @Relation(parentColumns = ["steamAppId"], entityColumns = ["steamAppId"])
    val screenshots: List<GameMedia>,
    @Relation(
        parentColumns = ["steamAppId"],
        entityColumns = ["categoryId"],
        associateBy = Junction(
            value = GameCategories::class,
            parentColumns = ["steamAppId"],
            entityColumns = ["categoryId"]
        )
    )
    val categories: List<Categories>
)
