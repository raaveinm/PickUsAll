package com.raaveinm.picasso.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.database.entities.server.toModel
import com.raaveinm.core.model.ServerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SettingsViewModel(
    private val serverDao: ServerDao
) : ViewModel() {

    val serverStates: StateFlow<List<ServerState>> = serverDao.getAllServers()
        .map { servers -> servers.map { it.toModel(reachable = null) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalTime::class)
    fun addServer(url: String, name: String) {
        viewModelScope.launch {
            serverDao.addServer(
                Servers(
                    id = 0,
                    url = url,
                    name = name.takeIf { it.isNotBlank() },
                    added = Clock.System.now().epochSeconds
                )
            )
        }
    }

    fun updateServer(server: ServerState, url: String, name: String) {
        viewModelScope.launch {
            serverDao.updateServer(
                Servers(
                    id = server.id,
                    url = url,
                    name = name.takeIf { it.isNotBlank() },
                    added = server.addedAt
                )
            )
        }
    }

    fun deleteServer(server: ServerState) {
        viewModelScope.launch {
            serverDao.deleteServer(
                Servers(
                    id = server.id,
                    url = server.url,
                    name = server.name,
                    added = server.addedAt
                )
            )
        }
    }

    /**
     * Template for a real reachability probe once picassobackend exists to answer one.
     * Not wired to any call site yet — every server's `reachable` stays null (unknown)
     * until this is implemented and hooked up (e.g. called from [serverStates]'s mapping).
     */
    suspend fun pingServer(server: ServerState): Boolean? {
        // TODO: replace with a real network probe against server.url
        return null
    }
}
