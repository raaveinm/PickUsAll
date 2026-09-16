package com.raaveinm.pickusall.core.designsystem.utils

import io.ktor.client.engine.HttpClientEngineFactory

expect fun httpClientEngine(): HttpClientEngineFactory<*>
