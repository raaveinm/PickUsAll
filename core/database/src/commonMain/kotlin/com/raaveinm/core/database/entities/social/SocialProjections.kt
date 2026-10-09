package com.raaveinm.core.database.entities.social

import androidx.room3.Embedded
import androidx.room3.Relation
import com.raaveinm.core.database.entities.api.user.Users

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

data class ContactWithUser(
    @Embedded val contact: Contacts,
    @Relation(parentColumns = ["steamId"], entityColumns = ["steamId"])
    val user: Users?
)

data class RequestWithUser(
    @Embedded val request: ContactRequests,
    @Relation(parentColumns = ["steamId"], entityColumns = ["steamId"])
    val user: Users?
)

data class InviteWithInviter(
    @Embedded val invite: PaletteInvites,
    @Relation(parentColumns = ["inviterSteamId"], entityColumns = ["steamId"])
    val inviter: Users?
)
