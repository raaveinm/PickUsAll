package com.raaveinm.core.model.chat

import com.raaveinm.core.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class Palette(
    override val id: Long,
    val name: String,
    val members: List<User>,
    override val lastMessage: String? = null
) : Conversation
