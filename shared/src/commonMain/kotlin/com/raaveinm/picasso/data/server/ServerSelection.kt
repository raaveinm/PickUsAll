package com.raaveinm.picasso.data.server

import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.datastore.settings.ServerSettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

//
// Created by Kirill "Raaveinm" on 10/26/26.
//

/**
 * [storedId] only counts while its server still exists - a deleted server's id may linger in the
 * datastore. [serverIds] is in `ORDER BY added DESC` order, so the fallback is the newest server.
 */
fun effectiveSelectedId(serverIds: List<Long>, storedId: Long?): Long? =
    storedId?.takeIf { it in serverIds } ?: serverIds.firstOrNull()

/**
 * The one answer to "which server is the app talking to": the server the user picked in
 * Settings / Server. Login ([com.raaveinm.picasso.data.repository.AuthRepository]) and the
 * connection ([ActiveServer]) both read it from here, so they can never disagree.
 * Servers with a blank url are never offered.
 */
class ServerSelection(
    serverDao: ServerDao,
    serverSettingsStore: ServerSettingsStore
) {
    val server: Flow<Servers?> = combine(
        serverDao.getAllServers(),
        serverSettingsStore.settings
    ) { servers, settings ->
        val usable: List<Servers> = servers.filter { it.url.isNotBlank() }
        val selectedId: Long? = effectiveSelectedId(usable.map { it.id }, settings.selectedServerId)
        usable.firstOrNull { it.id == selectedId }
    }.distinctUntilChanged()

    suspend fun current(): Servers? = server.first()
}
