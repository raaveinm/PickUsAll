package com.raaveinm.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.WebViewCommands
import com.raaveinm.webview.wv.WebViewEvent

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    state: AppWebViewState,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = state.settings.javaScriptEnabled
                settings.domStorageEnabled = state.settings.domStorageEnabled
                settings.allowFileAccess = state.settings.allowFileAccess
                settings.setSupportZoom(state.settings.supportZoom)
                settings.builtInZoomControls = state.settings.supportZoom
                settings.displayZoomControls = false
                state.settings.userAgent?.let { settings.userAgentString = it }
                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView, title: String?) {
                        state.dispatch(WebViewEvent.TitleChanged(title.orEmpty()))
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        if (!request.isForMainFrame) return false
                        val url: String = request.url.toString()
                        state.dispatch(WebViewEvent.NavigationRequested(url))
                        return request.url.scheme !in listOf("https", "http", "about")
                    }

                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        state.dispatch(WebViewEvent.PageStarted(url.orEmpty()))
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        state.canGoBack = view.canGoBack()
                        state.canGoForward = view.canGoForward()
                        state.dispatch(WebViewEvent.PageFinished(url.orEmpty()))
                    }

                    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                        state.url = url.orEmpty()
                        state.canGoBack = view.canGoBack()
                        state.canGoForward = view.canGoForward()
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError,
                    ) {
                        if (request.isForMainFrame) {
                            state.dispatch(WebViewEvent.Error(error.description.toString(), error.errorCode))
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView,
                        request: WebResourceRequest,
                        errorResponse: WebResourceResponse,
                    ) {
                        if (request.isForMainFrame) {
                            state.dispatch(WebViewEvent.Error(errorResponse.reasonPhrase, errorResponse.statusCode))
                        }
                    }
                }
                val webView: WebView = this
                state.commands = object : WebViewCommands {
                    override fun loadUrl(url: String) = webView.loadUrl(url)
                    override fun goBack() = webView.goBack()
                    override fun goForward() = webView.goForward()
                    override fun reload() = webView.reload()
                    override fun stopLoading() {
                        webView.stopLoading()
                        state.dispatch(WebViewEvent.LoadingChanged(false))
                    }
                    override fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)?) {
                        webView.evaluateJavascript(script) { result -> callback?.invoke(result) }
                    }
                }
                loadUrl(state.url)
            }
        },
        onRelease = { webView ->
            state.commands = null
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            webView.destroy()
        },
    )
}
