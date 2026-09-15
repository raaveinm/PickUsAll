package com.raaveinm.picasso.ui.actions

//
// Created by Kirill "Raaveinm" on 8/31/26.
//

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object TimeConverter
{
    private val months = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    val Long.toDate: String
        get() {
            val date = Instant.fromEpochSeconds(
                epochSeconds = this,
            )
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            return "${date.day} ${months[date.month.number - 1]}, ${date.year}"
        }

    fun toEpochSeconds(timeStamp: String) : Long { // returns 1761782400
        val (day, monthName, year) = timeStamp.replace(",", "").split(" ")
        val month = months.indexOf(monthName) + 1
        require(month != 0) { "Unknown month in timestamp: $timeStamp" }
        val date = LocalDate(year.toInt(), month, day.toInt())
        return date.atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds
    }
}
