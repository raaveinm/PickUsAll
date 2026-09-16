package com.raaveinm.features.impl_webrtc.signaling

import io.ktor.client.engine.HttpClientEngineFactory

expect fun signalingHttpClientEngine(): HttpClientEngineFactory<*>
