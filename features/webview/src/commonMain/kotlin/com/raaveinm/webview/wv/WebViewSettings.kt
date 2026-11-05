package com.raaveinm.webview.wv

//
// Created by Kirill "Raaveinm" on 10/31/2026.
//

/**
 * Browser options applied when the native view is created.
 *
 * @property javaScriptEnabled Allows page scripts on all platforms.
 * @property domStorageEnabled Controls Android DOM storage and desktop local storage.
 * WKWebView has no public switch for disabling DOM storage. Desktop storage and user agent
 * are process-wide: changing either after Chromium starts requires an app restart.
 * @property allowFileAccess Allows local files; disabled by default.
 * @property supportZoom Controls zoom gestures on Android/iOS; desktop uses Chromium defaults.
 * @property userAgent Optional engine user agent override; null retains the engine default.
 * @property allowsBackForwardNavigationGestures Enables iOS swipe navigation.
 * @property allowNavigation Allows the document to navigate away; blocked links still emit navigation events.
 * @property mediaPlaybackRequiresUserGesture Requires a tap before media playback on Android/iOS.
 * Desktop follows Chromium's autoplay policy.
 * @property transparentBackground Lets the Compose background show through transparent HTML.
 */
data class WebViewSettings(
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val allowFileAccess: Boolean = false,
    val supportZoom: Boolean = true,
    val userAgent: String? = null,
    val allowsBackForwardNavigationGestures: Boolean = true,
    val allowNavigation: Boolean = true,
    val mediaPlaybackRequiresUserGesture: Boolean = true,
    val transparentBackground: Boolean = false,
)
