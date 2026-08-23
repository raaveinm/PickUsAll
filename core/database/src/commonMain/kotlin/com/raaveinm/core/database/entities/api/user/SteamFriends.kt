package com.raaveinm.core.database.entities.api.user

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Entity(
    primaryKeys = ["userSteamId", "friendSteamId"],
    foreignKeys = [
        ForeignKey(
            entity = Users::class,
            parentColumns = ["steamId"],
            childColumns = ["userSteamId"],
            onDelete = ForeignKey.CASCADE // this row IS part of the user's friend list
        ),
        ForeignKey(
            entity = Users::class,
            parentColumns = ["steamId"],
            childColumns = ["friendSteamId"],
            onDelete = ForeignKey.NO_ACTION // just a pointer to the friend's cached profile
        )
    ],
    // userSteamId covered by the composite PK; friendSteamId needs its own
    indices = [Index("friendSteamId")]
)
data class SteamFriends(
    val userSteamId: Long,
    val friendSteamId: Long,
    val relation: String?,
    val friendsSince: Long?,
    val createdAt: Long?
)
