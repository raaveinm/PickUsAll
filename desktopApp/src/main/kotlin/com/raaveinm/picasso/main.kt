package com.raaveinm.picasso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.v2.Window
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.raaveinm.core.database.DatabaseFactory
import com.raaveinm.core.database.databaseModule
import com.raaveinm.core.datastore.auth.AuthDataStoreFactory
import com.raaveinm.core.datastore.authDataStoreModule
import com.raaveinm.core.datastore.settings.BehaviourSettingsList
import com.raaveinm.core.datastore.settings.BehaviourSettingsStore
import com.raaveinm.core.model.preferences.OnCloseAction
import com.raaveinm.picasso.di.initKoin
import com.raaveinm.picasso.fragments.AppMenuBar
import com.raaveinm.picasso.ui.app.QuickDestination
import com.raaveinm.picasso.ui.app.WindowActions
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.navigation.SideWindow
import com.raaveinm.picasso.ui.navigation.SideWindowRequest
import dev.nucleusframework.composenativetray.tray.api.Tray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext
import pickusall.desktopapp.generated.resources.Res
import pickusall.desktopapp.generated.resources.app_name
import pickusall.desktopapp.generated.resources.minimize_application
import pickusall.desktopapp.generated.resources.minimize_to_tray
import pickusall.desktopapp.generated.resources.phrases
import pickusall.desktopapp.generated.resources.quit_application
import pickusall.desktopapp.generated.resources.restore_application
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin {
        modules(
            databaseModule(DatabaseFactory()),
            authDataStoreModule(AuthDataStoreFactory())
        )
    }

    val behaviourSettingsStore: BehaviourSettingsStore = GlobalContext.get().get()

    application {
        val windowName = "${stringResource(Res.string.app_name)} " +
                ":: ${stringArrayResource(Res.array.phrases).random()}"

        val appViewModelOwner: ViewModelStoreOwner = remember {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
        }
        DisposableEffect(appViewModelOwner) {
            onDispose { appViewModelOwner.viewModelStore.clear() }
        }
        val appViewModel: AppViewModel = koinViewModel(viewModelStoreOwner = appViewModelOwner)
        val appUiState by appViewModel.uiState.collectAsState()
        val sideWindowRequest: SideWindowRequest? = appUiState.sideWindowRequest
        val behaviourSettings: BehaviourSettingsList by behaviourSettingsStore.settings.collectAsState(
            initial = BehaviourSettingsList(),
        )
        val windowState = rememberWindowState(
            initialBoundsProvider = WindowBoundsProvider(
                positionProvider = WindowPositionProvider.Default,
                sizeProvider = WindowSizeProvider.Fixed(DpSize(1200.dp, 900.dp))
            )
        )
        var isVisible by remember { mutableStateOf(true) }
        var isOnTop by remember { mutableStateOf(false) }
        val onToggleVisibility: () -> Unit = {
            isVisible = !isVisible

            CoroutineScope(Dispatchers.Default).launch {
                if (isVisible) {
                    isOnTop = true
                    delay(50.milliseconds)
                    isOnTop = false
                }
            }
        }
        val onQuit: () -> Unit = ::exitApplication
        val onMinimize: () -> Unit = { windowState.requestMinimized(true) }
        val onCloseRequest: () -> Unit = {
            when (behaviourSettings.onCloseRequest) {
                OnCloseAction.TO_TRAY -> isVisible = false
                OnCloseAction.MINIMIZE -> onMinimize()
                OnCloseAction.QUIT -> onQuit()
            }
        }

        val quickNavRequests = remember { Channel<QuickDestination>(Channel.CONFLATED) }
        val quickNavFlow = remember { quickNavRequests.receiveAsFlow() }
        val navigate: (QuickDestination) -> Unit = { quickNavRequests.trySend(it) }

        val windowActions: WindowActions = remember {
            WindowActions(onQuit = onQuit, onMinimize = onMinimize)
        }

        if (!behaviourSettings.isHiddenTrayIcon) {
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
        }

        Window(
            onCloseRequest = onCloseRequest,
            title = windowName,
            state = windowState,
            visible = isVisible,
            alwaysOnTop = isOnTop,
            minSize = DpSize(800.dp, 600.dp)
        ) {
            if (System.getProperty("os.name").lowercase().contains("mac"))
            {
                AppMenuBar(
                    onNavigate = navigate,
                    onOpenSteamWindow = {
                        appViewModel.setWindowRequest(
                            SideWindow.Browser(url = "https://google.com"),
                        )
                    },
                )
            }

            App(
                windowActions = windowActions,
                quickNavRequests = quickNavFlow,
                appViewModel = appViewModel,
            )
        }

        if (sideWindowRequest != null) {
            key(sideWindowRequest.id) {
                val isMediaViewer: Boolean = sideWindowRequest.destination is SideWindow.MediaLibraryViewer
                val secondWindowState = rememberWindowState(
                    initialPlacement = if (isMediaViewer) WindowPlacement.Fullscreen else WindowPlacement.Floating,
                    initialBoundsProvider = if (isMediaViewer) WindowBoundsProvider {
                        windowMetrics.screen.availableBounds
                    } else WindowBoundsProvider(
                        positionProvider = WindowPositionProvider.CenteredOnScreen,
                        sizeProvider = WindowSizeProvider.Unconstrained
                    )
                )

                Window(
                    onCloseRequest = { appViewModel.resetSideWindowRequest(sideWindowRequest.id) },
                    state = secondWindowState,
                    title = stringResource(Res.string.app_name),
                    decoration = WindowDecoration.Undecorated(),
                    transparent = !isMediaViewer,
                    resizable = true,
                    alwaysOnTop = true,
                    minSize = DpSize(800.dp, 600.dp)
                ) {
                    if (System.getProperty("os.name").lowercase().contains("mac"))
                    {
                        AppMenuBar(
                            onNavigate = navigate,
                            onOpenSteamWindow = {
                                appViewModel.setWindowRequest(
                                    SideWindow.Browser(url = "https://google.com"),
                                )
                            },
                        )
                    }
                    if (isMediaViewer) { AppSideWindow(appViewModel = appViewModel)
                    } else WindowDraggableArea {
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).fillMaxWidth().background(
                            MaterialTheme.colors.primary.copy(alpha = .72f))) {
                            AppSideWindow(appViewModel = appViewModel)
                        }
                    }
                }
            }
        }
    }
}


object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        val path = Path().apply {
            moveTo(size.width/2 - size.width/8, size.height/6)
            lineTo(size.width/2 - size.width/8, size.height/2)

            moveTo(size.width/2 + size.width/8, size.height/6)
            lineTo(size.width/2 + size.width/8, size.height/2)

            moveTo(size.width/2 - size.width/3, size.height/2)
            cubicTo(
                x1 = size.width/2 - size.width/4, y1 = size.height,
                x2 = size.width/2 + size.width/4, y2 = size.height,
                x3 = size.width/2 + size.width/3, y3 = size.width/2
            )
        }
        drawOval(Color(0xDEFFFFFF))
        drawPath(
            path = path,
            color = Color.Black,
            alpha = 1f,
            style = Stroke(16f)
        )
    }
}
