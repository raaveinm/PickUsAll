package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.raaveinm.core.database.entities.api.user.OwnedGames
import com.raaveinm.core.database.entities.api.user.Users
import kotlinx.coroutines.flow.Flow

//
// Created by Kirill "Raaveinm" on 8/23/26.
//

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: Users)

    // NOTES: Cache rotation
    @Query(
        """
        DELETE FROM Users
        WHERE steamId != :selfSteamId
          AND fetchedAt < :cutoff
          AND steamId NOT IN (SELECT friendSteamId FROM SteamFriends WHERE userSteamId = :selfSteamId)
          AND steamId NOT IN (SELECT chatTitleSteamId FROM Chats)
          AND steamId NOT IN (SELECT userSteamId FROM PaletteMembers)
        """
    )
    suspend fun pruneStaleUsers(selfSteamId: Long, cutoff: Long): Int
    @Query("select * from OwnedGames where userSteamId=:userId")
    fun getUserLibrary(userId: Long): Flow<List<OwnedGames>>
}