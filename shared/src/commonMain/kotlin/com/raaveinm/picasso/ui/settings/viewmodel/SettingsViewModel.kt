package com.raaveinm.picasso.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.database.entities.server.toModel
import com.raaveinm.core.model.ServerState
import com.raaveinm.core.model.toHttpBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

class SettingsViewModel(
    private val serverDao: ServerDao,
    private val httpClient: HttpClient
) : ViewModel() {

    private val pingResults = MutableStateFlow<Map<Long, PingResult>>(emptyMap())

    val serverStates: StateFlow<List<ServerState>> = combine(
        serverDao.getAllServers(),
        pingResults
    ) { servers, pings ->
        servers.map { server ->
            val result = pings[server.id]
            server.toModel(reachable = result?.reachable).apply { ping = result?.ping }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    ///////////////////////////////////////////////
    // Server Manipulation
    ///////////////////////////////////////////////

    @OptIn(ExperimentalTime::class)
    fun addServer(url: String, name: String) {
        viewModelScope.launch {
            serverDao.addServer(
                Servers(
                    id = 0,
                    url = url,
                    name = name.takeIf { it.isNotBlank() },
                    added = System.now().epochSeconds
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

    ///////////////////////////////////////////////
    // Server Pings
    ///////////////////////////////////////////////

    /**
     * Fired from [com.raaveinm.picasso.ui.settings.screens.ServerScreen]'s ping button.
     * Result is published into [pingResults], which [serverStates] combines back in —
     * mutating [server]'s own `var`s wouldn't do anything, since it's a plain data class
     * snapshot handed to Compose, not observed state.
     */
    @OptIn(ExperimentalTime::class)
    fun pingServer(server: ServerState) {
        pingResults.update { it + (server.id to PingResult(reachable = null, ping = null)) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                var elapsed = 0
                val reachable = try {
                    val timeStamp = System.now()
                    val res = withTimeoutOrNull(7000.milliseconds) {
                        httpClient.get("${server.url.toHttpBaseUrl()}/ping").status.value in 200..299
                    } ?: false
                    elapsed = (System.now().toEpochMilliseconds() - timeStamp.toEpochMilliseconds()).toInt()
                    res
                } catch (_: Exception) { // TODO pass as an exception to AppViewModel to print for user
                    false
                }
                PingResult(reachable = reachable, ping = if (reachable) elapsed else null)
            }
            pingResults.update { it + (server.id to result) }
            serverDao.updatePing(server.id, result.ping)
        }
    }

    private data class PingResult(val reachable: Boolean?, val ping: Int?)

    ///////////////////////////////////////////////
    // Server read-only
    ///////////////////////////////////////////////

    suspend fun getReachableServer(): ServerState? =
        serverDao.getReachableServers().firstOrNull()?.toModel()
}
