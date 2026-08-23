package com.raaveinm.core.database.entities.server

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Servers(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val url: String,
    val name: String?,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val added: Long
)
