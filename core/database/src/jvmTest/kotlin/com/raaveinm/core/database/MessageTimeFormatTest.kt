package com.raaveinm.core.database

import com.raaveinm.core.database.entities.chat.formatMessageTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageTimeFormatTest {

    private val zone: TimeZone = TimeZone.UTC

    private fun ms(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        LocalDateTime(year, month, day, hour, minute).toInstant(zone).toEpochMilliseconds()

    private val now: Long = ms(2026, 10, 12, 18, 0)

    @Test
    fun todayIsTimeOnly() {
        assertEquals("09:05", formatMessageTime(ms(2026, 10, 12, 9, 5), now, zone))
    }

    @Test
    fun earlierThisYearGetsDayAndMonth() {
        assertEquals("11 Oct, 23:59", formatMessageTime(ms(2026, 10, 11, 23, 59), now, zone))
    }

    @Test
    fun otherYearGetsYearToo() {
        assertEquals("31 Dec 2025, 00:00", formatMessageTime(ms(2025, 12, 31, 0, 0), now, zone))
    }
}
