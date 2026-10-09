package com.raaveinm.picasso

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.raaveinm.core.database.PicassoDatabase
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.model.chat.MessageStatus
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.picasso.data.repository.ChatEvent
import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.ContactsRepository
import com.raaveinm.picasso.data.repository.ConversationResult
import com.raaveinm.picasso.data.repository.ProfileHydrator
import com.raaveinm.picasso.data.repository.SocialResult
import com.raaveinm.picasso.data.server.ChatAck
import com.raaveinm.picasso.data.server.ChatEnvelope
import com.raaveinm.picasso.data.server.ChatFrameType
import com.raaveinm.picasso.data.server.ChatNack
import com.raaveinm.picasso.data.server.ChatTransport
import com.raaveinm.picasso.data.server.ContactsApi
import com.raaveinm.picasso.data.server.ConversationsApi
import com.raaveinm.picasso.data.server.IncomingChat
import com.raaveinm.picasso.data.server.MessageDeleted
import com.raaveinm.picasso.data.server.ServerContext
import com.raaveinm.picasso.data.server.ServerContextSource
import com.raaveinm.picasso.data.server.WireMessage
import com.raaveinm.picasso.data.sync.ConnectionState
import com.raaveinm.picasso.data.sync.SyncCoordinator
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChatIntegrationTest {
    private val serverAddress: String? = TestServer.address
    private val actors = mutableListOf<Actor>()

    @BeforeTest
    fun requireServer() {
        assumeTrue("PICASSO_TEST_SERVER is not set", serverAddress != null)
    }

    @AfterTest
    fun cleanUp() {
        actors.forEach { it.close() }
        actors.clear()
    }

    ///////////////////////////////////////////////
    // Contacts
    ///////////////////////////////////////////////

    @Test
    fun `an ally request travels to the other side and accepting makes both contacts`() = runBlocking {
        val alice = newActor()
        val bob = newActor()

        assertEquals(SocialResult.PENDING, alice.contacts.request(bob.steamId))
        assertTrue(bob.contacts.refresh(bob.context))
        val incoming = bob.db.getContactDao().observeRequests(1, incoming = true).first()
        assertEquals(listOf(alice.steamId), incoming.map { it.request.steamId })

        assertEquals(SocialResult.OK, bob.contacts.accept(alice.steamId))

        alice.contacts.refresh(alice.context)
        for ((actor, other) in listOf(alice to bob, bob to alice)) {
            val rows = actor.db.getContactDao().observeContacts(1).first()
            assertEquals(listOf(other.steamId), rows.map { it.contact.steamId })
            assertEquals("ally", rows.single().contact.level)
        }
        Unit
    }

    @Test
    fun `a blocked person is told the request is pending and nothing arrives`() = runBlocking {
        val alice = newActor()
        val bob = newActor()
        assertEquals(SocialResult.OK, bob.contacts.setLevel(alice.steamId, ContactLevel.IMPOSTER))

        // same answer as any other request - the block must not be discoverable
        assertEquals(SocialResult.PENDING, alice.contacts.request(bob.steamId))

        bob.contacts.refresh(bob.context)
        assertTrue(bob.db.getContactDao().observeRequests(1, incoming = true).first().isEmpty())
        assertEquals(SocialResult.UNBLOCK_FIRST, bob.contacts.request(alice.steamId))
    }

    ///////////////////////////////////////////////
    // Conversations
    ///////////////////////////////////////////////

    @Test
    fun `a dm needs allies, is shared by both sides and freezes on removal`() = runBlocking {
        val alice = newActor()
        val bob = newActor()

        assertEquals(ConversationResult.NotAllowed, alice.chat.createDm(bob.steamId))   // strangers

        befriend(alice, bob)
        val mine = alice.chat.createDm(bob.steamId) as ConversationResult.Ready
        val theirs = bob.chat.createDm(alice.steamId) as ConversationResult.Ready

        val aliceRow = alice.db.getChatDao().observeChats().first().single()
        val bobRow = bob.db.getChatDao().observeChats().first().single()
        assertEquals(aliceRow.conversation.remoteId, bobRow.conversation.remoteId)       // ONE conversation, server-issued id
        assertTrue(aliceRow.conversation.remoteId > 0)
        assertEquals(bob.steamId, aliceRow.chat.chatTitleSteamId)
        assertEquals(alice.steamId, bobRow.chat.chatTitleSteamId)
        assertTrue(aliceRow.conversation.writable)
        assertNotNull(mine.localId)
        assertNotNull(theirs.localId)

        // removing the contact ends it for both; the dm stays, read-only
        assertEquals(SocialResult.OK, alice.contacts.remove(bob.steamId))
        assertEquals(false, alice.db.getChatDao().observeChats().first().single().conversation.writable)
        assertEquals(ConversationResult.NotAllowed, bob.chat.createDm(alice.steamId))
        Unit
    }

    @Test
    fun `a palette invitation has to be accepted before anyone is a member`() = runBlocking {
        val alice = newActor()
        val bob = newActor()
        val stranger = newActor()
        befriend(alice, bob)

        // everyone invited must be an ally - one stranger and nothing is created
        assertEquals(ConversationResult.NotAllowed, alice.chat.createPalette("squad", listOf(bob.steamId, stranger.steamId)))
        assertTrue(alice.db.getChatDao().observePalettes().first().isEmpty())

        val created = alice.chat.createPalette("squad", listOf(bob.steamId)) as ConversationResult.Ready
        val palette = alice.db.getChatDao().observePalettes().first().single()
        assertEquals(listOf(alice.steamId), palette.members.map { it.steamId })    // creator only: bob is pending
        assertNotNull(created.localId)

        bob.contacts.refresh(bob.context)
        val invite = bob.db.getContactDao().observePaletteInvites(1).first().single().invite
        assertEquals("squad", invite.name)
        assertEquals(alice.steamId, invite.inviterSteamId)
        assertTrue(bob.db.getChatDao().observePalettes().first().isEmpty())         // an invite is not a conversation

        assertEquals(SocialResult.OK, bob.contacts.acceptPaletteInvite(invite.conversationRemoteId))

        val joined = bob.db.getChatDao().observePalettes().first().single()
        assertEquals(setOf(alice.steamId, bob.steamId), joined.members.map { it.steamId }.toSet())
        assertTrue(bob.db.getContactDao().observePaletteInvites(1).first().isEmpty())
        Unit
    }

    ///////////////////////////////////////////////
    // The outbox, with the server's frames scripted
    ///////////////////////////////////////////////

    @Test
    fun `a sent message waits as pending, is acknowledged once, and incoming ones interleave`() = runBlocking {
        val (alice, bob, conversation) = dmBetweenAllies()
        alice.transport.simulateConnected()

        val localId = alice.chat.sendMessage(conversation.localId, alice.steamId, "hello")

        // out on the socket: the SERVER's conversation id, a client-minted UUID, and no sender
        val frame = alice.transport.sent.single()
        assertEquals(ChatFrameType.CHAT_MESSAGE, frame.type)
        assertEquals(conversation.remoteId.toString(), frame.chatMessage!!.conversationId)
        assertEquals("hello", frame.chatMessage!!.body)
        val clientId = frame.chatMessage!!.clientMessageId
        assertEquals(36, clientId.length)
        var row = alice.db.getChatDao().getMessage(localId)!!
        assertEquals(MessageStatus.PENDING, row.status)
        assertTrue(row.timestamp > 1_700_000_000_000L)          // milliseconds, not seconds

        val ack = ChatAck(
            clientMessageId = clientId,
            message = WireMessage("500", conversation.remoteId.toString(), alice.steamId.toString(), clientId, "hello", 1_790_000_000_000)
        )
        alice.chat.onAck(alice.context, ack)
        alice.chat.onAck(alice.context, ack.copy(duplicate = true))     // a retried send's second ack

        row = alice.db.getChatDao().getMessage(localId)!!
        assertEquals(MessageStatus.SENT, row.status)
        assertEquals(500L, row.remoteId)
        assertEquals(1_790_000_000_000, row.timestamp)                   // the server's clock replaces the device's
        assertEquals(1, alice.db.getChatDao().countMessages(conversation.localId))

        // bob's reply arrives as a live frame
        alice.chat.onIncoming(
            alice.context,
            IncomingChat(WireMessage("501", conversation.remoteId.toString(), bob.steamId.toString(), "bob-1", "hi!", 1_790_000_001_000))
        )
        val history = alice.db.getChatDao().getChatHistory(conversation.localId, 0, 10).map { it.message.textMessage }
        assertEquals(listOf("hi!", "hello"), history)
        assertEquals("hi!", alice.db.getChatDao().observeChats().first().single().conversation.lastMessage)
        Unit
    }

    @Test
    fun `a message sent offline is not failed and goes out after the reconnect`() = runBlocking {
        val (alice, _, conversation) = dmBetweenAllies()

        val localId = alice.chat.sendMessage(conversation.localId, alice.steamId, "later")     // no socket at all

        assertEquals(MessageStatus.PENDING, alice.db.getChatDao().getMessage(localId)!!.status)
        assertTrue(alice.transport.sent.isEmpty())

        alice.transport.simulateConnected()
        alice.chat.resendPending(alice.context)

        assertEquals("later", alice.transport.sent.single().chatMessage!!.body)
        assertEquals(MessageStatus.PENDING, alice.db.getChatDao().getMessage(localId)!!.status)   // still waiting for the ack
    }

    @Test
    fun `a permanent refusal fails the message and a retry queues it again`() = runBlocking {
        val (alice, _, conversation) = dmBetweenAllies()
        alice.transport.simulateConnected()
        val localId = alice.chat.sendMessage(conversation.localId, alice.steamId, "no")
        val clientId = alice.transport.sent.single().chatMessage!!.clientMessageId
        val rejected = async(start = CoroutineStart.UNDISPATCHED) { alice.chat.events.first() }

        alice.chat.onNack(ChatNack(clientMessageId = clientId, code = "rate_limited"))
        assertEquals(MessageStatus.PENDING, alice.db.getChatDao().getMessage(localId)!!.status)   // transient: stays queued

        alice.chat.onNack(ChatNack(clientMessageId = clientId, code = "not_allowed"))
        assertEquals(MessageStatus.FAILED, alice.db.getChatDao().getMessage(localId)!!.status)
        assertEquals(ChatEvent.SendRejected("not_allowed"), withTimeout(2_000) { rejected.await() })

        alice.chat.retryFailed(localId)
        assertEquals(MessageStatus.PENDING, alice.db.getChatDao().getMessage(localId)!!.status)
        assertEquals(2, alice.transport.sent.size)
        assertEquals(clientId, alice.transport.sent.last().chatMessage!!.clientMessageId)          // the SAME idempotency key
    }

    @Test
    fun `a delete frame removes the message and an unsent one can be dropped locally`() = runBlocking {
        val (alice, bob, conversation) = dmBetweenAllies()
        alice.chat.onIncoming(
            alice.context,
            IncomingChat(WireMessage("700", conversation.remoteId.toString(), bob.steamId.toString(), "b1", "oops", 1_790_000_000_000))
        )
        assertEquals(1, alice.db.getChatDao().countMessages(conversation.localId))

        alice.chat.onDeleted(alice.context, MessageDeleted(conversation.remoteId.toString(), "700"))
        assertEquals(0, alice.db.getChatDao().countMessages(conversation.localId))

        val unsent = alice.chat.sendMessage(conversation.localId, alice.steamId, "never sent")
        assertTrue(alice.chat.deleteMessage(unsent))
        assertEquals(0, alice.db.getChatDao().countMessages(conversation.localId))
        Unit
    }

    @Test
    fun `a message for a conversation never heard of does not crash the stream`() = runBlocking {
        val alice = newActor()

        alice.chat.onIncoming(
            alice.context,
            IncomingChat(WireMessage("1", "999999", alice.steamId.toString(), "x", "orphan", 1))
        )

        assertTrue(alice.db.getChatDao().observeChats().first().isEmpty())
    }

    ///////////////////////////////////////////////
    // The connection lifecycle
    ///////////////////////////////////////////////

    @Test
    fun `the coordinator connects, catches up, resends pending, goes live and reconnects when the socket drops`() = runBlocking {
        val (alice, _, conversation) = dmBetweenAllies()
        val queued = alice.chat.sendMessage(conversation.localId, alice.steamId, "queued while offline")
        assertTrue(alice.transport.sent.isEmpty())

        val scope = CoroutineScope(Dispatchers.Default + Job())
        try {
            val coordinator = SyncCoordinator(
                activeServer = alice.source,
                socket = alice.transport,
                chatRepository = alice.chat,
                contactsRepository = alice.contacts,
                profiles = ProfileHydrator { },
                scope = scope
            )
            coordinator.start()

            // live even if the server has no /sync yet: a failed catch-up must not cost the connection
            withTimeout(15_000) { coordinator.state.first { it == ConnectionState.LIVE } }
            assertEquals(1, alice.transport.connectCalls)
            assertEquals("queued while offline", alice.transport.sent.single().chatMessage!!.body)
            assertEquals(MessageStatus.PENDING, alice.db.getChatDao().getMessage(queued)!!.status)

            // a frame through the real dispatch path: the ack settles the queued message
            val clientId = alice.transport.sent.single().chatMessage!!.clientMessageId
            alice.transport.deliver(
                ChatEnvelope(
                    type = ChatFrameType.CHAT_ACK,
                    chatAck = ChatAck(
                        clientMessageId = clientId,
                        message = WireMessage("900", conversation.remoteId.toString(), alice.steamId.toString(), clientId, "queued while offline", 1_790_000_000_000)
                    )
                )
            )
            withTimeout(5_000) {
                while (alice.db.getChatDao().getMessage(queued)!!.status != MessageStatus.SENT) kotlinx.coroutines.delay(50)
            }

            // the socket drops: back off, redial, catch up again
            alice.transport.drop()
            withTimeout(5_000) { coordinator.state.first { it != ConnectionState.LIVE } }
            withTimeout(20_000) { coordinator.state.first { it == ConnectionState.LIVE } }
            assertEquals(2, alice.transport.connectCalls)
        } finally {
            scope.cancel()
        }
    }

    ///////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////

    private data class Dm(val localId: Long, val remoteId: Long)

    private suspend fun befriend(first: Actor, second: Actor) {
        assertEquals(SocialResult.PENDING, first.contacts.request(second.steamId))
        assertEquals(SocialResult.OK, second.contacts.accept(first.steamId))
        first.contacts.refresh(first.context)
    }

    private suspend fun dmBetweenAllies(): Triple<Actor, Actor, Dm> {
        val alice = newActor()
        val bob = newActor()
        befriend(alice, bob)
        val ready = alice.chat.createDm(bob.steamId) as ConversationResult.Ready
        val remoteId = alice.db.getChatDao().remoteIdOf(ready.localId)!!
        return Triple(alice, bob, Dm(ready.localId, remoteId))
    }

    private suspend fun newActor(): Actor {
        return Actor(TestServer.nextUser(), serverAddress!!).also { actors += it; it.open() }
    }

    /** One user with a device of their own: own Room database, own scripted socket, real HTTP to the server. */
    private class Actor(val n: Int, address: String) {
        val steamId = 76561198000000000L + n
        val context = ServerContext(
            serverId = 1,
            baseUrl = "http://$address",
            wsUrl = "ws://$address/ws",
            token = "tok-$n",
            selfSteamId = steamId
        )
        val source = object : ServerContextSource {
            override val context: Flow<ServerContext?> = MutableStateFlow<ServerContext?>(this@Actor.context)
        }
        val transport = ScriptedTransport()

        private val dbFile: File = File.createTempFile("picasso-it-$n", ".db")
        val db: PicassoDatabase = Room.databaseBuilder<PicassoDatabase>(name = dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        private val http = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val chat = ChatRepository(db.getChatDao(), db.getServerDao(), source, ConversationsApi(http), transport)
        val contacts = ContactsRepository(
            db.getContactDao(), db.getChatDao(), source, ContactsApi(http), ConversationsApi(http), chat
        )

        suspend fun open() {
            db.getServerDao().addServer(Servers(id = 1, url = context.baseUrl.removePrefix("http://"), name = "it", added = 0))
        }

        fun close() {
            http.close()
            db.close()
            dbFile.delete()
        }
    }

    /** A WebSocket the test drives by hand: records what is sent, lets the test deliver frames and drop the line. */
    private class ScriptedTransport : ChatTransport {
        private val connected = MutableStateFlow(false)
        private val incoming = MutableSharedFlow<ChatEnvelope>(extraBufferCapacity = 64)
        val sent = mutableListOf<ChatEnvelope>()
        var connectCalls = 0

        override val isConnected: StateFlow<Boolean> = connected
        override val frames: Flow<ChatEnvelope> = incoming

        override suspend fun awaitSubscriber() {
            incoming.subscriptionCount.first { it > 0 }
        }

        override suspend fun connect(context: ServerContext) {
            connectCalls++
            connected.value = true
        }

        override suspend fun send(envelope: ChatEnvelope): Boolean {
            if (!connected.value) return false
            sent += envelope
            return true
        }

        override fun close() {
            connected.value = false
        }

        fun simulateConnected() {
            connected.value = true
        }

        fun drop() {
            connected.value = false
        }

        suspend fun deliver(envelope: ChatEnvelope) = incoming.emit(envelope)
    }
}
