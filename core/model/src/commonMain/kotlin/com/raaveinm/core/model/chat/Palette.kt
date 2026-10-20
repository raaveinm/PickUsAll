package com.raaveinm.core.model.chat

import com.raaveinm.core.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class Palette(
    override val id: Long,
    val name: String,
    val members: List<User>,
//    val owner: User,
//    val icon: String? = null,
    override val lastMessage: String? = null,
    override val listMessageData: List<MessageData> = emptyList()
) : Conversation
