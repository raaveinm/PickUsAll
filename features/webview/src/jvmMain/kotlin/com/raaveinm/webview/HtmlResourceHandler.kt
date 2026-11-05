package com.raaveinm.webview

import org.cef.callback.CefCallback
import org.cef.callback.CefResourceReadCallback
import org.cef.handler.CefResourceHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse

internal class HtmlResourceHandler(html: String) : CefResourceHandlerAdapter() {
    private val bytes: ByteArray = html.toByteArray(Charsets.UTF_8)
    private var offset: Int = 0

    override fun open(request: CefRequest?, handleRequest: BoolRef, callback: CefCallback?): Boolean {
        handleRequest.set(true)
        return true
    }

    override fun getResponseHeaders(response: CefResponse, responseLength: IntRef, redirectUrl: StringRef) {
        response.status = 200
        response.statusText = "OK"
        response.mimeType = "text/html"
        response.setHeaderMap(mapOf("Content-Type" to "text/html; charset=utf-8", "Cache-Control" to "no-store"))
        responseLength.set(bytes.size)
    }

    override fun read(
        dataOut: ByteArray,
        bytesToRead: Int,
        bytesRead: IntRef,
        callback: CefResourceReadCallback?,
    ): Boolean {
        val count: Int = minOf(bytesToRead, dataOut.size, bytes.size - offset)
        bytesRead.set(count)
        if (count == 0) return false
        bytes.copyInto(dataOut, startIndex = offset, endIndex = offset + count)
        offset += count
        return true
    }
}
