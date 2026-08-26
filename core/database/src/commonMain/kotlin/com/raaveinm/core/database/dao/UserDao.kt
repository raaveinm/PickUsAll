package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import com.raaveinm.core.database.entities.api.user.Users

//
// Created by Kirill "Raaveinm" on 8/23/26.
//

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: Users)
}