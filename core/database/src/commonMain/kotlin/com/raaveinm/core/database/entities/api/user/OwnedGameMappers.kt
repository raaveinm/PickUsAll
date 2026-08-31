package com.raaveinm.core.database.entities.api.user

import com.raaveinm.core.model.user.OwnedGame

fun OwnedGames.toDto(): OwnedGame = OwnedGame(
    appId = appId,
    name = name,
    playtime2Weeks = playtime2Weeks.toShort(),
    playtimeForever = playtimeForever,
    imgIconUrl = imgIconUrl,
    hasCommunityVisibleStats = hasCommunityVisible,
    playtimeWindowsForever = playtimeForeverWindows,
    playtimeMacForever = playtimeForeverMac,
    playtimeLinuxForever = playtimeForeverLinux,
    playtimeDeckForever = playtimeForeverDeck,
    rtimeLastPlayed = rTimeLastPlayed.toInt(),
    playtimeDisconnected = playtimeDisconnected,
)

fun OwnedGame.toEntity(userSteamId: Long, fetchedAt: Long): OwnedGames = OwnedGames(
    userSteamId = userSteamId,
    appId = appId,
    name = name,
    imgIconUrl = imgIconUrl,
    playtimeForever = playtimeForever,
    playtime2Weeks = playtime2Weeks.toInt(),
    playtimeForeverWindows = playtimeWindowsForever,
    playtimeForeverMac = playtimeMacForever,
    playtimeForeverLinux = playtimeLinuxForever,
    playtimeForeverDeck = playtimeDeckForever,
    playtimeDisconnected = playtimeDisconnected,
    hasCommunityVisible = hasCommunityVisibleStats,
    rTimeLastPlayed = rtimeLastPlayed.toLong(),
    fetchedAt = fetchedAt,
)
