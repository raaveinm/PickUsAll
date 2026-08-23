package com.raaveinm.core.database.entities.chat

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.raaveinm.core.database.entities.server.Servers

//
// Created by Kirill "Raaveinm" on 8/23/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Servers::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // covers the FK column too (serverId is the leftmost column), so no separate index needed for it
    indices = [Index(value = ["serverId", "remoteId"], unique = true)]
)
data class Conversations(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverId: Long,
    val kind: String,
    val lastMessage: String?,
    val remoteId: Long
)