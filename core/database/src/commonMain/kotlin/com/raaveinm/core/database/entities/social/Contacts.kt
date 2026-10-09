package com.raaveinm.core.database.entities.social

import androidx.room3.Entity
import androidx.room3.ForeignKey
import com.raaveinm.core.database.entities.server.Servers

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

/**
 * The LOCAL user's own row about another Artist, mirroring GET /contacts (this is a
 * cache - the server is SSOT, so it is replaced wholesale on every fetch). Includes
 * the blocklist: [level] is "ally" | "friend" | "imposter", see ContactLevel.
 *
 * No FK to Users on purpose: a contact can be someone whose Steam profile has not been
 * fetched yet, and a missing row here must not depend on one.
 */
@Entity(
    primaryKeys = ["serverId", "steamId"],
    foreignKeys = [
        ForeignKey(
            entity = Servers::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Contacts(
    val serverId: Long,
    val steamId: Long,
    val level: String,
    val since: Long
)
