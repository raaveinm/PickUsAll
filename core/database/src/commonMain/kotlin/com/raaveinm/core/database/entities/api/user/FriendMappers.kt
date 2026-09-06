package com.raaveinm.core.database.entities.api.user

import com.raaveinm.core.model.user.Friend

fun Friend.toEntity(userSteamId: Long, createdAt: Long): SteamFriends = SteamFriends(
    userSteamId = userSteamId,
    friendSteamId = steamId,
    relation = relationship,
    friendsSince = friendSince,
    createdAt = createdAt,
)
