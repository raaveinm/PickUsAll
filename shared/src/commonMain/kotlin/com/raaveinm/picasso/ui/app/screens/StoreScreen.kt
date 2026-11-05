package com.raaveinm.picasso.ui.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.game.Game
import com.raaveinm.core.model.responses.GameStoreInfoEntry
import com.raaveinm.core.model.responses.GetGameStoreInfoResponse
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.pickusall.core.designsystem.animated.UnspecifiedProgressIndicator
import com.raaveinm.pickusall.core.designsystem.utils.ImageUtility
import com.raaveinm.webview.AppWebView
import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.WebViewEvent
import com.raaveinm.webview.wv.WebViewSettings
import com.raaveinm.webview.wv.rememberAppHtmlWebViewState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.store_details_unavailable

//
// Created by Kirill "Raaveinm" on 10/30/2026.
//

// https://store.steampowered.com/api/appdetails?appids=1808500&cc=ua&l=english
// https://store.steampowered.com/api/appdetails?appids=$appId&cc=u&l=$language

@Composable
fun StoreScreen(
    modifier: Modifier = Modifier,
    appId: Int = 252490,
    language: String = "english",
) {
    val httpClient: HttpClient = koinInject()
    var storeItem: GameStoreInfoEntry? by remember(appId, language, httpClient) { mutableStateOf(null) }
    var isLoading: Boolean by remember(appId, language, httpClient) { mutableStateOf(true) }

    LaunchedEffect(appId, language, httpClient) {
        try {
            val response: GetGameStoreInfoResponse = httpClient.get(
                urlString = "https://store.steampowered.com/api/appdetails",
            ) {
                parameter("appids", appId)
                parameter("cc", "u")
                parameter("l", language)
            }.body<GetGameStoreInfoResponse>()

            storeItem = response[appId.toString()]
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            println("StoreScreen: app details failed for appId=$appId: $exception")
        } finally { isLoading = false }
    }

    val game: Game? = storeItem?.takeIf { it.success }?.data
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> UnspecifiedProgressIndicator(selfSize = 72.dp, widthAngular = 9f)
            game != null -> PrivateStoreScreen(game = game)
            else -> Text(text = stringResource(Res.string.store_details_unavailable))
        }
    }
    Box(Modifier.fillMaxSize().zIndex(-1f)) {
        ImageUtility(
            imageUrl = game?.backgroundRaw ?: "",
            modifier = Modifier.fillMaxSize(),
            enableGlowEffect = false,
            errorColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Preview
@Composable
private fun PrivateStoreScreen(
    modifier: Modifier = Modifier,
    game: Game = Mock.game,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = game.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        val description: String? = game.aboutGame?.takeIf { it.isNotBlank() }
        if (description != null) {
            WebViewRender(
                modifier = Modifier.weight(1f),
                html = description,
            )
        } else {
            Text(
                text = game.shortDescription.orEmpty(),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

///////////////////////////////////////////////
// Store description HTML
///////////////////////////////////////////////

private const val STORE_DESCRIPTION_BASE_URL: String = "https://picasso.invalid/store-description/"

@Composable
private fun WebViewRender(
    modifier: Modifier = Modifier,
    html: String,
) {
    val foreground: String = MaterialTheme.colorScheme.onBackground.toCssColor()
    val linkColor: String = MaterialTheme.colorScheme.primary.toCssColor()
    val fontSize: Float = MaterialTheme.typography.bodyLarge.fontSize.value * LocalDensity.current.fontScale
    val document: String = remember(html, foreground, linkColor, fontSize) {
        """
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <meta name="referrer" content="no-referrer">
                <meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src https: data:; media-src https:; style-src 'unsafe-inline'; base-uri 'none'; form-action 'none'">
                <style>
                    * { box-sizing: border-box; }
                    html, body { margin: 0; background: transparent; color: $foreground; }
                    body {
                        padding: 0 16px 16px;
                        font-family: system-ui, sans-serif;
                        font-size: ${fontSize}px;
                        line-height: 1.6;
                        overflow-wrap: anywhere;
                    }
                    h1, h2, h3, h4 { line-height: 1.3; margin: 24px 0 12px; }
                    p { margin: 0 0 16px; }
                    a { color: $linkColor; }
                    img, video { display: block; max-width: 100% !important; height: auto !important; }
                    .bb_img_ctn { display: block; margin: 16px 0; }
                    pre { white-space: pre-wrap; }
                    table { display: block; max-width: 100%; overflow-x: auto; }
                </style>
            </head>
            <body>$html</body>
            </html>
        """.trimIndent()
    }
    val uriHandler: UriHandler = LocalUriHandler.current
    val state: AppWebViewState = rememberAppHtmlWebViewState(
        html = document,
        baseUrl = STORE_DESCRIPTION_BASE_URL,
        settings = WebViewSettings(
            transparentBackground = true,
            javaScriptEnabled = false,
            allowFileAccess = false,
            allowsBackForwardNavigationGestures = false,
            allowNavigation = false,
            mediaPlaybackRequiresUserGesture = false,
        ),
        onEvent = { event ->
            if (event is WebViewEvent.NavigationRequested && event.isUserInitiated &&
                event.url.substringBefore('#') != STORE_DESCRIPTION_BASE_URL &&
                (event.url.startsWith("https://", ignoreCase = true) ||
                    event.url.startsWith("http://", ignoreCase = true))
            ) {
                try {
                    uriHandler.openUri(event.url)
                } catch (exception: IllegalArgumentException) {
                    println("StoreScreen: could not open description link: $exception")
                }
            }
        },
    )
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.error == null) {
            AppWebView(state = state, modifier = Modifier.sizeIn(maxWidth = 728.dp).fillMaxSize().background(
                MaterialTheme.colorScheme.surface.copy(alpha = .64f)))
        } else {
            Text(text = stringResource(Res.string.store_details_unavailable))
        }
    }
}

private fun Color.toCssColor(): String =
    "#${(toArgb() and 0x00FFFFFF).toString(radix = 16).padStart(length = 6, padChar = '0')}"
