package com.raaveinm.picasso

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.application
import androidx.compose.ui.window.v2.Window
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import androidx.compose.ui.zIndex
import com.raaveinm.core.database.DatabaseFactory
import com.raaveinm.core.database.databaseModule
import com.raaveinm.core.datastore.auth.AuthDataStoreFactory
import com.raaveinm.core.datastore.authDataStoreModule
import com.raaveinm.picasso.di.initKoin
import com.raaveinm.picasso.fragments.AppMenuBar
import com.raaveinm.picasso.ui.app.QuickDestination
import com.raaveinm.picasso.ui.app.WindowActions
import dev.nucleusframework.composenativetray.tray.api.Tray
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import pickusall.desktopapp.generated.resources.Res
import pickusall.desktopapp.generated.resources.app_name
import pickusall.desktopapp.generated.resources.minimize_application
import pickusall.desktopapp.generated.resources.minimize_to_tray
import pickusall.desktopapp.generated.resources.phrases
import pickusall.desktopapp.generated.resources.quit_application
import pickusall.desktopapp.generated.resources.restore_application

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin {
        modules(
            databaseModule(DatabaseFactory()),
            authDataStoreModule(AuthDataStoreFactory())
        )
    }

    application {
        val windowState = rememberWindowState(
            initialBoundsProvider = WindowBoundsProvider(
                positionProvider = WindowPositionProvider.Default,
                sizeProvider = WindowSizeProvider.Fixed(DpSize(1200.dp, 900.dp))
            )
        )
        var isVisible by remember { mutableStateOf(true) }
        val onToggleVisibility: () -> Unit = { isVisible = !isVisible }
        val onQuit: () -> Unit = ::exitApplication
        val onMinimize: () -> Unit = { windowState.requestMinimized(true) }

        var isSecondWindow by remember { mutableStateOf(false) }

        val quickNavRequests = remember { Channel<QuickDestination>(Channel.CONFLATED) }
        val quickNavFlow = remember { quickNavRequests.receiveAsFlow() }
        val navigate: (QuickDestination) -> Unit = { quickNavRequests.trySend(it) }

        val windowActions: WindowActions = remember {
            WindowActions(onQuit = onQuit, onMinimize = onMinimize)
        }

        Tray(
            icon = TrayIcon,
            tooltip = stringResource(Res.string.app_name),
        ) {
            Item(
                label =
                    if (isVisible) stringResource(Res.string.minimize_to_tray)
                    else stringResource(Res.string.restore_application),
                isEnabled = true,
                shortcut = null,
                onClick = onToggleVisibility
            )
            Divider()
            Item(
                label = stringResource(Res.string.quit_application),
                isEnabled = true,
                shortcut = null,
                onClick = onQuit
            )
            Item(
                label = stringResource(Res.string.minimize_application),
                isEnabled = true,
                shortcut = null,
                onClick = onMinimize
            )
        }

        Window(
            onCloseRequest = onToggleVisibility,
            title = "${stringResource(Res.string.app_name)} :: ${stringArrayResource(Res.array.phrases).random()}",
            state = windowState,
            visible = isVisible,
            minSize = DpSize(800.dp, 600.dp)
        ) {
            if (System.getProperty("os.name").lowercase().contains("mac"))
            {
                AppMenuBar(onNavigate = navigate, onOpenSteamWindow = { isSecondWindow = !isSecondWindow })
            }

            App(windowActions = windowActions, quickNavRequests = quickNavFlow)
        }

        if (isSecondWindow) {
            val secondWindowState = rememberWindowState(
                initialBoundsProvider = WindowBoundsProvider(
                    positionProvider = WindowPositionProvider.CenteredOnScreen,
                    sizeProvider = WindowSizeProvider.Unconstrained
                )
            )

            Window(
                onCloseRequest = { isSecondWindow = false },
                state = secondWindowState,
                title = stringResource(Res.string.app_name),
                decoration = WindowDecoration.Undecorated(),
                transparent = true,
                resizable = false,
                alwaysOnTop = true
            ) {
                if (System.getProperty("os.name").lowercase().contains("mac"))
                {
                    AppMenuBar(onNavigate = navigate, onOpenSteamWindow = { isSecondWindow = !isSecondWindow })
                }
                WindowDraggableArea {
                    Box(Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .align(Alignment.TopEnd)
                                .background(MaterialTheme.colors.error)
                                .zIndex(5f)
                                .clickable(true) { isSecondWindow = false },
                            contentAlignment = Alignment.Center
                        ) { Text("X", color = MaterialTheme.colors.onError) }
                        AppSideWindow()
                    }
                }
            }
        }
    }
}


object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFF0088FF))
    }
}
