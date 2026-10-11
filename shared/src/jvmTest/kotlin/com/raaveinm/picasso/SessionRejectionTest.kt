package com.raaveinm.picasso

//
// Created by Kirill "Raaveinm" on 10/11/26.
//

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.raaveinm.core.database.PicassoDatabase
import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.ContactsRepository
import com.raaveinm.picasso.data.repository.ProfileHydrator
import com.raaveinm.picasso.data.server.ChatEnvelope
import com.raaveinm.picasso.data.server.ChatTransport
import com.raaveinm.picasso.data.server.ContactsApi
import com.raaveinm.picasso.data.server.ConversationsApi
import com.raaveinm.picasso.data.server.ServerContext
import com.raaveinm.picasso.data.server.ServerContextSource
import com.raaveinm.picasso.data.sync.SyncCoordinator
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
 * A session the server no longer knows must end in a sign-out, not in a reconnect loop
 * that retries the dead token forever. No server needed: the handshake always fails
 * and the probe's answer is scripted.
 */
class SessionRejectionTest {
    private val context = ServerContext(
        serverId = 1,
        baseUrl = "http://test.invalid",
        wsUrl = "ws://test.invalid/ws",
        token = "lost-token",
        selfSteamId = 76561198000000001
    )
    private val active = MutableStateFlow<ServerContext?>(context)
    private val source = object : ServerContextSource {
        override val context: Flow<ServerContext?> = active
    }

    private val dbFile: File = File.createTempFile("picasso-session-test", ".db")
    private val db: PicassoDatabase = Room.databaseBuilder<PicassoDatabase>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
    private val http = HttpClient(OkHttp)
    private val socket = RefusingTransport()
    private val chat = ChatRepository(db.getChatDao(), db.getServerDao(), source, ConversationsApi(http), socket)
    private val contacts = ContactsRepository(db.getContactDao(), db.getChatDao(), source, ContactsApi(http), ConversationsApi(http), chat)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @AfterTest
    fun tearDown() {
        scope.cancel()
        http.close()
        db.close()
        dbFile.delete()
    }

    private fun coordinator(
        rejected: Boolean,
        onRejected: suspend (ServerContext) -> Unit
    ) = SyncCoordinator(
        activeServer = source,
        socket = socket,
        chatRepository = chat,
        contactsRepository = contacts,
        profiles = ProfileHydrator { },
        isSessionRejected = { rejected },
        onSessionRejected = onRejected,
        scope = scope
    )

    @Test
    fun `a rejected session is dropped once and the coordinator stops`() = runBlocking {
        val dropped = mutableListOf<String>()
        val sync = coordinator(rejected = true) { rejected ->
            dropped += rejected.token
            active.value = null                         // what clearing the token does to ActiveServer
        }
        val signal = scope.async { sync.sessionRejected.first() }

        sync.start()
        withTimeout(5_000) { signal.await() }
        withTimeout(5_000) { sync.awaitIdle() }

        assertEquals(listOf("lost-token"), dropped)     // the exact token that was refused
        assertEquals(1, socket.attempts.get())          // no retry against a dead session
    }

    @Test
    fun `an unreachable server keeps the session and keeps retrying`() = runBlocking {
        var dropped = false
        val sync = coordinator(rejected = false) { dropped = true }

        sync.start()
        withTimeout(10_000) { while (socket.attempts.get() < 2) kotlinx.coroutines.delay(20) }

        assertTrue(!dropped)
        assertEquals(context, active.value)
    }

    /** Every handshake fails, the way a 401 on the upgrade (or no network) does. */
    private class RefusingTransport : ChatTransport {
        val attempts = AtomicInteger(0)

        override val isConnected: StateFlow<Boolean> = MutableStateFlow(false)
        override val frames: Flow<ChatEnvelope> = emptyFlow()

        override suspend fun awaitSubscriber() = Unit

        override suspend fun connect(context: ServerContext) {
            attempts.incrementAndGet()
            throw IllegalStateException("Handshake exception, expected status code 101 but was 401")
        }

        override suspend fun send(envelope: ChatEnvelope): Boolean = false

        override fun close() = Unit
    }
}
