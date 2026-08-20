package com.raaveinm.picasso.ui.actions

import androidx.compose.ui.platform.UriHandler

actual fun libraryRedirect(
    gameId: Int,
    uriHandler: UriHandler
) = uriHandler.openUri("steam://rungameid/$gameId")