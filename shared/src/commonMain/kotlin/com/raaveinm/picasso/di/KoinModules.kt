package com.raaveinm.picasso.di

import com.raaveinm.picasso.data.ApiClient
import com.raaveinm.picasso.data.repository.OwnedGamesRepository
import com.raaveinm.picasso.ui.canvas.viewmodel.CanvasViewModel
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

private val sharedModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { ApiClient(get()) }
    single { OwnedGamesRepository(get(), get()) }
    viewModelOf(::CanvasViewModel)
    viewModelOf(::ChatViewModel)
    viewModelOf(::SettingsViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        config?.invoke(this)
        modules(sharedModule)
    }
}