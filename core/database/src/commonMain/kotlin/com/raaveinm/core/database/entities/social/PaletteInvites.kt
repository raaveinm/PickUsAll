package com.raaveinm.core.database.entities.social

import androidx.room3.Entity
import androidx.room3.ForeignKey
import com.raaveinm.core.database.entities.server.Servers

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

/**
 * Pending invitations to a palette. Deliberately a table of its own and not a
 * Conversations row: a pending invitee is not a member, so nothing that lists
 * conversations or messages can ever surface one by accident.
 */
@Entity(
    primaryKeys = ["serverId", "conversationRemoteId"],
    foreignKeys = [
        ForeignKey(
            entity = Servers::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PaletteInvites(
    val serverId: Long,
    val conversationRemoteId: Long,
    val name: String,
    val inviterSteamId: Long,
    val createdAt: Long
)
