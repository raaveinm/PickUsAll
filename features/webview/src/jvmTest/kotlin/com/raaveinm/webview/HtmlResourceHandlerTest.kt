package com.raaveinm.webview

import org.cef.misc.BoolRef
import org.cef.misc.IntRef
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HtmlResourceHandlerTest {
    @Test
    fun streamsLargeUnicodeHtmlAcrossPartialReads() {
        val html: String = "<p>Описание игры 🎮</p>".repeat(100_000)
        val handler = HtmlResourceHandler(html)
        val handleRequest = BoolRef(false)
        assertTrue(handler.open(null, handleRequest, null))
        assertTrue(handleRequest.get())
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        val bytesRead = IntRef()
        while (handler.read(buffer, 1023, bytesRead, null)) {
            assertTrue(bytesRead.get() in 1..1023)
            output.write(buffer, 0, bytesRead.get())
        }
        assertEquals(0, bytesRead.get())
        assertEquals(html, output.toByteArray().toString(Charsets.UTF_8))
    }
}
