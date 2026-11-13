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
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.game.Game
import com.raaveinm.core.model.game.ProtonDB
import com.raaveinm.core.model.responses.GameStoreInfoEntry
import com.raaveinm.core.model.responses.GetGameStoreInfoResponse
import com.raaveinm.core.model.responses.ProtonDBResponse
import com.raaveinm.pickusall.core.designsystem.animated.CircleProgressBar
import com.raaveinm.pickusall.core.designsystem.animated.UnspecifiedProgressIndicator
import com.raaveinm.pickusall.core.designsystem.components.ImageGallery
import com.raaveinm.pickusall.core.designsystem.components.ScrollBar
import com.raaveinm.pickusall.core.designsystem.icons.ExtraIcons
import com.raaveinm.pickusall.core.designsystem.obj.MediaGalleryItem
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.theme.SpecialColors
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
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.overall_rating
import pickusall.shared.generated.resources.proton_db_description
import pickusall.shared.generated.resources.proton_db_section_title
import pickusall.shared.generated.resources.stema_deck_compatibility
import pickusall.shared.generated.resources.store_available_on
import pickusall.shared.generated.resources.store_details_unavailable
import pickusall.shared.generated.resources.store_developer_website
import pickusall.shared.generated.resources.store_languages
import pickusall.shared.generated.resources.store_protondb_borked
import pickusall.shared.generated.resources.store_protondb_bronze
import pickusall.shared.generated.resources.store_protondb_gold
import pickusall.shared.generated.resources.store_protondb_pending
import pickusall.shared.generated.resources.store_protondb_platinum
import pickusall.shared.generated.resources.store_protondb_rating
import pickusall.shared.generated.resources.store_protondb_silver
import kotlin.uuid.Uuid

//
// Created by Kirill "Raaveinm" on 10/30/2026.
//

// https://store.steampowered.com/api/appdetails?appids=1808500&cc=ua&l=english
// https://store.steampowered.com/api/appdetails?appids=$appId&cc=u&l=$language

// im really sorry, but this screen is pure spaghetti 🍝

