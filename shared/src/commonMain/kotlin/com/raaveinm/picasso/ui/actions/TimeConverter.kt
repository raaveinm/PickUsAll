package com.raaveinm.picasso.ui.actions

//
// Created by Kirill "Raaveinm" on 8/31/26.
//

import com.raaveinm.core.model.chat.TimeStamp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object TimeConverter
{
    private fun LocalDateTime.toTimeStamp() = TimeStamp(
        year = year,
        month = month.number.toShort(),
        day = day.toShort(),
        hour = hour.toShort(),
        minutes = minute.toShort(),
        seconds = second.toShort()
    )

    fun toTimeStamp(epochSeconds: Long) : TimeStamp {
        val instant = Instant.fromEpochSeconds(epochSeconds)
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).toTimeStamp()
    }

    fun toEpochSeconds(timeStamp: TimeStamp) : Long {
        val ldt = LocalDateTime(
            timeStamp.year, timeStamp.month.toInt(), timeStamp.day.toInt(),
            timeStamp.hour.toInt(), timeStamp.minutes.toInt(), timeStamp.seconds.toInt()
        )
        return ldt.toInstant(TimeZone.currentSystemDefault()).epochSeconds
    }
}
