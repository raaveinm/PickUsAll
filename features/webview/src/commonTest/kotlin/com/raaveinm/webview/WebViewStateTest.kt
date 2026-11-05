package com.raaveinm.webview

import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.WebViewEvent
import com.raaveinm.webview.wv.WebViewSettings
import com.raaveinm.webview.wv.WebViewHtmlContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebViewStateTest {
    @Test
    fun failedNavigationStopsLoadingAndNextNavigationClearsError() {
        val events: MutableList<WebViewEvent> = mutableListOf()
        val state = AppWebViewState("about:blank", WebViewSettings(), events::add)
        state.dispatch(WebViewEvent.PageStarted("https://example.com"))
        state.dispatch(WebViewEvent.LoadingChanged(true))
        state.dispatch(WebViewEvent.Error("Offline", -106))
        assertFalse(state.isLoading)
        assertEquals("Offline", state.error?.description)
        assertEquals(
            listOf(WebViewEvent.LoadingChanged(true), WebViewEvent.LoadingChanged(false)),
            events.filterIsInstance<WebViewEvent.LoadingChanged>(),
        )
        state.dispatch(WebViewEvent.PageStarted("https://example.com/retry"))
        assertTrue(state.isLoading)
        assertNull(state.error)
        state.dispatch(WebViewEvent.TitleChanged("Example"))
        state.dispatch(WebViewEvent.PageFinished("https://example.com/redirect"))
        assertFalse(state.isLoading)
        assertEquals("https://example.com/redirect", state.url)
        assertEquals("Example", state.title)
    }

    @Test
    fun urlRequestedBeforeMountIsRetainedAndDetachedEvaluationCompletes() {
        val state = AppWebViewState("about:blank", WebViewSettings(), onEvent = {})
        state.controller.loadUrl("https://example.com")
        assertEquals("https://example.com", state.url)
        var completed = false
        state.controller.evaluateJavaScript("document.title") { result ->
            assertNull(result)
            completed = true
        }
        assertTrue(completed)
    }

    @Test
    fun evaluationScriptIsQuotedWithoutChangingItsSyntax() {
        assertEquals(
            "\"a\\\"\\\\\\n\\r\\t\\u0000\\u2028\\u2029\"",
            quoteJavaScript("a\"\\\n\r\t\u0000\u2028\u2029"),
        )
    }

    @Test
    fun descriptionAllowsItsDocumentAndAnchorsButBlocksOtherPages() {
        val content = WebViewHtmlContent("<h2>Game</h2>")
        val state = AppWebViewState(
            initialUrl = content.baseUrl,
            settings = WebViewSettings(allowNavigation = false),
            onEvent = {},
            htmlContent = content,
        )
        assertTrue(state.allowsNavigation(content.baseUrl))
        assertTrue(state.allowsNavigation("${content.baseUrl}#features"))
        assertTrue(state.allowsNavigation("about:blank"))
        assertFalse(state.allowsNavigation("https://store.steampowered.com/app/620"))
        assertFalse(state.allowsNavigation("${content.baseUrl}other-page"))
        assertFalse(state.allowsNavigation("javascript:alert(1)"))
    }

    @Test
    fun normalBrowsersKeepNavigationEnabledByDefault() {
        val state = AppWebViewState("about:blank", WebViewSettings(), onEvent = {})
        assertTrue(state.allowsNavigation("https://store.steampowered.com/app/620"))
    }
}
