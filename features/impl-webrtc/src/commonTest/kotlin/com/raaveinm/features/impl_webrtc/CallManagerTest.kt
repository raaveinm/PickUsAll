package com.raaveinm.features.impl_webrtc

import com.raaveinm.features.impl_webrtc.rtc.CallConnectionState
import com.raaveinm.features.impl_webrtc.rtc.CallPeer
import com.raaveinm.features.impl_webrtc.rtc.LocalIceCandidateData
import com.raaveinm.features.impl_webrtc.signaling.CallSignaling
import com.raaveinm.features.impl_webrtc.signaling.HangupPayload
import com.raaveinm.features.impl_webrtc.signaling.IceCandidatePayload
import com.raaveinm.features.impl_webrtc.signaling.IceCandidateWireData
import com.raaveinm.features.impl_webrtc.signaling.SdpPayload
import com.raaveinm.features.impl_webrtc.signaling.SignalingEnvelope
import com.raaveinm.features.impl_webrtc.signaling.SignalingMessageType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CallManagerTest {
    @Test
    fun offerWaitsForConsentWithoutCreatingMedia() = runTest {
        val fixture: Fixture = fixture()
        val call: IncomingCall = offer(fixture)

        assertFalse(fixture.manager.isInCall.value)
        assertEquals(0, fixture.created)
        assertTrue(fixture.signaling.sent.isEmpty())
        fixture.signaling.receive(sdp(SignalingMessageType.SDP_OFFER))
        runCurrent()
        assertEquals(call, fixture.manager.incomingCall.value)
    }

    @Test
    fun acceptBuffersIceUntilRemoteDescriptionAndAnswersOnlyOnce() = runTest {
        val peer: FakePeer = FakePeer().apply { answerGate = CompletableDeferred() }
        val fixture: Fixture = fixture(peer)
        val call: IncomingCall = offer(fixture)
        fixture.signaling.receive(ice("early"))
        fixture.manager.acceptIncomingCall(call.id)
        fixture.manager.acceptIncomingCall(call.id)
        runCurrent()
        fixture.signaling.receive(ice("during-setup"))
        runCurrent()

        assertEquals(1, fixture.created)
        assertNull(fixture.manager.incomingCall.value)
        assertTrue(fixture.manager.isInCall.value)
        assertTrue(peer.candidates.isEmpty())
        assertTrue(fixture.signaling.sent.isEmpty())

        peer.answerGate?.complete(Unit)
        runCurrent()
        assertEquals(listOf("early", "during-setup"), peer.candidates)
        val answer: SignalingEnvelope = fixture.signaling.sent.single()
        assertEquals(SignalingMessageType.SDP_ANSWER, answer.type)
        assertEquals("42", answer.sdp?.toSteamId)
        assertEquals("7", answer.sdp?.conversationId)
        assertEquals("answer", answer.sdp?.sdp)
        fixture.signaling.receive(ice("after-setup"))
        runCurrent()
        assertEquals(listOf("early", "during-setup", "after-setup"), peer.candidates)
    }

    @Test
    fun declineSendsHangupWithoutCreatingMedia() = runTest {
        val fixture: Fixture = fixture()
        val call: IncomingCall = offer(fixture)
        fixture.manager.declineIncomingCall(call.id)
        runCurrent()

        assertNull(fixture.manager.incomingCall.value)
        assertFalse(fixture.manager.isInCall.value)
        assertEquals(0, fixture.created)
        assertEquals(HangupPayload("7", "42"), fixture.signaling.sent.single().callHangup)
    }

    @Test
    fun remoteHangupClosesPendingConsentWithoutEchoingHangup() = runTest {
        val fixture: Fixture = fixture()
        offer(fixture)
        fixture.signaling.receive(hangup())
        runCurrent()

        assertNull(fixture.manager.incomingCall.value)
        assertEquals(0, fixture.created)
        assertTrue(fixture.signaling.sent.isEmpty())
    }

    @Test
    fun otherPeersAndConversationsCannotEndThePendingCall() = runTest {
        val fixture: Fixture = fixture()
        val call: IncomingCall = offer(fixture)
        fixture.signaling.receive(hangup(peer = "99"))
        fixture.signaling.receive(hangup(conversation = "8"))
        fixture.signaling.receive(ice("wrong-peer", peer = "99"))
        fixture.signaling.receive(ice("wrong-conversation", conversation = "8"))
        runCurrent()
        assertEquals(call, fixture.manager.incomingCall.value)

        fixture.manager.acceptIncomingCall(call.id)
        runCurrent()
        assertTrue(fixture.peer.candidates.isEmpty())
    }

    @Test
    fun busyDeclinesAnotherCallerAndKeepsTheFirstPrompt() = runTest {
        val fixture: Fixture = fixture()
        val call: IncomingCall = offer(fixture)
        fixture.signaling.receive(sdp(SignalingMessageType.SDP_OFFER, peer = "99"))
        runCurrent()

        assertEquals(call, fixture.manager.incomingCall.value)
        assertEquals(HangupPayload("7", "99"), fixture.signaling.sent.single().callHangup)
    }

    @Test
    fun signalingLossClearsPendingConsent() = runTest {
        val fixture: Fixture = fixture()
        offer(fixture)
        fixture.signaling.isConnected.value = false
        runCurrent()
        assertNull(fixture.manager.incomingCall.value)
        assertEquals(0, fixture.created)
    }

    @Test
    fun unansweredOfferExpiresWithoutAcquiringMedia() = runTest {
        val fixture: Fixture = fixture()
        offer(fixture)
        advanceTimeBy(45_000L.milliseconds)
        runCurrent()

        assertNull(fixture.manager.incomingCall.value)
        assertEquals(0, fixture.created)
        assertEquals(SignalingMessageType.CALL_HANGUP, fixture.signaling.sent.single().type)
    }

    @Test
    fun hangupDuringAnswerCancelsSetupAndStaleActionsCannotTouchNextCall() = runTest {
        val peer: FakePeer = FakePeer().apply { answerGate = CompletableDeferred() }
        val fixture: Fixture = fixture(peer)
        val first: IncomingCall = offer(fixture)
        fixture.manager.acceptIncomingCall(first.id)
        runCurrent()
        fixture.signaling.receive(hangup())
        runCurrent()
        peer.answerGate?.complete(Unit)
        runCurrent()

        assertTrue(peer.closed)
        assertFalse(fixture.manager.isInCall.value)
        assertTrue(fixture.signaling.sent.isEmpty())
        val second: IncomingCall = offer(fixture)
        assertNotEquals(first.id, second.id)
        fixture.manager.acceptIncomingCall(first.id)
        fixture.manager.declineIncomingCall(first.id)
        runCurrent()
        assertEquals(second, fixture.manager.incomingCall.value)
        assertEquals(1, fixture.created)
    }

    @Test
    fun failedMediaCreationReleasesTheCallAndNotifiesTheCaller() = runTest {
        val fixture = Fixture(this, failCreation = true)
        runCurrent()
        val call: IncomingCall = offer(fixture)
        fixture.manager.acceptIncomingCall(call.id)
        runCurrent()
        assertNull(fixture.manager.incomingCall.value)
        assertFalse(fixture.manager.isInCall.value)
        assertEquals(SignalingMessageType.CALL_HANGUP, fixture.signaling.sent.single().type)
    }

    @Test
    fun failedAnswerClosesMediaAndReleasesTheCall() = runTest {
        val fixture: Fixture = fixture(FakePeer().apply { failAnswer = true })
        val call: IncomingCall = offer(fixture)
        fixture.manager.acceptIncomingCall(call.id)
        runCurrent()
        assertTrue(fixture.peer.closed)
        assertFalse(fixture.manager.isInCall.value)
        assertEquals(SignalingMessageType.CALL_HANGUP, fixture.signaling.sent.single().type)
    }

    @Test
    fun outgoingCallAppliesOnlyItsPeersAnswerAndBufferedIce() = runTest {
        val fixture: Fixture = fixture()
        fixture.manager.startCall(7L, 42L)
        runCurrent()
        assertEquals(SignalingMessageType.SDP_OFFER, fixture.signaling.sent.single().type)
        fixture.signaling.receive(ice("early"))
        fixture.signaling.receive(sdp(SignalingMessageType.SDP_ANSWER, peer = "99"))
        runCurrent()
        assertFalse(fixture.peer.hasRemoteDescription)
        assertTrue(fixture.peer.candidates.isEmpty())

        fixture.signaling.receive(sdp(SignalingMessageType.SDP_ANSWER))
        runCurrent()
        assertTrue(fixture.peer.hasRemoteDescription)
        assertEquals(listOf("early"), fixture.peer.candidates)
        fixture.manager.endCall()
        runCurrent()
        assertTrue(fixture.peer.closed)
        assertFalse(fixture.manager.isInCall.value)
    }

    @Test
    fun disconnectAndTerminalMediaStateReleaseMedia() = runTest {
        for (disconnect: Boolean in listOf(false, true)) {
            val fixture: Fixture = fixture()
            val call: IncomingCall = offer(fixture)
            fixture.manager.acceptIncomingCall(call.id)
            runCurrent()
            if (disconnect) fixture.manager.disconnectSignaling()
            else fixture.peer.connectionState.value = CallConnectionState.FAILED
            runCurrent()
            assertTrue(fixture.peer.closed)
            assertFalse(fixture.manager.isInCall.value)
        }
    }

    private fun TestScope.fixture(peer: FakePeer = FakePeer()): Fixture = Fixture(this, peer).also { runCurrent() }

    private suspend fun TestScope.offer(fixture: Fixture): IncomingCall {
        fixture.signaling.receive(sdp(SignalingMessageType.SDP_OFFER))
        runCurrent()
        return assertNotNull(fixture.manager.incomingCall.value)
    }

    private class Fixture(
        testScope: TestScope,
        val peer: FakePeer = FakePeer(),
        failCreation: Boolean = false,
    ) {
        val signaling: FakeSignaling = FakeSignaling()
        var created: Int = 0
        val manager: CallManager = CallManager(
            signalingClient = signaling,
            webRtcClientFactory = {
                created += 1
                check(!failCreation) { "Microphone unavailable" }
                peer
            },
            scope = testScope.backgroundScope,
        )
    }

    private class FakeSignaling : CallSignaling {
        private val received: MutableSharedFlow<SignalingEnvelope> = MutableSharedFlow(extraBufferCapacity = 32)
        override val incoming: Flow<SignalingEnvelope> = received
        override val isConnected: MutableStateFlow<Boolean> = MutableStateFlow(true)
        val sent: MutableList<SignalingEnvelope> = mutableListOf()
        suspend fun receive(envelope: SignalingEnvelope) = received.emit(envelope)
        override suspend fun send(envelope: SignalingEnvelope) { sent.add(envelope) }
        override fun close() { isConnected.value = false }
    }

    private class FakePeer : CallPeer {
        override val connectionState: MutableStateFlow<CallConnectionState> = MutableStateFlow(CallConnectionState.NEW)
        override val localIceCandidates: Flow<LocalIceCandidateData> = MutableSharedFlow()
        val candidates: MutableList<String> = mutableListOf()
        var answerGate: CompletableDeferred<Unit>? = null
        var hasRemoteDescription: Boolean = false
        var closed: Boolean = false
        var failAnswer: Boolean = false
        override suspend fun createOffer(): String = "offer"
        override suspend fun createAnswer(remoteOfferSdp: String): String {
            answerGate?.await()
            check(!failAnswer) { "Answer failed" }
            hasRemoteDescription = true
            return "answer"
        }
        override suspend fun setRemoteAnswer(sdp: String) { hasRemoteDescription = true }
        override fun addRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String) {
            check(hasRemoteDescription)
            candidates.add(candidate)
        }
        override fun close() { closed = true }
    }

    private fun sdp(type: String, conversation: String = "7", peer: String = "42"): SignalingEnvelope =
        SignalingEnvelope(type, sdp = SdpPayload(conversation, "1", peer, "sdp"))

    private fun hangup(conversation: String = "7", peer: String = "42"): SignalingEnvelope =
        SignalingEnvelope(SignalingMessageType.CALL_HANGUP, callHangup = HangupPayload(conversation, "1", peer))

    private fun ice(candidate: String, conversation: String = "7", peer: String = "42"): SignalingEnvelope =
        SignalingEnvelope(
            SignalingMessageType.ICE_CANDIDATE,
            iceCandidate = IceCandidatePayload(
                conversation, "1", peer, Json.encodeToString(IceCandidateWireData("audio", 0, candidate)),
            ),
        )
}
