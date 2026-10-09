package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.raaveinm.core.database.entities.server.Servers
import kotlinx.coroutines.flow.Flow

//
// Created by Kirill "Raaveinm" on 8/23/26.
//

@Dao
interface ServerDao {
    @Insert suspend fun addServer(server: Servers)

    @Update suspend fun updateServer(server: Servers)

    @Delete suspend fun deleteServer(server: Servers)

    @Query("SELECT * FROM Servers ORDER BY added DESC")
    fun getAllServers(): Flow<List<Servers>>

    @Query("SELECT * FROM Servers where ping IS NOT NULL")
    suspend fun getReachableServers(): List<Servers>

    @Query("UPDATE Servers SET ping = :ping WHERE id = :serverId")
    suspend fun updatePing(serverId: Long, ping: Int?)

    /** The `deletedCursor` the last successful POST /sync handed back; null = never synced with this server. */
    @Query("UPDATE Servers SET deletedCursor = :cursor WHERE id = :serverId")
    suspend fun updateDeletedCursor(serverId: Long, cursor: Long?)

    @Query("SELECT deletedCursor FROM Servers WHERE id = :serverId")
    suspend fun getDeletedCursor(serverId: Long): Long?
}