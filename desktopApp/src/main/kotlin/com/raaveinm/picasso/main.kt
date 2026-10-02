package com.raaveinm.picasso

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.raaveinm.core.database.DatabaseFactory
import com.raaveinm.core.database.databaseModule
import com.raaveinm.core.datastore.auth.AuthDataStoreFactory
import com.raaveinm.core.datastore.authDataStoreModule
import com.raaveinm.picasso.di.initKoin

fun main() {
    initKoin {
        modules(
            databaseModule(DatabaseFactory()),
            authDataStoreModule(AuthDataStoreFactory())
        )
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "PickUsAll",
            state = WindowState(size = DpSize(1200.dp, 900.dp))
        ) {
            App()
        }
    }
}
