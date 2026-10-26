package com.raaveinm.picasso

//
// Created by Kirill "Raaveinm" on 10/26/26.
//

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.raaveinm.core.database.PicassoDatabase
import com.raaveinm.core.database.entities.chat.ServerConversation
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.datastore.auth.createSettingsDataStore
import com.raaveinm.core.datastore.settings.ServerSettingsStore
import com.raaveinm.picasso.data.sync.ServerCacheGuard
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/*
 * Switching the selected server must close the old connection, wipe the old server's chat
 * data, and only then declare the cache ready for the new one. Real Room (the wipe has to
 * get past the foreign keys) and a real settings DataStore (the cache owner is persisted).
 */
class ServerCacheGuardTest {
    private val self = 100L
    private val friend = 200L

    private val dbFile: File = File.createTempFile("picasso-guard-test", ".db")
    private val db: PicassoDatabase = Room.databaseBuilder<PicassoDatabase>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
    private val settingsFile: File = File.createTempFile("picasso-guard-settings", ".preferences_pb").also { it.delete() }
    private val settings = ServerSettingsStore(createSettingsDataStore { settingsFile.absolutePath })
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val selected = MutableStateFlow<Long?>(1L)

    @AfterTest
    fun tearDown() {
        scope.cancel()
        db.close()
        dbFile.delete()
        settingsFile.delete()
    }

    private fun guard(awaitIdle: suspend () -> Unit = {}) = ServerCacheGuard(
        selectedServerId = selected,
        settings = settings,
        accountDao = db.getAccountDao(),
        awaitIdle = awaitIdle,
        scope = scope
    )

    private suspend fun seedServersAndChat() {
        db.getServerDao().addServer(Servers(id = 1, url = "one.invalid", name = "one", added = 0))
        db.getServerDao().addServer(Servers(id = 2, url = "two.invalid", name = "two", added = 0))
        db.getChatDao().upsertServerConversation(
            1L,
            ServerConversation(remoteId = 10, isDm = true, name = null, members = listOf(self, friend), writable = true),
            self
        )
    }

    private suspend fun conversationsOfServerOne(): Int = db.getChatDao().observeChats().first().size

    @Test
    fun `the cache that already belongs to the selected server is left alone`() = runBlocking {
        seedServersAndChat()
        settings.markCacheOwner(1L)
        val guard = guard()

        guard.start()
        withTimeout(5_000) { guard.readyFor.first { it == 1L } }

        assertEquals(1, conversationsOfServerOne())
    }

    @Test
    fun `switching waits for the old connection, wipes its data and then reports the new server`() = runBlocking {
        seedServersAndChat()
        settings.markCacheOwner(1L)
        val connectionClosed = CompletableDeferred<Unit>()
        val guard = guard(awaitIdle = { connectionClosed.await() })
        guard.start()
        withTimeout(5_000) { guard.readyFor.first { it == 1L } }

        selected.value = 2L
        withTimeout(5_000) { guard.readyFor.first { it == null } }       // not ready for 1 any more, not yet for 2
        delay(200)
        assertNull(guard.readyFor.value)                                  // still waiting on the old connection
        assertEquals(1, conversationsOfServerOne())                       // nothing wiped under a live connection

        connectionClosed.complete(Unit)
        withTimeout(5_000) { guard.readyFor.first { it == 2L } }

        assertEquals(0, conversationsOfServerOne())
        assertEquals(2L, settings.settings.first().cachedServerId)
    }

    @Test
    fun `a cache with no known owner is wiped before the first connection`() = runBlocking {
        seedServersAndChat()                                              // cachedServerId was never recorded
        val guard = guard()

        guard.start()
        withTimeout(5_000) { guard.readyFor.first { it == 1L } }

        assertEquals(0, conversationsOfServerOne())
        assertEquals(1L, settings.settings.first().cachedServerId)
    }

    @Test
    fun `nothing selected means nothing is ready`() = runBlocking {
        selected.value = null
        val guard = guard()

        guard.start()
        delay(200)

        assertNull(guard.readyFor.value)
        assertTrue(conversationsOfServerOne() == 0)
    }
}
