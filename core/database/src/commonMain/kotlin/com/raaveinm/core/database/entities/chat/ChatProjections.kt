package com.raaveinm.core.database.entities.chat

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import com.raaveinm.core.database.entities.api.user.Users

data class ChatWithTitle(
    @Embedded val conversation: Conversations,
    @Embedded val chat: Chats,
    @Relation(parentColumns = ["chatTitleSteamId"], entityColumns = ["steamId"])
    val titleUser: Users
)

data class MessageWithSender(
    @Embedded val message: MessageData,
    @Relation(parentColumns = ["senderSteamId"], entityColumns = ["steamId"])
    val sender: Users
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
