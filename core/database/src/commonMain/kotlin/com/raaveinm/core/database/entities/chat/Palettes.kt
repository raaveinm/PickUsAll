package com.raaveinm.core.database.entities.chat

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Palettes(
    @PrimaryKey(autoGenerate = true) val conversationId: Long,
    val name: String
)
