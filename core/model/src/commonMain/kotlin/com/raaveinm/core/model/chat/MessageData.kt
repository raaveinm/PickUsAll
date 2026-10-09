package com.raaveinm.core.model.chat

import com.raaveinm.core.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class MessageData(
    val user: User,
    val textMessage: String,
    val timestamp: String,
    val status: MessageStatus = MessageStatus.SENT,
    /** Room row id, so the UI can address one message (retry, delete). 0 for mocks and previews. */
    val localId: Long = 0,
    /** The server's id; null until the server has acknowledged the message. */
    val remoteId: Long? = null
)
