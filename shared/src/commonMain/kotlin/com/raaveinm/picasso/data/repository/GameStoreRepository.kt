package com.raaveinm.picasso.data.repository

//
// Created by Kirill "Raaveinm" on 9/3/26.
//

import com.raaveinm.core.database.dao.GameDao
import com.raaveinm.core.database.entities.api.game.toCategoriesEntities
import com.raaveinm.core.database.entities.api.game.toEntity
import com.raaveinm.core.database.entities.api.game.toGameCategoriesEntities
import com.raaveinm.core.database.entities.api.game.toGameMediaEntities
import com.raaveinm.picasso.data.ApiClient
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GameStoreRepository(
    private val apiClient: ApiClient,
    private val gameDao: GameDao
) {
    @OptIn(ExperimentalTime::class)
    suspend fun refreshGameDetails(appId: Int) {
        val game = apiClient.getGameStoreInfo(appId.toLong()) ?: return
        val fetchedAt = Clock.System.now().epochSeconds
        gameDao.addGame(game.toEntity(fetchedAt))
        gameDao.addGameMedia(game.toGameMediaEntities())
        gameDao.addCategories(game.toCategoriesEntities())
        gameDao.addGameCategories(game.toGameCategoriesEntities())
    }

    suspend fun refreshMissingDetails(appIds: List<Int>) {
        val cached = gameDao.getCachedAppIds().toSet()
        appIds.filterNot { it in cached }.forEach { refreshGameDetails(it) }
    }
}
