package com.raaveinm.features.impl_webrtc.signaling

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun signalingHttpClientEngine(): HttpClientEngineFactory<*> = CIO
