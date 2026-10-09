package com.raaveinm.core.database.entities.chat

import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.MessageData as MessageDataDto
import com.raaveinm.core.model.chat.Palette
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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

/** Room stores epoch MILLISECONDS (the wire unit); the UI model wants a display-ready "HH:mm". */
private fun Long.toDisplayTime(): String {
    val localDateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}
