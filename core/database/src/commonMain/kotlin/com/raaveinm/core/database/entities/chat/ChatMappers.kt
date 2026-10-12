package com.raaveinm.core.database.entities.chat

import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.MessageData as MessageDataDto
import com.raaveinm.core.model.chat.Palette
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * `core/model.chat.*` is a UI/domain model (embeds full `User` objects for the
 * screens to render directly), not a network wire DTO. There is deliberately no
 * UI-model -> entity direction any more: conversations enter Room from the server
 * (`ChatDao.upsertServerConversation`), so a mapper that invented `remoteId = id`
 * would only reintroduce the placeholder ids the server now replaces.
 *
 * Reading the chat list back out: listMessageData is intentionally left empty here -
 * the list view only needs lastMessage, full history is loaded per opened conversation.
 */
fun ChatWithTitle.toDto(): Chat = Chat(
    id = conversation.id,
    chatTitle = titleUser.toDto(),
    lastMessage = conversation.lastMessage,
    writable = conversation.writable
)

fun PaletteWithMembers.toDto(): Palette = Palette(
    id = conversation.id,
    name = palette.name,
    members = members.map { it.toDto() },
    lastMessage = conversation.lastMessage
)

fun MessageWithSender.toDto(): MessageDataDto = MessageDataDto(
    user = sender.toDto(),
    textMessage = message.textMessage,
    timestamp = message.timestamp.toDisplayTime(),
    status = message.status,
    localId = message.id,
    remoteId = message.remoteId
)

private val MONTH_NAMES: Array<String> = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

/** Room stores epoch MILLISECONDS (the wire unit); the UI model wants a display-ready string. */
private fun Long.toDisplayTime(): String = formatMessageTime(
    epochMs = this,
    nowMs = Clock.System.now().toEpochMilliseconds(),
    zone = TimeZone.currentSystemDefault()
)

/**
 * Today's messages are "HH:mm"; older ones get the date in front ("12 Oct, 14:30"),
 * plus the year when it is not the current one ("12 Oct 2025, 14:30").
 * "Today" is judged in [zone], not in UTC.
 */
internal fun formatMessageTime(epochMs: Long, nowMs: Long, zone: TimeZone): String {
    val moment = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(zone)
    val today = Instant.fromEpochMilliseconds(nowMs).toLocalDateTime(zone).date
    val hourMinute: String =
        "${moment.hour.toString().padStart(2, '0')}:${moment.minute.toString().padStart(2, '0')}"

    if (moment.date == today) return hourMinute

    val dayAndMonth: String = "${moment.date.day} ${MONTH_NAMES[moment.date.month.number - 1]}"
    return if (moment.date.year == today.year) {
        "$dayAndMonth, $hourMinute"
    } else {
        "$dayAndMonth ${moment.date.year}, $hourMinute"
    }
}
