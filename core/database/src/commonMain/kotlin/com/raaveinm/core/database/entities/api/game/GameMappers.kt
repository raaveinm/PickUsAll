package com.raaveinm.core.database.entities.api.game

import com.raaveinm.core.model.game.Game

//
// Created by Kirill "Raaveinm" on 9/3/26.
//

fun Game.toEntity(fetchedAt: Long): Games = Games(
    steamAppId = steamAppId,
    name = name,
    type = type,
    shortDescription = shortDescription,
    aboutGame = aboutGame,
    detailedDescription = detailedDescription,
    supportedLanguages = supportedLanguages,
    headerImage = headerImage,
    webSite = website,
    platformWindows = platforms.windows,
    platformMac = platforms.mac,
    platformLinux = platforms.linux,
    priceCurrency = priceOverview?.currency,
    priceInitial = priceOverview?.initial,
    priceFinal = priceOverview?.final,
    priceDiscountPercent = priceOverview?.discountPercent,
    priceInitialFormatted = priceOverview?.initialFormatted,
    priceFinalFormatted = priceOverview?.finalFormatted,
    releaseComingSoon = releaseDate?.comingSoon,
    releaseDate = releaseDate?.date,
    createdAt = fetchedAt,
    updatedAt = fetchedAt,
)

fun Game.toGameMediaEntities(): List<GameMedia> = screenshots.map { screenshot ->
    GameMedia(
        steamAppId = steamAppId,
        screenshotSteamId = screenshot.id,
        pathThumbNail = screenshot.pathThumbnail.toScreenshotPath(steamAppId),
        pathFull = screenshot.pathFull.toScreenshotPath(steamAppId),
    )
}

fun Game.toCategoriesEntities(): List<Categories> = categories.map { category ->
    Categories(categoryId = category.id, description = category.description)
}

fun Game.toGameCategoriesEntities(): List<GameCategories> = categories.map { category ->
    GameCategories(steamAppId = steamAppId, categoryId = category.id)
}

private val dimensionSuffix = Regex("""\.\d+x\d+$""")

private fun String.toScreenshotPath(steamAppId: Int): String {
    val withoutQuery = substringBefore('?')
    val afterAppId = withoutQuery.substringAfter("apps/$steamAppId/", withoutQuery)
    return afterAppId.substringBeforeLast('.').replace(dimensionSuffix, "")
}
