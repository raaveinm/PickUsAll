package com.raaveinm.core.model

//
// Created by Kirill "Raaveinm" on 9/15/26.
//

private val IP_OR_LOCALHOST_REGEX = Regex(
    "^(\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?$" + // IPv4, optional :port
        "|^\\[?[0-9a-fA-F:]+]?(:\\d+)?$" + // IPv6, optional :port
        "|^localhost(:\\d+)?$" +
        "|^:\\d+$" // bare ":80"-style (Caddy's own no-TLS default, see picassobackend's Caddyfile)
)

/**
 * `Servers.url`/`ServerState.url` is stored bare (no scheme, no path) - see
 * SettingsViewModel. Building a real URL from it needs to guess http(s)/ws(s):
 * a real hostname is assumed to be served through picassobackend's own Caddy, which only ever gets a TLS
 * cert for an actual domain - a raw IP/localhost never does, so it's always
 * plain HTTP/WS there instead.
 */
fun String.looksLikeIpOrLocalhost(): Boolean = IP_OR_LOCALHOST_REGEX.matches(this)

fun String.toHttpBaseUrl(): String = "${if (looksLikeIpOrLocalhost()) "http" else "https"}://$this"

fun String.toWsUrl(path: String = "/ws"): String =
    "${if (looksLikeIpOrLocalhost()) "ws" else "wss"}://$this$path"
