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
    // paletteConversationId is covered by the composite PK; userSteamId needs its own
    // (also makes "which palettes is this user in" queries fast)
    indices = [Index("userSteamId")]
)
data class PaletteMembers(
    val paletteConversationId: Long,
    val userSteamId: Long
)