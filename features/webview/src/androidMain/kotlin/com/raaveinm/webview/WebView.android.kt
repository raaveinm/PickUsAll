package com.raaveinm.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent
import android.webkit.JavascriptInterface
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
import com.raaveinm.webview.wv.WebViewHtmlContent

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    state: AppWebViewState,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AppNativeWebView(
                context = context,
                captureTouchGestures = state.settings.captureTouchGestures,
            ).apply {
                addJavascriptInterface(
                    WebViewMessageBridge { message ->
                        post { state.dispatch(WebViewEvent.MessageReceived(message)) }
                    },
                    "PicassoWebView",
                )
                if (state.settings.transparentBackground) setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = state.settings.javaScriptEnabled
                settings.domStorageEnabled = state.settings.domStorageEnabled
                settings.allowFileAccess = state.settings.allowFileAccess
                settings.mediaPlaybackRequiresUserGesture = state.settings.mediaPlaybackRequiresUserGesture
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
                        state.dispatch(WebViewEvent.NavigationRequested(url, request.hasGesture()))
                        return !state.allowsNavigation(url) ||
                            request.url.scheme !in listOf("https", "http", "about")
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
                val content: WebViewHtmlContent? = state.htmlContent
                if (content != null) {
                    loadDataWithBaseURL(content.baseUrl, content.html, "text/html", "UTF-8", null)
                } else {
                    loadUrl(state.url)
                }
            }
        },
        onRelease = { webView ->
            state.commands = null
            webView.removeJavascriptInterface("PicassoWebView")
            webView.stopLoading()
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
            webView.destroy()
        },
    )
}

private class AppNativeWebView(
    context: Context,
    private val captureTouchGestures: Boolean,
) : WebView(context) {
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (captureTouchGestures) parent?.requestDisallowInterceptTouchEvent(true)
        val handled: Boolean = super.dispatchTouchEvent(event)
        if (captureTouchGestures) {
            val continues: Boolean = event.actionMasked != MotionEvent.ACTION_UP &&
                event.actionMasked != MotionEvent.ACTION_CANCEL
            parent?.requestDisallowInterceptTouchEvent(continues)
        }
        return handled
    }
}

/** Page messages are untrusted input; receivers decide which messages they accept. */
private class WebViewMessageBridge(
    private val onMessage: (String) -> Unit,
) {
    @JavascriptInterface
    fun postMessage(message: String) {
        onMessage(message)
    }
}
