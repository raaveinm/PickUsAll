package com.raaveinm.picasso.ui.settings.viewmodel

import androidx.compose.ui.input.key.Key
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.database.entities.server.toModel
import com.raaveinm.core.model.ServerState
import com.raaveinm.core.model.toHttpBaseUrl
import com.raaveinm.picasso.data.repository.KeyBindingRepository
import com.raaveinm.pickusall.core.designsystem.keybinding.Commands
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyChord
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyMap
import com.raaveinm.pickusall.core.designsystem.keybinding.RebindProblem
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val httpClient: HttpClient,
    private val keyBindingRepository: KeyBindingRepository
) : ViewModel() {
    private val pingResults = MutableStateFlow<Map<Long, PingResult>>(emptyMap())
    private val recordingFor = MutableStateFlow<Commands?>(null)
    private val notice = MutableStateFlow<Notice?>(null)

    val behaviourState: StateFlow<BehaviourState> = combine(
        keyBindingRepository.keyMap,
        recordingFor,
        notice
    ) { keyMap, recording, lastNotice ->
        BehaviourState(
            bindings = Commands.entries.mapNotNull { command ->
                val chord: KeyChord = keyMap.chordFor(command) ?: return@mapNotNull null
                ChordBindings(
                    command = command,
                    chord = chord,
                    isDefault = chord == KeyMap.defaultChordFor(command)
                )
            },
            recordingFor = recording,
            notice = lastNotice
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BehaviourState()
    )

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
    // Key Bindings
    ///////////////////////////////////////////////

    fun startRecording(command: Commands) {
        recordingFor.value = command
        notice.value = null
    }

    fun cancelRecording() {
        recordingFor.value = null
        notice.value = Notice.Cancelled
    }

    /**
     * Receives the chord pressed while [recordingFor] is set. Plain Escape is reserved for aborting,
     * so it can never be bound. Anything [KeyMap.check] refuses ends the attempt with a [Notice.Rejected]
     * instead of being saved; the user hits "Rebind" again to retry.
     */
    fun onChordCaptured(chord: KeyChord) {
        val command: Commands = recordingFor.value ?: return
        recordingFor.value = null // cleared synchronously: a second key event must not start a second save

        if (chord == KeyChord(Key.Escape, isPrimary = false, isShift = false, isAlt = false, isControl = false)) {
            notice.value = Notice.Cancelled
            return
        }

        viewModelScope.launch {
            val problem: RebindProblem? = keyBindingRepository.keyMap.first().check(command, chord)
            if (problem != null) {
                notice.value = Notice.Rejected(command, chord, problem)
                return@launch
            }
            keyBindingRepository.rebind(command, chord)
            notice.value = Notice.Rebound(command, chord)
        }
    }

    fun resetBinding(command: Commands) {
        recordingFor.value = null
        viewModelScope.launch {
            keyBindingRepository.reset(command)
            KeyMap.defaultChordFor(command)?.let { notice.value = Notice.Reset(command, it) }
        }
    }

    /** Called when the Behaviour screen goes away, so a half-finished recording doesn't greet the user next visit. */
    fun clearKeyBindingFeedback() {
        recordingFor.value = null
        notice.value = null
    }

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
    fun pingServer(server: ServerState, postMessage: (WarnLevel, String) -> Unit = { _,_-> }) {
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
                } catch (_: Exception) {
                    postMessage(WarnLevel.WARN, "SERVER REACHABILITY ERROR: sp_e100")
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
