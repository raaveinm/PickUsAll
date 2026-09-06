package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.raaveinm.core.database.entities.api.user.OwnedGames
import com.raaveinm.core.database.entities.api.user.SteamFriends
import com.raaveinm.core.database.entities.api.user.Users
import kotlinx.coroutines.flow.Flow

//
// Created by Kirill "Raaveinm" on 8/23/26.
//

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: Users)

    @Upsert
    suspend fun upsertUsers(users: List<Users>)

    @Query("DELETE FROM SteamFriends WHERE userSteamId = :userSteamId")
    suspend fun deleteFriendsOf(userSteamId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFriends(friends: List<SteamFriends>)

    /**
     * Steam hands back the friend list as a whole, so it is authoritative as a whole:
     * whoever dropped off it has to disappear locally too. The unfriended user's `Users`
     * row is deliberately left behind - [pruneStaleUsers] is what reclaims it, once it's
     * both stale and unreachable.
     */
    @Transaction
    suspend fun replaceFriends(userSteamId: Long, friends: List<SteamFriends>) {
        deleteFriendsOf(userSteamId)
        addFriends(friends)
    }

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOwnedGames(games: List<OwnedGames>)
}