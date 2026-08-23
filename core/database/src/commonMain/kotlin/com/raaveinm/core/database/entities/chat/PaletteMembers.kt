package com.raaveinm.core.database.entities.chat

import androidx.room3.Entity

@Entity(primaryKeys = ["paletteConversationId", "userSteamId"])
data class PaletteMembers(
    val paletteConversationId: Long,
    val userSteamId: Long
)