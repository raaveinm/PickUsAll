package com.raaveinm.features.impl_webrtc.signaling

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** RTC's view of the shared socket, also allowing call consent to be tested without a server. */
interface CallSignaling {
    val incoming: Flow<SignalingEnvelope>
    val isConnected: StateFlow<Boolean>

    suspend fun send(envelope: SignalingEnvelope)
    fun close()
}
