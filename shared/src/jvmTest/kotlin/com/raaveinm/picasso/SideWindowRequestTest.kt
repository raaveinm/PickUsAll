package com.raaveinm.picasso

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.raaveinm.core.database.PicassoDatabase
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.datastore.auth.AuthTokenStore
import com.raaveinm.core.datastore.settings.ServerSettingsStore
import com.raaveinm.features.impl_webrtc.signaling.SignalingClient
import com.raaveinm.picasso.data.AuthApi
import com.raaveinm.picasso.data.repository.AuthRepository
import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.ContactsRepository
import com.raaveinm.picasso.data.repository.ProfileHydrator
import com.raaveinm.picasso.data.server.ChatSocket
import com.raaveinm.picasso.data.server.ContactsApi
import com.raaveinm.picasso.data.server.ConversationsApi
import com.raaveinm.picasso.data.server.ServerContext
import com.raaveinm.picasso.data.server.ServerContextSource
import com.raaveinm.picasso.data.server.ServerSelection
import com.raaveinm.picasso.data.sync.ServerCacheGuard
import com.raaveinm.picasso.data.sync.SignedOutCacheGuard
import com.raaveinm.picasso.data.sync.SyncCoordinator
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.navigation.SideWindow
import com.raaveinm.picasso.ui.navigation.SideWindowRequest
import com.sun.net.httpserver.HttpServer
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.Path.Companion.toPath
import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

//
// Created by Kirill "Raaveinm" on 11/02/2026.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

