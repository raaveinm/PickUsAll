package com.raaveinm.core.database.entities.chat

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import com.raaveinm.core.database.entities.api.user.Users

@Entity(
    primaryKeys = ["paletteConversationId", "userSteamId"],
    foreignKeys = [
        ForeignKey(
            entity = Palettes::class,
            parentColumns = ["conversationId"],
            childColumns = ["paletteConversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Users::class,
            parentColumns = ["steamId"],
            childColumns = ["userSteamId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("userSteamId")]
)
data class PaletteMembers(
    val paletteConversationId: Long,
    val userSteamId: Long
)