package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import com.raaveinm.core.database.entities.server.Servers

//
// Created by Kirill "Raaveinm" on 8/23/26.
//

@Dao
interface ServerDao {
    @Insert suspend fun addServer(server: Servers)
}