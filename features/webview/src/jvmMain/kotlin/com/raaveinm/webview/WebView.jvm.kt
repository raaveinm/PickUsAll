package com.raaveinm.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.WebViewCommands
import com.raaveinm.webview.wv.WebViewEvent
import com.raaveinm.webview.wv.WebViewSettings
import com.raaveinm.webview.wv.WebViewHtmlContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.friwi.jcefmaven.CefAppBuilder
import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.ComposeCefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRequestContext
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLifeSpanHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import java.awt.BorderLayout
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.UUID
import javax.swing.JPanel
import javax.swing.SwingUtilities

@Composable
actual fun PlatformWebView(
    state: AppWebViewState,
    modifier: Modifier,
) {
    val host: DesktopBrowserHost = remember(state) { DesktopBrowserHost(state) }
    DisposableEffect(host) {
        onDispose { host.close() }
    }
    LaunchedEffect(host) {
        state.dispatch(WebViewEvent.LoadingChanged(true))
        try {
            val app: CefApp = withContext(Dispatchers.IO) { DesktopBrowserRuntime.app(state.settings) }
            host.scheduleOpen(app)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            state.dispatch(WebViewEvent.Error(exception.message ?: exception.toString()))
        } catch (error: LinkageError) {
            state.dispatch(WebViewEvent.Error(error.message ?: error.toString()))
        }
    }
    if (state.settings.transparentBackground) {
        TransparentWebView(
            modifier = modifier,
            frame = host.frame,
            onResize = host::resize,
            onPointer = host::pointerEvent,
            onKey = host::keyEvent,
            onFocus = { focused -> host.setBrowserFocus(focused) },
        )
    } else {
        SwingPanel(modifier = modifier, factory = { host })
    }
}

/** CEF is process-wide and cannot be initialized again after disposal. Each view owns its client. */
private object DesktopBrowserRuntime {
    private var cefApp: CefApp? = null
    private var runtimeOptions: Pair<Boolean, String?>? = null

    @Synchronized
    fun app(settings: WebViewSettings): CefApp {
        val options: Pair<Boolean, String?> = settings.domStorageEnabled to settings.userAgent
        cefApp?.let {
            check(runtimeOptions == options) {
                "Desktop DOM storage and user agent settings require an application restart."
            }
            return it
        }
        val builder = CefAppBuilder()
        val runtimeDirectory = File(System.getProperty("user.home"), ".picasso")
        check(runtimeDirectory.isDirectory || runtimeDirectory.mkdirs()) {
            "Could not create the Chromium runtime directory."
        }
        builder.setInstallDir(File(runtimeDirectory, "jcef"))
        builder.cefSettings.windowless_rendering_enabled = true
        builder.cefSettings.root_cache_path = Files.createTempDirectory("picasso-chromium-").toString()
        if (!settings.domStorageEnabled) builder.addJcefArgs("--disable-local-storage")
        settings.userAgent?.let { builder.addJcefArgs("--user-agent=$it") }
        // Separate app processes must not delete each other's in-progress native installation.
        val app: CefApp = FileChannel.open(
            File(runtimeDirectory, "jcef-install.lock").toPath(),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
        ).use { channel ->
            channel.lock().use { builder.build() }
        }
        cefApp = app
        runtimeOptions = options

        return app
    }
}

