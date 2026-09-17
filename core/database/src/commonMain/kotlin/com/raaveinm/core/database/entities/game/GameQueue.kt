package com.raaveinm.core.database.entities.game

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import com.raaveinm.core.database.entities.api.game.Games
import com.raaveinm.core.database.entities.api.user.Users

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Users::class,
            parentColumns = ["steamId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Games::class,
            parentColumns = ["steamAppId"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("userId", "priority")],
    primaryKeys = ["id", "userId"]
)
data class GameQueue(
    val id: Long,
    val userId: Long,
    val gameId: Int,
    val priority: Int // >= 0
)
