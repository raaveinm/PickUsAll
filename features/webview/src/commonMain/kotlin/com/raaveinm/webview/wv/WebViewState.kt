package com.raaveinm.webview.wv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/** State for one mounted browser. Settings changes create a new browser. */
@Stable
class AppWebViewState internal constructor(
    initialUrl: String,
    val settings: WebViewSettings,
    val onEvent: (WebViewEvent) -> Unit,
) {
    var url: String by mutableStateOf(initialUrl)
        internal set
    var title: String by mutableStateOf("")
        internal set
    var isLoading: Boolean by mutableStateOf(false)
        internal set
    var canGoBack: Boolean by mutableStateOf(false)
        internal set
    var canGoForward: Boolean by mutableStateOf(false)
        internal set
    var error: WebViewEvent.Error? by mutableStateOf(null)
        internal set

    internal var commands: WebViewCommands? = null
    val controller: WebViewController = WebViewController(this)

    internal fun dispatch(event: WebViewEvent) {
        when (event) {
            is WebViewEvent.PageStarted -> {
                url = event.url
                error = null
                setLoading(true)
            }
            is WebViewEvent.PageFinished -> {
                url = event.url
                setLoading(false)
            }
            is WebViewEvent.TitleChanged -> title = event.title
            is WebViewEvent.LoadingChanged -> {
                setLoading(event.isLoading)
                return
            }
            is WebViewEvent.Error -> {
                error = event
                setLoading(false)
            }
            is WebViewEvent.MessageReceived, is WebViewEvent.NavigationRequested -> Unit
        }
        onEvent(event)
    }

    private fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            onEvent(WebViewEvent.LoadingChanged(loading))
        }
    }
}

/** URL argument changes navigate; redirects do not reload the input URL. */
@Composable
fun rememberAppWebViewState(
    url: String,
    settings: WebViewSettings = WebViewSettings(),
    onEvent: (WebViewEvent) -> Unit = {},
): AppWebViewState {
    val currentOnEvent: (WebViewEvent) -> Unit by rememberUpdatedState(onEvent)
    val state: AppWebViewState = remember(settings) {
        AppWebViewState(
            initialUrl = url,
            settings = settings,
            onEvent = { event -> currentOnEvent(event) },
        )
    }
    LaunchedEffect(state, url) {
        if (state.url != url) state.controller.loadUrl(url)
    }
    return state
}
