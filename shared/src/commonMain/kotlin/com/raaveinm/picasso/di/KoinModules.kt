package com.raaveinm.picasso.di

import com.raaveinm.features.impl_webrtc.CallManager
import com.raaveinm.features.impl_webrtc.signaling.SignalingClient
import com.raaveinm.picasso.data.ApiClient
import com.raaveinm.picasso.data.AuthApi
import com.raaveinm.picasso.data.httpClientEngine
import com.raaveinm.picasso.data.repository.AuthRepository
import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.ContactsRepository
import com.raaveinm.picasso.data.repository.FriendsRepository
import com.raaveinm.picasso.data.repository.GameStoreRepository
import com.raaveinm.picasso.data.repository.KeyBindingRepository
import com.raaveinm.picasso.data.repository.OwnedGamesRepository
import com.raaveinm.picasso.data.repository.ProfileHydrator
import com.raaveinm.picasso.data.repository.ProfilesRepository
import com.raaveinm.picasso.data.server.ActiveServer
import com.raaveinm.picasso.data.server.ChatSocket
import com.raaveinm.picasso.data.server.ChatTransport
import com.raaveinm.picasso.data.server.ServerContextSource
import com.raaveinm.picasso.data.server.ContactsApi
import com.raaveinm.picasso.data.server.ConversationsApi
import com.raaveinm.picasso.data.sync.SyncCoordinator
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
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
        HttpClient(httpClientEngine()) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { ApiClient(get()) }
    single { AuthApi(get()) }
    single { OwnedGamesRepository(get(), get()) }
    single { GameStoreRepository(get(), get()) }
    single { ConversationsApi(get()) }
    single { ContactsApi(get()) }
    single { ActiveServer(get(), get()) }
    single<ServerContextSource> { get<ActiveServer>() }
    single { ChatSocket(get()) }
    single<ChatTransport> { get<ChatSocket>() }
    single { ProfilesRepository(get(), get(), get()) }
    single<ProfileHydrator> { get<ProfilesRepository>() }
    single { ChatRepository(get(), get(), get(), get(), get()) }
    single { ContactsRepository(get(), get(), get(), get(), get(), get()) }
    single {
        val callManager = get<CallManager>()
        SyncCoordinator(
            activeServer = get(),
            socket = get(),
            chatRepository = get(),
            contactsRepository = get(),
            profiles = get(),
            onNothingToConnect = { callManager.disconnectSignaling() }
        )
    }
    single { FriendsRepository(get(), get()) }
    single { AuthRepository(get(), get(), get()) }
    single { KeyBindingRepository(get()) }
    // one SignalingClient: calls and chat share the single WebSocket a client keeps per server
    single { SignalingClient() }
    single { CallManager(get()) }
    viewModelOf(::AppViewModel)
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