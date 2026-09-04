package com.raaveinm.core.database.entities.api.game

import com.raaveinm.core.database.entities.api.user.OwnedGames
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.model.game.CommunityContent

//
// Created by Kirill "Raaveinm" on 9/3/26.
//

fun List<GameWithDetails>.toCommunityContent(ownedGames: List<OwnedGames>): List<CommunityContent> {
    val ownedByAppId = ownedGames.associateBy { it.appId }
    return mapNotNull { details ->
        val owned = ownedByAppId[details.game.steamAppId] ?: return@mapNotNull null
        CommunityContent(
            ownedGame = owned.toDto(),
            imageUrl = details.screenshots.map { it.pathFull },
            inLibrary = true,
            gameTags = details.categories.map { it.description }
        )
    }
}
