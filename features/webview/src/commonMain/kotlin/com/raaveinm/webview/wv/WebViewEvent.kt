package com.raaveinm.webview.wv

//
// Created by Kirill "Raaveinm" on 10/31/2026.
//

sealed interface WebViewEvent {

    data class PageStarted(
        val url: String
    ) : WebViewEvent

    data class PageFinished(
        val url: String
    ) : WebViewEvent

    data class TitleChanged(
        val title: String
    ) : WebViewEvent

    data class LoadingChanged(
        val isLoading: Boolean
    ) : WebViewEvent

    data class Error(
        val description: String,
        val code: Int? = null
    ) : WebViewEvent

    data class MessageReceived(
        val message: String
    ) : WebViewEvent

    data class NavigationRequested(
        val url: String
    ) : WebViewEvent
}