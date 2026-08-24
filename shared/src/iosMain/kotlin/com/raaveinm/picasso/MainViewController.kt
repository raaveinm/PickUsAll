package com.raaveinm.picasso

import androidx.compose.ui.window.ComposeUIViewController
import com.raaveinm.picasso.di.initKoin

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { App() }
}
