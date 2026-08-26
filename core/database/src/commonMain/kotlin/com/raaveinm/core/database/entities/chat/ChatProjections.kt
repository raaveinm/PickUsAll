package com.raaveinm.core.database.entities.chat

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import com.raaveinm.core.database.entities.api.user.Users

// Room can't map a sum type (Chat | Palette) out of one query, so the chat list is
// read as two separate flows - one per conversation kind - and merged by the caller.

data class ChatWithTitle(
    @Embedded val conversation: Conversations,
    @Embedded val chat: Chats,
    @Relation(parentColumns = ["chatTitleSteamId"], entityColumns = ["steamId"])
    val titleUser: Users
)

data class PaletteWithMembers(
    @Embedded val conversation: Conversations,
    @Embedded val palette: Palettes,
    @Relation(
        parentColumns = ["conversationId"],
        entityColumns = ["steamId"],
        associateBy = Junction(
            value = PaletteMembers::class,
            parentColumns = ["paletteConversationId"],
            entityColumns = ["userSteamId"]
        )
    )
    val members: List<Users>
)
