@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.raaveinm.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitView
import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.WebViewCommands
import com.raaveinm.webview.wv.WebViewEvent
import com.raaveinm.webview.wv.WebViewHtmlContent
import platform.CoreGraphics.CGRectZero
import kotlinx.cinterop.readValue
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject
import platform.UIKit.UIColor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun PlatformWebView(
    state: AppWebViewState,
    modifier: Modifier,
) {
    // WKWebView holds its navigation delegate weakly, so Compose retains it for the view's lifetime.
    val delegate: BrowserNavigationDelegate = remember(state) { BrowserNavigationDelegate(state) }
    val messageHandler: BrowserMessageHandler = remember(state) { BrowserMessageHandler(state) }
    UIKitView(
        modifier = modifier,
        // Default UIKit placement clears the Compose backdrop instead of showing it through the view.
        properties = UIKitInteropProperties(
            interactionMode = if (state.settings.captureTouchGestures) {
                UIKitInteropInteractionMode.NonCooperative
            } else {
                UIKitInteropInteractionMode.Cooperative()
            },
            placedAsOverlay = state.settings.transparentBackground,
        ),
        factory = {
            val configuration: WKWebViewConfiguration = WKWebViewConfiguration().apply {
                userContentController.addScriptMessageHandler(messageHandler, name = "picasso")
                defaultWebpagePreferences.allowsContentJavaScript = state.settings.javaScriptEnabled
                allowsInlineMediaPlayback = true
                if (!state.settings.mediaPlaybackRequiresUserGesture) {
                    mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
                }
            }
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = configuration,
            ).apply {
                if (state.settings.transparentBackground) {
                    opaque = false
                    backgroundColor = UIColor.clearColor
                    scrollView.opaque = false
                    scrollView.backgroundColor = UIColor.clearColor
                    // Keep the rubber-band scrolling area transparent as well as the HTML viewport.
                    underPageBackgroundColor = UIColor.clearColor
                }
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
                val content: WebViewHtmlContent? = state.htmlContent
                if (content != null) {
                    loadHTMLString(content.html, baseURL = NSURL.URLWithString(content.baseUrl))
                } else {
                    commands.loadUrl(state.url)
                }
            }
        },
        onRelease = { webView ->
            state.commands = null
            webView.stopLoading()
            webView.navigationDelegate = null
            webView.configuration.userContentController.removeScriptMessageHandlerForName("picasso")
        },
    )
}

private class BrowserMessageHandler(
    private val state: AppWebViewState,
) : NSObject(), WKScriptMessageHandlerProtocol {
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        if (!didReceiveScriptMessage.frameInfo.mainFrame) return
        val message: String = didReceiveScriptMessage.body as? String ?: return
        state.dispatch(WebViewEvent.MessageReceived(message))
    }
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
            state.dispatch(WebViewEvent.NavigationRequested(
                url = url?.absoluteString.orEmpty(),
                isUserInitiated = decidePolicyForNavigationAction.navigationType ==
                    WKNavigationTypeLinkActivated,
            ))
        }
        val allowed: Boolean = state.allowsNavigation(url?.absoluteString.orEmpty()) &&
            (url?.scheme in listOf("http", "https", "about") ||
                (url?.scheme == "file" && state.settings.allowFileAccess))
        decisionHandler(
            if (allowed) WKNavigationActionPolicy.WKNavigationActionPolicyAllow
            else WKNavigationActionPolicy.WKNavigationActionPolicyCancel,
        )
    }
}
