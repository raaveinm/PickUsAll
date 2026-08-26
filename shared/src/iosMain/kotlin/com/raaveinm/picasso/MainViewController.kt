package com.raaveinm.picasso

import androidx.compose.ui.window.ComposeUIViewController
import com.raaveinm.core.database.DatabaseFactory
import com.raaveinm.core.database.databaseModule
import com.raaveinm.picasso.di.initKoin

fun MainViewController() = run {
    initKoin {
        modules(databaseModule(DatabaseFactory()))
    }
    ComposeUIViewController { App() }
}
