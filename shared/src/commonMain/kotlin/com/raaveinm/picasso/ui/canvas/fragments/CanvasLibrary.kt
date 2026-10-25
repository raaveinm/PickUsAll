package com.raaveinm.picasso.ui.canvas.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.user.OwnedGame
import com.raaveinm.picasso.ui.actions.libraryRedirect
import com.raaveinm.picasso.ui.actions.openStorePage
import com.raaveinm.picasso.ui.actions.sideAction
import com.raaveinm.pickusall.core.designsystem.components.DropDownMenu
import com.raaveinm.pickusall.core.designsystem.components.DropDownSelector
import com.raaveinm.pickusall.core.designsystem.components.GameCard
import com.raaveinm.pickusall.core.designsystem.components.ScrollBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.add_game_to_complete
import pickusall.shared.generated.resources.dismiss
import pickusall.shared.generated.resources.game_more_actions_label
import pickusall.shared.generated.resources.game_open_label
import pickusall.shared.generated.resources.open_in_store
import pickusall.shared.generated.resources.sort_options

//
// Created by Kirill "Raaveinm" on 8/18/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Composable
fun CanvasLibrary(
    modifier: Modifier = Modifier,
    libraryList: List<OwnedGame>,
    sortOptions: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onAddToQueue: (gameId: Int) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        val minCellSize = if (maxWidth < 700.dp) 110.dp else 220.dp
        val scrollState = rememberLazyGridState()
        var isSortVisible by remember { mutableStateOf(true) }

        val scrollingOffset: Int by remember {
            var previousIndex = 64
            derivedStateOf {
                val currentFirstIndex: Int = scrollState.firstVisibleItemIndex
                if (currentFirstIndex != previousIndex) {
                    val delta: Int = currentFirstIndex - previousIndex
                    previousIndex = currentFirstIndex
                    delta
                } else { 0 }
            }
        }

        LaunchedEffect(scrollingOffset) { isSortVisible = scrollingOffset < 0 }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minCellSize),
            state = scrollState,
            contentPadding = PaddingValues(
                top = Dimensions.paddingAboveAverage + PlatformSpecificDim.systemBarsPadding.calculateTopPadding(),
                bottom = Dimensions.paddingAboveAverage
            ),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center,
        ) {
            items(libraryList, key = { it.appId }) { game ->
                var sideSwitch by remember { mutableIntStateOf(0) }
                val uriHandler = LocalUriHandler.current
                Box(contentAlignment = Alignment.Center) {
                    AnimatedVisibility (
                        visible = sideSwitch != 0,
                        modifier = Modifier
                            .zIndex(3f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(interactionSource = MutableInteractionSource(), indication = null) {
                                sideSwitch = 0
                            },
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        val menuActions: List<Pair<String, () -> Unit>> = listOf(
                            stringResource(Res.string.dismiss) to { },
                            stringResource(Res.string.add_game_to_complete) to { onAddToQueue(game.appId) },
                            stringResource(Res.string.open_in_store) to { openStorePage(game.appId, uriHandler) },
                        )
                        DropDownMenu(
                            modifier = Modifier.zIndex(2f),
                            onItemSelected = { index ->
                                sideSwitch = 0
                                menuActions[index].second()
                            },
                            items = menuActions.map { it.first }
                        )
                    }
                    GameCard(
                        modifier = Modifier.sideAction(
                            onClickLabel = stringResource(Res.string.game_open_label, game.name),
                            onSideActionLabel = stringResource(Res.string.game_more_actions_label, game.name),
                            onSideAction = { sideSwitch = 1 },
                            onClick = { libraryRedirect(game.appId, uriHandler) }
                        ),
                        gameId = game.appId,
                        text = game.name
                    )
                }
            }
        }

        ScrollBar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(vertical = Dimensions.extraLarge)
                .padding(end = Dimensions.small),
            state = scrollState,
            autoHide = true,
        )

        AnimatedVisibility(
            visible = isSortVisible,
            enter = expandVertically(clip = false) + fadeIn(),
            exit = shrinkVertically(clip = false) + fadeOut(),
            modifier = Modifier.padding(
                top = Dimensions.extraLarge + PlatformSpecificDim.systemBarsPadding.calculateTopPadding()
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DropDownSelector(
                    modifier = Modifier
                        .sizeIn(maxWidth = 232.dp)
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.medium),
                    selectedOption = selectedOption,
                    onOptionSelected = onOptionSelected,
                    optionsList = sortOptions
                )
                HorizontalDivider(Modifier.padding(horizontal = Dimensions.medium))
            }
        }
    }
}

@Preview
@Composable
fun CanvasLibraryPreview() {
    CanvasLibrary(
        modifier = Modifier,
        sortOptions = stringArrayResource(Res.array.sort_options),
        selectedOption = stringArrayResource(Res.array.sort_options)[0],
        onOptionSelected = {},
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