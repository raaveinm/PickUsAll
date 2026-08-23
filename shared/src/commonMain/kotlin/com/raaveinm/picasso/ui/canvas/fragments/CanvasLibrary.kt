package com.raaveinm.picasso.ui.canvas.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.user.OwnedGame
import com.raaveinm.picasso.ui.actions.libraryRedirect
import com.raaveinm.pickusall.core.designsystem.components.GameCard

//
// Created by Kirill "Raaveinm" on 8/18/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Composable
fun CanvasLibrary(
    modifier: Modifier = Modifier,
    libraryList: List<OwnedGame>,
) {
    BoxWithConstraints(modifier = modifier) {
        val minCellSize = if (maxWidth < 700.dp) 110.dp else 220.dp

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minCellSize),
//            contentPadding = PaddingValues(Dimensions.medium),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center,
        ) {
            items(libraryList) { game ->
                val uriHandler = LocalUriHandler.current
                GameCard(
                    modifier = Modifier,
                    gameId = game.appId,
                    text = game.name,
                    onClick = { libraryRedirect(game.appId, uriHandler) }
                )
            }
        }
    }
}

@Preview
@Composable
fun CanvasLibraryPreview() {
    CanvasLibrary(
        modifier = Modifier,
        libraryList = listOf(
            OwnedGame(
                appId = 1808500,
                name = "Arc Raiders",
                playtime2Weeks = 407,
                playtimeForever = 16877,
                imgIconUrl = "c284e73b6f3321864805d66f99924a0da9f0b219",
                hasCommunityVisibleStats = true,
                playtimeWindowsForever = 2935,
                playtimeMacForever = 0,
                playtimeLinuxForever = 13941,
                playtimeDeckForever = 0,
                rtimeLastPlayed = 1787003896,
                playtimeDisconnected = 37,
            ),
            OwnedGame(
                appId = 1091500,
                name = "Cyberpunk 2077",
                playtime2Weeks = 310,
                playtimeForever = 24634,
                imgIconUrl = "6897c3848f3e0350d512f59d5bae174a1e3739f9",
                hasCommunityVisibleStats = true,
                playtimeWindowsForever = 21363,
                playtimeMacForever = 0,
                playtimeLinuxForever = 3270,
                playtimeDeckForever = 0,
                rtimeLastPlayed = 1786658136,
                playtimeDisconnected = 0,
            ),
            OwnedGame(
                appId = 2073850,
                name = "THE FINALS",
                playtime2Weeks = 499,
                playtimeForever = 4674,
                imgIconUrl = "9532db560dca3b4982f4af3f5981b6b2ce2a6909",
                hasCommunityVisibleStats = true,
                playtimeWindowsForever = 460,
                playtimeMacForever = 0,
                playtimeLinuxForever = 4213,
                playtimeDeckForever = 0,
                rtimeLastPlayed = 1786914018,
                playtimeDisconnected = 0,
            ),
            OwnedGame(
                appId = 1601580,
                name = "Frostpunk 2",
                playtimeForever = 417,
                imgIconUrl = "5e66161686d4e2503a8a42aab9e8bc1c46c68fc1",
                hasCommunityVisibleStats = true,
                playtimeWindowsForever = 384,
                playtimeMacForever = 11,
                playtimeLinuxForever = 20,
                playtimeDeckForever = 0,
                rtimeLastPlayed = 1767214834,
                playtimeDisconnected = 0,
                playtime2Weeks = 0
            ),
            OwnedGame(
                appId = 264710,
                name = "Subnautica",
                playtimeForever = 5522,
                imgIconUrl = "8a14ceef6e230330a916d7a6324b8c52d464d569",
                hasCommunityVisibleStats = true,
                playtimeWindowsForever = 5455,
                playtimeMacForever = 51,
                playtimeLinuxForever = 15,
                playtimeDeckForever = 0,
                rtimeLastPlayed = 1776366237,
                playtimeDisconnected = 0,
                playtime2Weeks = 0,
            ),
        )
    )
}