package com.raaveinm.core.model.chat

// Outbox status for a locally-authored message
enum class MessageStatus {
    PENDING,
    SENT,
    FAILED
}
