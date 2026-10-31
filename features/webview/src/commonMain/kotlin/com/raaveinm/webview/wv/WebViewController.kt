package com.raaveinm.webview.wv

//
// Created by Kirill "Raaveinm" on 10/31/2026.
//

class WebViewController internal constructor(
    private val state: AppWebViewState,
) {
    fun loadUrl(url: String) {
        state.url = url
        state.commands?.loadUrl(url)
    }

    fun goBack() {
        state.commands?.goBack()
    }

    fun goForward() {
        state.commands?.goForward()
    }

    fun reload() {
        state.commands?.reload()
    }

    fun stopLoading() {
        state.commands?.stopLoading()
    }

    fun evaluateJavaScript(
        script: String,
        callback: ((String?) -> Unit)? = null,
    ) {
        val commands: WebViewCommands? = state.commands
        if (commands == null) {
            callback?.invoke(null)
        } else {
            commands.evaluateJavaScript(script, callback)
        }
    }
}

internal interface WebViewCommands {
    fun loadUrl(url: String)
    fun goBack()
    fun goForward()
    fun reload()
    fun stopLoading()

    fun evaluateJavaScript(
        script: String,
        callback: ((String?) -> Unit)?,
    )
}
