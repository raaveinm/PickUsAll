@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.raaveinm.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.WebViewCommands
import com.raaveinm.webview.wv.WebViewEvent
import platform.CoreGraphics.CGRectZero
import kotlinx.cinterop.readValue
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@Composable
actual fun PlatformWebView(
    state: AppWebViewState,
    modifier: Modifier,
) {
    // WKWebView holds its navigation delegate weakly, so Compose retains it for the view's lifetime.
    val delegate: BrowserNavigationDelegate = remember(state) { BrowserNavigationDelegate(state) }
    UIKitView(
        modifier = modifier,
        factory = {
            val configuration: WKWebViewConfiguration = WKWebViewConfiguration().apply {
                defaultWebpagePreferences.allowsContentJavaScript = state.settings.javaScriptEnabled
            }
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = configuration,
            ).apply {
                navigationDelegate = delegate
                allowsBackForwardNavigationGestures = state.settings.allowsBackForwardNavigationGestures
                scrollView.pinchGestureRecognizer?.enabled = state.settings.supportZoom
                customUserAgent = state.settings.userAgent
                val webView: WKWebView = this
                val commands: WebViewCommands = object : WebViewCommands {
                    override fun loadUrl(url: String) {
                        val target: NSURL = NSURL.URLWithString(url) ?: return
                        if (target.scheme == "file" && state.settings.allowFileAccess) {
                            webView.loadFileURL(target, allowingReadAccessToURL = target.URLByDeletingLastPathComponent ?: target)
                        } else {
                            webView.loadRequest(NSURLRequest.requestWithURL(target))
                        }
                    }
                    override fun goBack() { webView.goBack() }
                    override fun goForward() { webView.goForward() }
                    override fun reload() { webView.reload() }
                    override fun stopLoading() {
                        webView.stopLoading()
                        state.dispatch(WebViewEvent.LoadingChanged(false))
                    }
                    override fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)?) {
                        webView.evaluateJavaScript(
                            "JSON.stringify((0,eval)(${quoteJavaScript(script)}))",
                        ) { result, error ->
                            callback?.invoke(if (error == null) result as? String ?: "null" else null)
                        }
                    }
                }
                state.commands = commands
                commands.loadUrl(state.url)
            }
        },
        onRelease = { webView ->
            state.commands = null
            webView.stopLoading()
            webView.navigationDelegate = null
        },
    )
}

private class BrowserNavigationDelegate(
    private val state: AppWebViewState,
) : NSObject(), WKNavigationDelegateProtocol {
    private fun update(webView: WKWebView) {
        state.url = webView.URL?.absoluteString.orEmpty()
        state.canGoBack = webView.canGoBack
        state.canGoForward = webView.canGoForward
        state.dispatch(WebViewEvent.TitleChanged(webView.title.orEmpty()))
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        state.dispatch(WebViewEvent.PageStarted(webView.URL?.absoluteString ?: state.url))
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        update(webView)
        state.dispatch(WebViewEvent.PageFinished(state.url))
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailNavigation: WKNavigation?, withError: NSError) {
        update(webView)
        state.dispatch(WebViewEvent.Error(withError.localizedDescription, withError.code.toInt()))
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailProvisionalNavigation: WKNavigation?, withError: NSError) {
        state.dispatch(WebViewEvent.Error(withError.localizedDescription, withError.code.toInt()))
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val url: NSURL? = decidePolicyForNavigationAction.request.URL
        if (decidePolicyForNavigationAction.targetFrame?.mainFrame != false) {
            state.dispatch(WebViewEvent.NavigationRequested(url?.absoluteString.orEmpty()))
        }
        val allowed: Boolean = url?.scheme in listOf("http", "https", "about") ||
            (url?.scheme == "file" && state.settings.allowFileAccess)
        decisionHandler(
            if (allowed) WKNavigationActionPolicy.WKNavigationActionPolicyAllow
            else WKNavigationActionPolicy.WKNavigationActionPolicyCancel,
        )
    }
}
