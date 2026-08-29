package com.raaveinm.core.database.entities.api.user

import androidx.room3.Entity
import androidx.room3.ForeignKey

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

// No FK to Games here on purpose: GetOwnedGames already returns name/icon/playtime
// inline, so a library list shouldn't require fetching full appdetails (Games row)
// for every owned game up front — that'd be one extra request per game just to
// render the list. Games gets fetched lazily when the user opens a game's detail page.
@Entity(
    primaryKeys = ["userSteamId", "appId"],
    foreignKeys = [
        ForeignKey(
            entity = Users::class,
            parentColumns = ["steamId"],
            childColumns = ["userSteamId"],
            onDelete = ForeignKey.CASCADE // this row IS the user's library — no point without them
        )
    ]
    // userSteamId is covered by the composite PK
)
data class OwnedGames(
    val userSteamId: Long,
    val appId: Int,
    val name: String,
    val imgIconUrl: String,
    val playtimeForever: Int,
    val playtime2Weeks: Int,
    val playtimeForeverWindows: Int?,
    val playtimeForeverMac: Int?,
    val playtimeForeverLinux: Int?,
    val playtimeForeverDeck: Int?,
    val playtimeDisconnected: Int?,
    val hasCommunityVisible: Boolean,
    val rTimeLastPlayed: Long,
    val fetchedAt: Long
)
