package com.raaveinm.picasso.data

import io.ktor.client.engine.HttpClientEngineFactory

expect fun httpClientEngine(): HttpClientEngineFactory<*>
