package com.raaveinm.core.database.entities.game

import androidx.room3.Embedded
import com.raaveinm.core.database.entities.api.game.Games
import com.raaveinm.core.model.game.GameQueueItem

data class GameQueueWithGame(
    @Embedded val queueEntry: GameQueue,
    @Embedded val game: Games
)

fun GameQueueWithGame.toDto(): GameQueueItem = GameQueueItem(
    gameId = queueEntry.gameId,
    name = game.name,
    priority = queueEntry.priority
)
