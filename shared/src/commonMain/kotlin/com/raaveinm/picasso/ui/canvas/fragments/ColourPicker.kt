package com.raaveinm.picasso.ui.canvas.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.core.model.game.CommunityContent
import com.raaveinm.pickusall.core.designsystem.components.GamePreview
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun ColourPicker(
    modifier: Modifier = Modifier,
    communityContent: List<CommunityContent>
) {
    LazyColumn(
        modifier,
        contentPadding = PaddingValues(vertical = Dimensions.paddingAboveAverage),
        verticalArrangement = Arrangement.spacedBy(Dimensions.small)
    ) {
        item {
            communityContent.forEach {
                GamePreview(
                    modifier = Modifier.fillMaxWidth().padding(top = Dimensions.sMedium),
                    gameName = it.ownedGame.name,
                    gameId = it.ownedGame.appId,
                    screenshotFilename = it.imageUrl,
                    inLibrary = it.inLibrary,
                    gameTags = it.gameTags
                )
            }
        }
    }
}