private class DesktopBrowserHost(
    private val state: AppWebViewState,
) : JPanel(BorderLayout()), WebViewCommands {
    private var client: CefClient? = null
    private var browser: CefBrowser? = null
    private var router: CefMessageRouter? = null
    private var requestContext: CefRequestContext? = null
    private var closed: Boolean = false
    private var offscreenCreationPending: Boolean = false
    private val callbacks: MutableMap<String, (String?) -> Unit> = mutableMapOf()
    var frame: ImageBitmap? by mutableStateOf(null)
        private set
    private var viewportSize: IntSize = IntSize(1, 1)
    private var viewportDensity: Float = 1f

    fun resize(size: IntSize, density: Float) {
        viewportSize = size
        viewportDensity = density
        (browser as? ComposeCefBrowser)?.resize(size.width, size.height, density.toDouble(), 0, 0)
    }

    fun pointerEvent(event: java.awt.event.MouseEvent, x: Int, y: Int) {
        (browser as? ComposeCefBrowser)?.pointerEvent(event, x, y)
    }

    fun keyEvent(event: java.awt.event.KeyEvent) {
        (browser as? ComposeCefBrowser)?.keyEvent(event)
    }

    fun setBrowserFocus(focused: Boolean) {
        browser?.setFocus(focused)
    }

    // CEF callbacks run on its own threads; all Compose state and callbacks belong on the EDT.
    private fun onUi(action: () -> Unit) {
        SwingUtilities.invokeLater { if (!closed) action() }
    }

    fun scheduleOpen(app: CefApp) {
        onUi {
            try {
                open(app)
            } catch (exception: Exception) {
                state.dispatch(WebViewEvent.Error(exception.message ?: exception.toString()))
            } catch (error: LinkageError) {
                state.dispatch(WebViewEvent.Error(error.message ?: error.toString()))
            }
        }
    }

    private fun open(app: CefApp) {
        val cefClient: CefClient = app.createClient()
        client = cefClient
        val context: CefRequestContext = CefRequestContext.createContext(null)
        requestContext = context
        cefClient.addLifeSpanHandler(object : CefLifeSpanHandlerAdapter() {
            override fun onAfterCreated(browser: CefBrowser) {
                val preferenceError: String? = context.setPreference(
                    "profile.default_content_setting_values.javascript",
                    if (state.settings.javaScriptEnabled) 1 else 2,
                )
                SwingUtilities.invokeLater {
                    offscreenCreationPending = false
                    if (closed) {
                        releaseBrowser()
                    } else {
                        resize(viewportSize, viewportDensity)
                        if (preferenceError != null) state.dispatch(WebViewEvent.Error(preferenceError))
                    }
                }
            }
            override fun onBeforePopup(
                browser: CefBrowser,
                frame: CefFrame,
                targetUrl: String,
                targetFrameName: String,
            ): Boolean {
                onUi {
                    state.dispatch(WebViewEvent.NavigationRequested(targetUrl, isUserInitiated = true))
                    if (state.allowsNavigation(targetUrl)) loadUrl(targetUrl)
                }
                return true
            }
        })
        cefClient.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onAddressChange(browser: CefBrowser, frame: CefFrame, url: String) {
                if (frame.isMain) onUi { state.url = url }
            }
            override fun onTitleChange(browser: CefBrowser, title: String) {
                onUi { state.dispatch(WebViewEvent.TitleChanged(title)) }
            }
        })
        cefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadingStateChange(
                browser: CefBrowser,
                isLoading: Boolean,
                canGoBack: Boolean,
                canGoForward: Boolean,
            ) {
                onUi {
                    state.canGoBack = canGoBack
                    state.canGoForward = canGoForward
                    state.dispatch(WebViewEvent.LoadingChanged(isLoading))
                }
            }
            override fun onLoadStart(browser: CefBrowser, frame: CefFrame, transitionType: CefRequest.TransitionType) {
                if (frame.isMain) {
                    val url: String = frame.url
                    onUi {
                        clearCallbacks()
                        state.dispatch(WebViewEvent.PageStarted(url))
                    }
                }
            }
            override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                if (frame.isMain) {
                    val url: String = frame.url
                    onUi {
                        if (httpStatusCode >= 400) {
                            state.dispatch(WebViewEvent.Error("HTTP $httpStatusCode", httpStatusCode))
                        }
                        state.dispatch(WebViewEvent.PageFinished(url))
                    }
                }
            }
            override fun onLoadError(
                browser: CefBrowser,
                frame: CefFrame,
                errorCode: CefLoadHandler.ErrorCode,
                errorText: String,
                failedUrl: String,
            ) {
                // ERR_ABORTED is normal for redirects and stopLoading().
                if (frame.isMain && errorCode != CefLoadHandler.ErrorCode.ERR_ABORTED) {
                    onUi { state.dispatch(WebViewEvent.Error(errorText, errorCode.code)) }
                }
            }
        })
        cefClient.addRequestHandler(object : CefRequestHandlerAdapter() {
            override fun onBeforeBrowse(
                browser: CefBrowser,
                frame: CefFrame,
                request: CefRequest,
                userGesture: Boolean,
                isRedirect: Boolean,
            ): Boolean {
                if (!frame.isMain) return false
                val url: String = request.url
                onUi { state.dispatch(WebViewEvent.NavigationRequested(url, userGesture)) }
                return !state.allowsNavigation(url) || !isAllowedUrl(url, state.settings.allowFileAccess)
            }

            override fun getResourceRequestHandler(
                browser: CefBrowser,
                frame: CefFrame,
                request: CefRequest,
                isNavigation: Boolean,
                isDownload: Boolean,
                requestInitiator: String,
                disableDefaultHandling: BoolRef,
            ): CefResourceRequestHandler? {
                val content: WebViewHtmlContent = state.htmlContent ?: return null
                if (request.url.substringBefore('#') != content.baseUrl.substringBefore('#')) return null
                return object : CefResourceRequestHandlerAdapter() {
                    override fun getResourceHandler(
                        browser: CefBrowser,
                        frame: CefFrame,
                        request: CefRequest,
                    ): CefResourceHandler = HtmlResourceHandler(content.html)
                }
            }
        })
        val messageRouter: CefMessageRouter = CefMessageRouter.create(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(
                browser: CefBrowser,
                frame: CefFrame,
                queryId: Long,
                request: String,
                persistent: Boolean,
                callback: CefQueryCallback,
            ): Boolean {
                if (!frame.isMain) return false
                if (request.startsWith("picasso-message:")) {
                    val message: String = request.removePrefix("picasso-message:")
                    onUi { state.dispatch(WebViewEvent.MessageReceived(message)) }
                    callback.success("")
                    return true
                }
                val id: String = request.substringBefore(':')
                val result: String = request.substringAfter(':', "null")
                onUi { callbacks.remove(id)?.invoke(result) }
                callback.success("")
                return true
            }
        })
        router = messageRouter
        cefClient.addMessageRouter(messageRouter)
        val cefBrowser: CefBrowser = if (state.settings.transparentBackground) {
            ComposeCefBrowser(cefClient, state.url, context) { image ->
                val bitmap: ImageBitmap = image.toComposeImageBitmap()
                onUi { frame = bitmap }
            }
        } else {
            cefClient.createBrowser(state.url, false, false, context)
        }
        browser = cefBrowser
        if (state.settings.transparentBackground) {
            offscreenCreationPending = true
            try {
                cefBrowser.createImmediately()
            } catch (failure: Throwable) {
                offscreenCreationPending = false
                throw failure
            }
        } else {
            add(cefBrowser.uiComponent, BorderLayout.CENTER)
        }
        state.commands = this
        revalidate()
        repaint()
    }

    override fun loadUrl(url: String) {
        if (isAllowedUrl(url, state.settings.allowFileAccess)) browser?.loadURL(url)
    }
    override fun goBack() { browser?.goBack() }
    override fun goForward() { browser?.goForward() }
    override fun reload() { browser?.reload() }
    override fun stopLoading() {
        browser?.stopLoad()
        state.dispatch(WebViewEvent.LoadingChanged(false))
    }
    override fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)?) {
        val cefBrowser: CefBrowser = browser ?: run {
            callback?.invoke(null)
            return
        }
        if (callback == null) {
            cefBrowser.executeJavaScript(script, cefBrowser.url, 0)
            return
        }
        val id: String = UUID.randomUUID().toString()
        callbacks[id] = callback
        // Keep arbitrary scripts out of the wrapper's syntax using a quoted JS string.
        val quotedScript: String = quoteJavaScript(script)
        cefBrowser.executeJavaScript(
            """(function(){let value=null;try{value=JSON.stringify((0,eval)($quotedScript))??'null';}catch(e){}window.cefQuery({request:'$id:'+value});})();""",
            cefBrowser.url,
            0,
        )
    }

    private fun clearCallbacks() {
        val pending: List<(String?) -> Unit> = callbacks.values.toList()
        callbacks.clear()
        pending.forEach { it(null) }
    }

    fun close() {
        if (closed) return
        closed = true
        if (state.commands === this) state.commands = null
        clearCallbacks()
        frame = null
        // Native creation completes asynchronously. Disposing the client before it registers
        // the browser can leave CEF without a view rectangle during initialization.
        SwingUtilities.invokeLater {
            if (!offscreenCreationPending) releaseBrowser()
        }
    }

    private fun releaseBrowser() {
        val cefBrowser: CefBrowser? = browser
        val cefRouter: CefMessageRouter? = router
        val cefClient: CefClient? = client
        val cefContext: CefRequestContext? = requestContext
        browser = null
        client = null
        router = null
        requestContext = null

        cefBrowser?.close(true)
        if (cefRouter != null) {
            cefClient?.removeMessageRouter(cefRouter)
            cefRouter.dispose()
        }
        cefClient?.dispose()
        cefContext?.dispose()
        removeAll()
    }
}

private fun isAllowedUrl(url: String, allowFileAccess: Boolean): Boolean =
    url.startsWith("https://", ignoreCase = true) ||
        url.startsWith("http://", ignoreCase = true) || url == "about:blank" ||
        (allowFileAccess && url.startsWith("file://", ignoreCase = true))

