package com.raaveinm.picasso

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.core.datastore.settings.VisualSettingsList
import com.raaveinm.core.datastore.settings.VisualSettingsStore
import com.raaveinm.core.model.preferences.ThemeMode
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.navigation.SideWindow
import com.raaveinm.picasso.ui.navigation.SideWindowRequest
import com.raaveinm.pickusall.core.designsystem.components.CallAccept
import com.raaveinm.picasso.ui.media.MediaLibraryViewer
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.webview.AppWebView
import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.rememberAppWebViewState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.dismiss
import pickusall.shared.generated.resources.side_window_incoming_call
import pickusall.shared.generated.resources.webview_back
import pickusall.shared.generated.resources.webview_error
import pickusall.shared.generated.resources.webview_forward
import pickusall.shared.generated.resources.webview_reload
import pickusall.shared.generated.resources.webview_retry
import pickusall.shared.generated.resources.webview_stop

//
// Created by Kirill "Raaveinm" on 11/02/2026.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

/**
 * Routes the current assistive request inside a desktop window or a mobile top screen.
 * The platform host supplies the same [appViewModel] as the main app and owns presentation only.
 */
@Composable
fun AppSideWindow(
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
) {
    val appUiState by appViewModel.uiState.collectAsState()
    val request: SideWindowRequest = appUiState.sideWindowRequest ?: return
    val visualSettings: VisualSettingsList by koinInject<VisualSettingsStore>().settings
        .collectAsState(initial = VisualSettingsList())
    val isDarkTheme: Boolean = when (visualSettings.themeMode) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    PicassoTheme(
        darkTheme = isDarkTheme,
        dynamicColor = visualSettings.isDynamicColor,
    ) {
        Surface(modifier = modifier.fillMaxSize()) {
            key(request.id) {
                val onDismiss: () -> Unit = { appViewModel.resetSideWindowRequest(request.id) }
                Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    when (val destination: SideWindow = request.destination) {
                        is SideWindow.WebAuth -> WebScreen(
                            url = destination.url,
                            onDismiss = onDismiss,
                        )

                        is SideWindow.IncomingCall -> IncomingCallScreen(
                            request = destination,
                            caller = appUiState.incomingCaller?.takeIf { it.steamId == destination.peerSteamId },
                            onDismiss = onDismiss,
                            onAccept = { appViewModel.acceptIncomingCall(request.id) },
                        )

                        is SideWindow.Browser -> WebScreen(
                            url = destination.url,
                            onDismiss = onDismiss,
                        )

                        is SideWindow.MediaLibraryViewer -> MediaLibraryViewer(
                            items = destination.mediaLinks,
                            initialSelectedId = destination.selectedId,
                            onDismiss = onDismiss,
                        )
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////
// Web pages and authentication
///////////////////////////////////////////////

@Composable
private fun WebScreen(
    url: String,
    onDismiss: () -> Unit,
) {
    var attempt: Int by remember { mutableStateOf(0) }
    key(attempt) {
        val state: AppWebViewState = rememberAppWebViewState(url)
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = state.controller::goBack, enabled = state.canGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.webview_back),
                    )
                }
                IconButton(onClick = state.controller::goForward, enabled = state.canGoForward) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(Res.string.webview_forward),
                    )
                }
                IconButton(
                    onClick = {
                        if (state.isLoading) state.controller.stopLoading()
                        else state.controller.reload()
                    },
                ) {
                    Icon(
                        imageVector = if (state.isLoading) Icons.Filled.Stop else Icons.Filled.Refresh,
                        contentDescription = stringResource(
                            if (state.isLoading) Res.string.webview_stop else Res.string.webview_reload,
                        ),
                    )
                }
                Text(
                    text = state.title.ifBlank { state.url },
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
                    maxLines = 1,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.dismiss),
                    )
                }
            }

            if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AppWebView(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                )
                if (state.error != null) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(
                                    Res.string.webview_error,
                                    state.error?.description.orEmpty(),
                                ),
                            )
                            Button(onClick = { attempt += 1 }) {
                                Text(stringResource(Res.string.webview_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////
// Incoming calls
///////////////////////////////////////////////

@Composable
private fun IncomingCallScreen(
    request: SideWindow.IncomingCall,
    caller: User?,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CallAccept(
            callerName = caller?.personaName ?: stringResource(
                Res.string.side_window_incoming_call,
                request.peerSteamId.toString(),
            ),
            callerIcon = caller?.avatarFull,
            onAccept = onAccept,
            onDismiss = onDismiss,
        )
    }
}
