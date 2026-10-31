package com.raaveinm.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.raaveinm.webview.wv.AppWebViewState

//
// Created by Kirill "Raaveinm" on 10/30/2026.
//

@Composable
fun AppWebView(
    state: AppWebViewState,
    modifier: Modifier = Modifier,
) {
    key(state) {
        PlatformWebView(
            state = state,
            modifier = modifier,
        )
    }
}

@Composable
expect fun PlatformWebView(
    state: AppWebViewState,
    modifier: Modifier = Modifier,
)