@Composable
fun StoreScreen(
    modifier: Modifier = Modifier,
    appId: Int = 1601580,
    countryCode: String = "u",
    language: String = "english",
    devPageOpen: (String) -> Unit = { _ -> },
    navigateBack: () -> Unit = {},
    onItemClicked: (Int, List<MediaGalleryItem>) -> Unit = { _,_ -> }
) {
    val httpClient: HttpClient = koinInject()
    var storeItem: GameStoreInfoEntry? by remember(appId, language, httpClient)
    { mutableStateOf(null) }
    var isLoading: Boolean by remember(appId, language, httpClient)
    { mutableStateOf(true) }
    var protonSummary: ProtonDBResponse? by remember(appId, httpClient)
    { mutableStateOf(null) }

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

    LaunchedEffect(appId, httpClient) {
        try {
            val response: HttpResponse = httpClient.get(
                urlString = "https://www.protondb.com/data/reports/newSummaries/$appId.json",
            )
            if (response.status.isSuccess()) {
                protonSummary = response.body<ProtonDBResponse>()
            }
        } catch (exception: CancellationException) { throw exception
        } catch (exception: Exception) { println("ProtonDB summary failed for appId=$appId: $exception") }
    }

    val game: Game? = storeItem?.takeIf { it.success }?.data
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> UnspecifiedProgressIndicator(selfSize = 64.dp, widthAngular = 9f)
            game != null -> key(game.steamAppId) {
                PrivateStoreScreen(
                    game = game,
                    protonSummary = protonSummary,
                    devPageOpen = devPageOpen,
                    navigateBack = navigateBack,
                    onItemClicked = onItemClicked,
                )
            }
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

///////////////////////////////////////////////
// Main Column
///////////////////////////////////////////////
@Composable
private fun PrivateStoreScreen(
    modifier: Modifier = Modifier,
    game: Game,
    protonSummary: ProtonDBResponse?,
    devPageOpen: (String) -> Unit = { _ -> },
    navigateBack: () -> Unit,
    onItemClicked: (Int, List<MediaGalleryItem>) -> Unit = { _, _ -> }
) {
    val listState: LazyListState = rememberLazyListState()
    val scrollScope: CoroutineScope = rememberCoroutineScope()
    val galleryItems: List<MediaGalleryItem> = remember(game.screenshots, game.movies) {
        game.toGalleryItems()
    }
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
                .padding(PlatformSpecificDim.paddingStoreScreen)
                .clip(Shapes.roundedAverage)
                .background(color = MaterialTheme.colorScheme.surface.copy(alpha = .64f)),
            contentPadding = PaddingValues(
                bottom = Dimensions.paddingAboveAverage,
                top = PlatformSpecificDim.storeNavPadding
            )
        ) {
            item {
                Box(Modifier.padding(horizontal = Dimensions.medium)) {
                    ImageUtility(
                        imageUrl = game.headerImage ?: "",
                        modifier = Modifier.clip(Shapes.roundedAverage)
                    )
                }
            }

            // WebView

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

            // Game Website

            if (!game.website.isNullOrBlank()) {
                item {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.medium)
                    ) {
                        Text(
                            text = stringResource(Res.string.store_developer_website),
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
                            modifier = Modifier.padding(start = Dimensions.sMedium).clickable(
                                interactionSource = source,
                                indication = null
                            ) { devPageOpen(game.website ?: "https://store.steampowered.com") }
                        )
                    }
                }
            }

            // Available Platforms

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.medium)) {
                    Text(
                        text = stringResource(Res.string.store_available_on),
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

            // languages

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimensions.medium),
                    modifier = Modifier.padding(horizontal = Dimensions.medium)
                ) {
                    Text(
                        text = stringResource(Res.string.store_languages),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    game.supportedLanguages?.getLanguages()?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            if (galleryItems.isNotEmpty()) {
                item {
                    ImageGallery(
                        items = galleryItems,
                        verticalScrollEnabled = true,
                        onItemClicked = { onItemClicked(it, galleryItems) }
                    )
                }
            }

            ///////////////////////////////////////////////
            // Proton DB
            ///////////////////////////////////////////////

            if (protonSummary != null) {
                item {
                    val source = remember { MutableInteractionSource() }
                    val isHovered by source.collectIsHoveredAsState()

                    Text(
                        text = stringResource(Res.string.proton_db_section_title) + " ↗",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isHovered) TextDecoration.Underline else TextDecoration.None,
                        modifier = Modifier.fillMaxWidth().clickable(
                            interactionSource = source,
                            indication = null
                        ) { devPageOpen("https://protondb.com") }
                            .padding(horizontal = Dimensions.medium)
                    )
                }

                item {
                    Text(
                        text = stringResource(Res.string.proton_db_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.fillMaxWidth().padding(Dimensions.medium)
                    )
                }

                item {
                    Text(
                        text = stringResource(
                            Res.string.store_protondb_rating,
                            protonSummary.tier.displayName(), protonSummary.total
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(
                            space = Dimensions.medium,
                            alignment = Alignment.CenterHorizontally
                        ),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimensions.medium)
                    ) {
                        if (protonSummary.score != null) {
                            val items = listOf<Triple<String, @Composable () -> Unit, String>>(
                                Triple(
                                    stringResource(Res.string.overall_rating),
                                    @Composable {
                                        CircleProgressBar(
                                            modifier = Modifier.size(64.dp),
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.errorContainer,
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                                MaterialTheme.colorScheme.surfaceContainer
                                            ),
                                            progress = protonSummary.score ?: .0f
                                        )
                                    },
                                    ((protonSummary.score ?: 0f) * 100).toInt().toString() + "%"
                                ),
                                Triple(
                                    stringResource(Res.string.stema_deck_compatibility),
                                    @Composable {
                                        Icon(
                                            imageVector = ExtraIcons.SteamDeckIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = protonSummary.numberTiers?.deck.asColor()
                                        )
                                    },
                                    protonSummary.numberTiers?.deck.asString()
                                ),
                                Triple(
                                    "tinkering",
                                    @Composable {
                                        Icon(
                                            imageVector = Icons.Filled.Troubleshoot,
                                            contentDescription = null,
                                            modifier = Modifier.padding(vertical = 4.dp).size(56.dp),
                                            tint = protonSummary.numberTiers?.tinker.asColor()
                                        )
                                    },
                                    protonSummary.numberTiers?.tinker.asString()
                                )
                            )

                            items.forEach { (title, content, desc) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = title,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(Dimensions.small)
                                    )
                                    content()
                                    Text(
                                        text = desc,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(Dimensions.small)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////
// Assistive Functions
///////////////////////////////////////////////

private fun ProtonDB.Tier?.asColor(): Color {
    return when (this) {
        ProtonDB.Tier.T1 -> SpecialColors.statusPlatinum
        ProtonDB.Tier.T2 -> SpecialColors.statusGold
        ProtonDB.Tier.T3 -> SpecialColors.statusSilver
        ProtonDB.Tier.T4 -> SpecialColors.statusBronze
        ProtonDB.Tier.T5 -> SpecialColors.statusBorked
        ProtonDB.Tier.NULL -> SpecialColors.unspecified
        null -> SpecialColors.unspecified
    }
}

@Composable
private fun ProtonDB.Tier?.asString(): String {
    return when (this) {
        ProtonDB.Tier.T1 -> stringResource(Res.string.store_protondb_platinum)
        ProtonDB.Tier.T2 -> stringResource(Res.string.store_protondb_gold)
        ProtonDB.Tier.T3 -> stringResource(Res.string.store_protondb_silver)
        ProtonDB.Tier.T4 -> stringResource(Res.string.store_protondb_bronze)
        ProtonDB.Tier.T5 -> stringResource(Res.string.store_protondb_borked)
        ProtonDB.Tier.NULL -> stringResource(Res.string.store_protondb_pending)
        null -> stringResource(Res.string.store_protondb_pending)
    }
}

@Composable
private fun ProtonDB.CompatibilityTier.displayName(): String = stringResource(
    when (this) {
        ProtonDB.CompatibilityTier.PLATINUM -> Res.string.store_protondb_platinum
        ProtonDB.CompatibilityTier.GOLD -> Res.string.store_protondb_gold
        ProtonDB.CompatibilityTier.SILVER -> Res.string.store_protondb_silver
        ProtonDB.CompatibilityTier.BRONZE -> Res.string.store_protondb_bronze
        ProtonDB.CompatibilityTier.BORKED -> Res.string.store_protondb_borked
        ProtonDB.CompatibilityTier.PENDING -> Res.string.store_protondb_pending
    },
)

private fun Game.toGalleryItems(): List<MediaGalleryItem> = buildList {
    for ((_, pathThumbnail, pathFull) in screenshots) {
        val url: String = pathFull.ifBlank { pathThumbnail }
        if (url.isNotBlank()) add(MediaGalleryItem.Image(url))
    }
    for ((_, name1, thumbnail, _, webm, mp4, dashAv1, dashH264, hlsH264) in movies) {
        val videoUrls: List<String> = listOfNotNull(
            mp4?.max,
            mp4?.video480,
            hlsH264,
            dashH264,
            webm?.max,
            webm?.video480,
            dashAv1,
        ).filter {
            it.startsWith("https://", ignoreCase = true) ||
                    it.startsWith("http://", ignoreCase = true)
        }.distinct()
        if (videoUrls.isNotEmpty()) {
            add(
                MediaGalleryItem.Movie(
                    title = name1,
                    thumbnailUrl = thumbnail.takeIf { it.isNotBlank() },
                    videoUrls = videoUrls,
                )
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
            AppWebView(state = state, modifier = Modifier.fillMaxSize())
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
