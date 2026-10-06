package com.raaveinm.picasso

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.raaveinm.core.database.DatabaseFactory
import com.raaveinm.core.database.databaseModule
import com.raaveinm.core.datastore.auth.AuthDataStoreFactory
import com.raaveinm.core.datastore.authDataStoreModule
import com.raaveinm.picasso.di.initKoin
import com.raaveinm.picasso.ui.app.WindowActions
import dev.nucleusframework.composenativetray.tray.api.Tray
import java.awt.Dimension
import org.jetbrains.compose.resources.stringResource
import pickusall.desktopapp.generated.resources.Res
import pickusall.desktopapp.generated.resources.app_name
import pickusall.desktopapp.generated.resources.go_to_library
import pickusall.desktopapp.generated.resources.minimize_application
import pickusall.desktopapp.generated.resources.quick_nav_menu
import pickusall.desktopapp.generated.resources.quit_application

fun main() {
    initKoin {
        modules(
            databaseModule(DatabaseFactory()),
            authDataStoreModule(AuthDataStoreFactory())
        )
    }

    application {
        val windowState = rememberWindowState(size = DpSize(1200.dp, 900.dp))

        val onQuit: () -> Unit = ::exitApplication
        val onMinimize: () -> Unit = { windowState.isMinimized = true }
        val onFocus: () -> Unit = {  }

        val windowActions: WindowActions = remember {
            WindowActions(onQuit = onQuit, onMinimize = onMinimize)
        }

        Tray(
            icon = TrayIcon,
            tooltip = stringResource(Res.string.app_name),
        ) {
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
            onCloseRequest = onQuit,
            title = stringResource(Res.string.app_name),
            state = windowState,
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(800, 600)
            }

            if (System.getProperty("os.name").lowercase().contains("mac"))
            {
                MenuBar {
                    Menu(stringResource(Res.string.quick_nav_menu)) {
                        Item(stringResource(Res.string.go_to_library), onClick = {})
                    }
                }
            }

            App(windowActions = windowActions)
        }
    }
}


object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFF0088FF))
    }
}
