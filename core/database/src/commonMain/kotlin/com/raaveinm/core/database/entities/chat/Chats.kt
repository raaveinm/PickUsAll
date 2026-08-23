package com.raaveinm.core.database.entities.chat

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.raaveinm.core.database.entities.api.user.Users

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Conversations::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Users::class,
            parentColumns = ["steamId"],
            childColumns = ["chatTitleSteamId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    // conversationId is already indexed as the PK; chatTitleSteamId needs its own
    indices = [Index("chatTitleSteamId")]
)
data class Chats(
    @PrimaryKey val conversationId: Long,
    val chatTitleSteamId: Long
)
