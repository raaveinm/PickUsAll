package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.raaveinm.core.database.entities.api.game.Categories
import com.raaveinm.core.database.entities.api.game.GameCategories
import com.raaveinm.core.database.entities.api.game.GameMedia
import com.raaveinm.core.database.entities.api.game.GameWithDetails
import com.raaveinm.core.database.entities.api.game.Games
import com.raaveinm.core.database.entities.game.GameQueue
import com.raaveinm.core.database.entities.game.GameQueueWithGame
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

    @Query("select steamAppId from Games")
    suspend fun getCachedAppIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGameMedia(media: List<GameMedia>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCategories(categories: List<Categories>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGameCategories(gameCategories: List<GameCategories>)

    @Transaction
    @Query("select * from Games")
    fun observeGamesWithDetails(): Flow<List<GameWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToQueue(item: GameQueue)

    @Transaction
    @Query(
        "SELECT gameQueue.*, games.* FROM GameQueue AS gameQueue " +
            "JOIN Games AS games ON games.steamAppId = gameQueue.gameId " +
            "WHERE gameQueue.userId = :userId ORDER BY gameQueue.priority ASC"
    )
    fun observeQueue(userId: Long): Flow<List<GameQueueWithGame>>

    @Query("UPDATE GameQueue SET priority = :priority WHERE gameId = :gameId AND userId = :userId")
    suspend fun updateQueuePriority(userId: Long, gameId: Int, priority: Int)

    @Transaction
    suspend fun reorderQueue(userId: Long, orderedGameIds: List<Int>) {
        orderedGameIds.forEachIndexed { index, gameId ->
            updateQueuePriority(userId, gameId, index)
        }
    }
}
