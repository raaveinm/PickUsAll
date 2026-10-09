package com.raaveinm.core.database.entities.social

import androidx.room3.Entity
import androidx.room3.ForeignKey
import com.raaveinm.core.database.entities.server.Servers

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

@Entity(
    primaryKeys = ["serverId", "steamId", "incoming"],
    foreignKeys = [
        ForeignKey(
            entity = Servers::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactRequests(
    val serverId: Long,
    val steamId: Long,
    val incoming: Boolean,
    val createdAt: Long
)
