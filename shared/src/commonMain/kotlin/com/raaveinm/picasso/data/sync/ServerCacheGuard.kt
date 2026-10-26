package com.raaveinm.picasso.data.sync

import com.raaveinm.core.database.dao.AccountDao
import com.raaveinm.core.datastore.settings.ServerSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

//
// Created by Kirill "Raaveinm" on 10/26/26.
//

/**
 * Keeps one rule: the cached chat data (conversations, messages, contacts) belongs to exactly
 * one server - the selected one. Selecting another server therefore throws the old data away
 * ([AccountDao.wipeServerData], unsent messages included) and lets the connection resync from
 * scratch against the new server.
 *
 * The order is what makes it safe, and [readyFor] is how it is enforced: `ActiveServer` stays
 * null until the cache is prepared for the selected server, so the old connection is closed
 * first ([awaitIdle]), then the wipe runs, and only then does the new connection open - a sync
 * can never write into rows that are about to be deleted.
 *
 * Which server the cache belongs to is persisted ([ServerSettingsStore.markCacheOwner]) and
 * marked only after the wipe, so an app killed halfway through a switch finishes it on the
 * next start instead of showing one server's chats under another. Signing out is a separate
 * rule with a bigger wipe: see [SignedOutCacheGuard].
 */
class ServerCacheGuard(
    private val selectedServerId: Flow<Long?>,
    private val settings: ServerSettingsStore,
    private val accountDao: AccountDao,
    private val awaitIdle: suspend () -> Unit, // returns once no connection loop is running; a lambda to break the ActiveServer <-> SyncCoordinator cycle
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _readyFor = MutableStateFlow<Long?>(null)

    /** The server the cache is clean for; null while a switch is in progress or nothing is selected. */
    val readyFor: StateFlow<Long?> = _readyFor

    private var job: Job? = null

    /** Idempotent. Called once from the app shell, before [SyncCoordinator.start]. */
    fun start() {
        if (job != null) return
        job = scope.launch {
            // collectLatest: a second switch while the first is still waiting for the old
            // connection simply replaces it - nothing has been wiped yet at that point.
            selectedServerId.distinctUntilChanged().collectLatest { serverId -> prepare(serverId) }
        }
    }

    private suspend fun prepare(serverId: Long?) {
        _readyFor.value = null
        if (serverId == null) return

        while (true) {
            try {
                if (settings.settings.first().cachedServerId != serverId) {
                    awaitIdle()
                    accountDao.wipeServerData()
                    settings.markCacheOwner(serverId)
                }
                _readyFor.value = serverId
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("ServerCacheGuard: preparing the cache for server $serverId failed, retrying: $e")
                delay(RETRY_SECONDS.seconds)
            }
        }
    }

    private companion object {
        const val RETRY_SECONDS = 2
    }
}
