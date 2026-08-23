package com.raaveinm.core.database.entities.api.user

import androidx.room3.Entity
import androidx.room3.PrimaryKey

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Entity
data class Users(
    @PrimaryKey val steamId: Long,
    val communityVisibilityState: Int,
    val profileState: Boolean? = null,
    val personaName: String,
    val commentPermission: Boolean,
    val profileUrl: String,
    val avatar: String,
    val avatarMedium: String,
    val avatarFull: String,
    val avatarHash: String,
    val lastLogOff: Long? = null,
    val personaState: Int,
    val realName: String? = null,
    val primaryClanId: String? = null,
    val timeCreated: Long? = null,
    val personaStateFlags: Int? = null,
    val locCountryCode: String? = null,
    val locStateCode: String? = null,
    val locCityId: Int? = null,
    val gameExtraInfo: String? = null,
    val gameId: Int? = null,
)
