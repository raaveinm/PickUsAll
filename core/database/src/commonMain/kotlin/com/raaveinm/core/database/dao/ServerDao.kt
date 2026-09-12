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
}