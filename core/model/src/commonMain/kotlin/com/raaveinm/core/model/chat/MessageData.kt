package com.raaveinm.core.model.chat

import com.raaveinm.core.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class MessageData(
    val user: User,
    val textMessage: String,
    val timestamp: String
)
