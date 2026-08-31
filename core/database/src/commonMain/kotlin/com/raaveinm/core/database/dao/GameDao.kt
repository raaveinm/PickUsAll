package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.raaveinm.core.database.entities.api.game.Games
import kotlinx.coroutines.flow.Flow

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGame(game: Games)

    @Query("select * from Games")
    fun observeGames(): Flow<List<Games>>
}
