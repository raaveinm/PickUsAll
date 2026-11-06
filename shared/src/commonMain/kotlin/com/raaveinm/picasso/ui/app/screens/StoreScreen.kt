package com.raaveinm.picasso.ui.app.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.game.Game
import com.raaveinm.core.model.responses.GameStoreInfoEntry
import com.raaveinm.core.model.responses.GetGameStoreInfoResponse
import com.raaveinm.pickusall.core.designsystem.animated.UnspecifiedProgressIndicator
import com.raaveinm.pickusall.core.designsystem.components.ScrollBar
import com.raaveinm.pickusall.core.designsystem.icons.ExtraIcons
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.store_details_unavailable
import kotlin.uuid.Uuid

//
// Created by Kirill "Raaveinm" on 10/30/2026.
//

// https://store.steampowered.com/api/appdetails?appids=1808500&cc=ua&l=english
// https://store.steampowered.com/api/appdetails?appids=$appId&cc=u&l=$language

@Composable
fun StoreScreen(
    modifier: Modifier = Modifier,
    appId: Int = 1601580,
    countryCode: String = "u",
    language: String = "english",
    devPageOpen: (String) -> Unit = { _ -> },
    navigateBack: () -> Unit = {}
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
                parameter("cc", countryCode)
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
            isLoading -> UnspecifiedProgressIndicator(selfSize = 64.dp, widthAngular = 9f)
            game != null -> PrivateStoreScreen(game = game, devPageOpen = { devPageOpen(it) }, navigateBack = navigateBack)
            else -> {
                Text(text = stringResource(Res.string.store_details_unavailable))
                navigateBack()
            }
        }
    }
    Box(Modifier.fillMaxSize().zIndex(-1f)) {
        ImageUtility(
            imageUrl = game?.backgroundRaw ?: "",
            modifier = Modifier.fillMaxSize(),
            enableGlowEffect = false,
            errorColor = MaterialTheme.colorScheme.surface,
            blurPadding = 0.dp
        )
    }
}

@Composable
private fun PrivateStoreScreen(
    modifier: Modifier = Modifier,
    game: Game,
    devPageOpen: (String) -> Unit = { _ -> },
    navigateBack: () -> Unit
) {
    val listState: LazyListState = rememberLazyListState()
    val scrollScope: CoroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Row(
            Modifier
                .padding(24.dp).align(Alignment.TopStart).zIndex(1.5f)
                .clip(Shapes.roundedSmall)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { navigateBack() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.small)
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(Shapes.circleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = game.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = Dimensions.small)
            )
        }
        ScrollBar(state = listState)
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.large),
            modifier = Modifier
                .sizeIn(maxWidth = 728.dp)
                .align(Alignment.Center)
                .clip(Shapes.roundedAverage)
                .background(color = MaterialTheme.colorScheme.surface.copy(alpha = .64f)),
            contentPadding = PaddingValues(vertical = Dimensions.paddingAboveAverage)
        ) {
            item {
                Box(Modifier.padding(horizontal = Dimensions.medium)){
                    ImageUtility(
                        imageUrl = game.headerImage ?: "",
                        modifier = Modifier.clip(Shapes.roundedSmoother)
                    )
                }
            }

            item {
                val description: String? = game.aboutGame?.takeIf { it.isNotBlank() }
                if (description != null) {
                    Box(Modifier.height(maxHeight)) {
                        WebViewRender(
                            html = description,
                            onScrollUnconsumed = { delta ->
                                scrollScope.launch { listState.scrollBy(delta) }
                            },
                        )
                    }
                } else {
                    Text(
                        text = game.shortDescription.orEmpty(),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            if (!game.website.isNullOrBlank()) {
                item {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.medium)
                    ) {
                        Text(
                            text = "Developer website: ",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        val source = remember { MutableInteractionSource() }
                        val isHovered by source.collectIsHoveredAsState()
                        val color by animateColorAsState(
                            targetValue = if (isHovered) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                            animationSpec = tween(durationMillis = 400)
                        )

                        Text(
                            text = game.website!!,
                            style = MaterialTheme.typography.titleLarge,
                            color = color,
                            modifier = Modifier.clickable(
                                interactionSource = source,
                                indication = null
                            ) { devPageOpen(game.website ?: "https://store.steampowered.com") }
                        )
                    }
                }
            }
            item {
                Row (modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.medium)) {
                    Text(
                        text = "Available On: ",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        Modifier.weight(1f).padding(horizontal = Dimensions.sMedium),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.medium)
                    ) {
                        if (game.platforms.mac) {
                            Icon(
                                imageVector = ExtraIcons.AppleIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (game.platforms.windows) {
                            Icon(
                                imageVector = ExtraIcons.WinIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (game.platforms.linux) {
                            Icon(
                                imageVector = ExtraIcons.ArchIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimensions.medium),
                    modifier = Modifier.padding(horizontal = Dimensions.medium)
                ) {
                    Text(
                        text = "Languages",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    game.supportedLanguages?.getLanguages()?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
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
    onScrollUnconsumed: (Float) -> Unit,
) {
    val foreground: String = MaterialTheme.colorScheme.onBackground.toCssColor()
    val linkColor: String = MaterialTheme.colorScheme.primary.toCssColor()
    val fontSize: Float = MaterialTheme.typography.bodyLarge.fontSize.value * LocalDensity.current.fontScale
    val density: Float = LocalDensity.current.density
    val scriptNonce: String = remember { Uuid.random().toString() }
    val document: String = remember(html, foreground, linkColor, fontSize, scriptNonce) {
        """
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <meta name="referrer" content="no-referrer">
                <meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src https: data:;
                 media-src https:; style-src 'unsafe-inline'; script-src 'nonce-$scriptNonce';
                 base-uri 'none'; form-action 'none'">
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
            <body>
                $html
                <script nonce="$scriptNonce">$STORE_DESCRIPTION_SCROLL_SCRIPT</script>
            </body>
            </html>
        """.trimIndent()
    }
    val uriHandler: UriHandler = LocalUriHandler.current
    val state: AppWebViewState = rememberAppHtmlWebViewState(
        html = document,
        baseUrl = STORE_DESCRIPTION_BASE_URL,
        settings = WebViewSettings(
            transparentBackground = true,
            captureTouchGestures = true,
            // The CSP permits only our scroll bridge; scripts in the store HTML remain blocked.
            javaScriptEnabled = true,
            allowFileAccess = false,
            allowsBackForwardNavigationGestures = false,
            allowNavigation = false,
            mediaPlaybackRequiresUserGesture = false,
        ),
        onEvent = { event ->
            if (event is WebViewEvent.MessageReceived) {
                storeDescriptionScrollDelta(event.message)?.let { delta ->
                    onScrollUnconsumed(delta * density)
                }
            }
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
            AppWebView(
                state = state,
                modifier = Modifier
                    .sizeIn(minHeight = 1024.dp)
                    .fillMaxSize())
        } else {
            Text(text = stringResource(Res.string.store_details_unavailable))
        }
    }
}

private fun Color.toCssColor(): String =
    "#${(toArgb() and 0x00FFFFFF).toString(radix = 16).padStart(length = 6, padChar = '0')}"

//"English<strong>*</strong>, French, Italian, German, Spanish - Spain,
// Japanese, Korean, Polish, Portuguese - Brazil, Russian, Simplified Chinese,
// Traditional Chinese, Turkish<br><strong>*</strong>languages with full audio support"
private fun String.getLanguages() : String {
    val languages = replace("<strong>*</strong>", "").split("<br>")[0]
    return languages
}
