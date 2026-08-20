package com.raaveinm.picasso.ui.actions

import androidx.compose.ui.platform.UriHandler

actual fun libraryRedirect(
    gameId: Int,
    uriHandler: UriHandler
) {
    uriHandler.openUri("https://store.steampowered.com/app/$gameId")
}