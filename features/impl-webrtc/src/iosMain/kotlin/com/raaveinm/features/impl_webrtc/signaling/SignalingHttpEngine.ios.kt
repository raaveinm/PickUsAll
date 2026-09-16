package com.raaveinm.features.impl_webrtc.signaling

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun signalingHttpClientEngine(): HttpClientEngineFactory<*> = Darwin