/** Exercises real app state and auth polling against a local HTTP endpoint, without Steam. */
class SideWindowRequestTest {
    private val directory: File = Files.createTempDirectory("picasso-side-window").toFile()
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val database: PicassoDatabase = Room.databaseBuilder<PicassoDatabase>(
        name = File(directory, "test.db").absolutePath,
    ).setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()
    private val settings: ServerSettingsStore = ServerSettingsStore(preferences("settings"))
    private val tokens: AuthTokenStore = AuthTokenStore(preferences("auth"))
    private val selection: ServerSelection = ServerSelection(database.getServerDao(), settings)
    private val http: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) { json() }
    }
    private val socket: ChatSocket = ChatSocket(SignalingClient())
    private val source: ServerContextSource = object : ServerContextSource {
        override val context: Flow<ServerContext?> = flowOf(null)
    }
    private val conversations: ConversationsApi = ConversationsApi(http)
    private val contactsApi: ContactsApi = ContactsApi(http)
    private val chat: ChatRepository = ChatRepository(
        database.getChatDao(), database.getServerDao(), source, conversations, socket,
    )
    private val contacts: ContactsRepository = ContactsRepository(
        database.getContactDao(), database.getChatDao(), source, contactsApi, conversations, chat,
    )
    private val sync: SyncCoordinator = SyncCoordinator(
        activeServer = source,
        socket = socket,
        chatRepository = chat,
        contactsRepository = contacts,
        profiles = ProfileHydrator { },
        scope = scope,
    )
    private val auth: AuthRepository = AuthRepository(AuthApi(http), tokens, selection)
    private val model: AppViewModel = AppViewModel(
        authRepository = auth,
        userDao = database.getUserDao(),
        syncCoordinator = sync,
        signedOutCacheGuard = SignedOutCacheGuard(auth.session, sync, database.getAccountDao(), scope),
        serverCacheGuard = ServerCacheGuard(
            selectedServerId = selection.server.map { it?.id },
            settings = settings,
            accountDao = database.getAccountDao(),
            awaitIdle = sync::awaitIdle,
            scope = scope,
        ),
    )
    @Volatile
    private var loginResponse: String? = null
    private val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/auth/steam/poll") { exchange ->
            val response: String? = loginResponse
            exchange.use {
                if (response == null) {
                    exchange.sendResponseHeaders(204, -1L)
                } else {
                    val bytes: ByteArray = response.toByteArray(Charsets.UTF_8)
                    exchange.responseHeaders.add("Content-Type", "application/json")
                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.write(bytes)
                }
            }
        }
        start()
    }

    private fun preferences(name: String): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        scope = scope,
        produceFile = { File(directory, "$name.preferences_pb").absolutePath.toPath() },
    )

    @AfterTest
    fun close() = runBlocking {
        withContext(Dispatchers.Main) { model.viewModelScope.cancel() }
        scope.coroutineContext[Job]?.cancelAndJoin()
        server.stop(0)
        http.close()
        database.close()
        directory.deleteRecursively()
        Unit
    }

    @Test
    fun `reopening the same destination has a fresh identity and ignores old dismissals`() = runBlocking {
        withContext(Dispatchers.Main) {
            val screen: SideWindow = SideWindow.WebAuth("https://example.invalid/login", "attempt")
            val first: SideWindowRequest = model.setWindowRequest(screen)
            val second: SideWindowRequest = model.setWindowRequest(screen)

            assertNotEquals(first.id, second.id)
            model.resetSideWindowRequest(first.id)
            assertEquals(second, model.uiState.value.sideWindowRequest)
            model.resetSideWindowRequest(second.id)
            assertNull(model.uiState.value.sideWindowRequest)
        }
    }

    @Test
    fun `login presents its URL and successful polling closes its window`() = runBlocking {
        val request: SideWindowRequest = startLogin()
        val destination: SideWindow.WebAuth = assertIs<SideWindow.WebAuth>(request.destination)
        assertEquals(
            "http://127.0.0.1:${server.address.port}/auth/steam/begin?state=${destination.loginAttemptId}",
            destination.url,
        )

        completeLogin()

        assertNull(model.uiState.value.sideWindowRequest)
        assertEquals("test-token", tokens.current(1L)?.token)
    }

    @Test
    fun `closing auth cancels polling and an old close cannot cancel the next login`() = runBlocking {
        val first: SideWindowRequest = startLogin()
        withContext(Dispatchers.Main) {
            model.resetSideWindowRequest(first.id)
            assertFalse(model.uiState.value.isLoggingIn)
            assertNull(model.uiState.value.sideWindowRequest)
            model.login()
        }
        val second: SideWindowRequest = awaitRequest()
        assertNotEquals(first.id, second.id)
        withContext(Dispatchers.Main) { model.resetSideWindowRequest(first.id) }
        assertEquals(second, model.uiState.value.sideWindowRequest)
        assertTrue(model.uiState.value.isLoggingIn)

        completeLogin()
        assertNull(model.uiState.value.sideWindowRequest)
    }

    @Test
    fun `auth completion leaves a replacement call presentation open`() = runBlocking {
        startLogin()
        val call: SideWindowRequest = withContext(Dispatchers.Main) {
            model.setWindowRequest(SideWindow.IncomingCall(callId = "call-1", peerSteamId = 42L))
        }

        completeLogin()

        assertEquals(call, model.uiState.value.sideWindowRequest)
    }

    @Test
    fun `login without a configured server leaves no pending presentation`() = runBlocking {
        withContext(Dispatchers.Main) { model.login() }
        withTimeout(5_000L) { model.uiState.first { !it.isLoggingIn } }
        assertNull(model.uiState.value.sideWindowRequest)
        assertTrue(model.uiState.value.message != null)
    }

    private suspend fun startLogin(): SideWindowRequest {
        database.getServerDao().addServer(
            Servers(id = 1L, url = "127.0.0.1:${server.address.port}", name = "test", added = 0L),
        )
        withContext(Dispatchers.Main) { model.login() }
        return awaitRequest()
    }

    private suspend fun awaitRequest(): SideWindowRequest = withTimeout(5_000L) {
        requireNotNull(model.uiState.first { it.sideWindowRequest != null }.sideWindowRequest)
    }

    private suspend fun completeLogin() {
        loginResponse = """{"token":"test-token","steamId":"42","expiresAt":0}"""
        withTimeout(5_000L) { model.uiState.first { !it.isLoggingIn } }
    }
}
