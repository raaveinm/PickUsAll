package com.raaveinm.webview

import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.WebViewEvent
import com.raaveinm.webview.wv.WebViewSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebViewStateTest {
    @Test
    fun failedNavigationStopsLoadingAndNextNavigationClearsError() {
        val events: MutableList<WebViewEvent> = mutableListOf()
        val state: AppWebViewState = AppWebViewState("about:blank", WebViewSettings(), events::add)
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
        val state: AppWebViewState = AppWebViewState("about:blank", WebViewSettings()) {}
        state.controller.loadUrl("https://example.com")
        assertEquals("https://example.com", state.url)
        var completed: Boolean = false
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
}
