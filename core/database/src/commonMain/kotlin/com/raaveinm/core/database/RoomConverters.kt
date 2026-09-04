package com.raaveinm.core.database

import androidx.room3.ColumnTypeConverter
import com.raaveinm.core.model.chat.MessageStatus

class RoomConverters {
    @ColumnTypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @ColumnTypeConverter
    fun toMessageStatus(value: String): MessageStatus = MessageStatus.valueOf(value)
}
