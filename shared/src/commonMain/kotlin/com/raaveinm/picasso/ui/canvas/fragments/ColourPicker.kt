package com.raaveinm.picasso.ui.canvas.fragments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.core.model.lib.CommunityContent
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.pickusall.core.designsystem.components.GamePreview

@Composable
fun ColourPicker(
    modifier: Modifier = Modifier,
    gameList: List<CommunityContent>
) {
    LazyColumn(modifier) {
        item {
            gameList.forEach {
                GamePreview(
                    modifier = Modifier.fillMaxWidth(),
                    gameName = it.gameInfo.name,
                    gameId = it.gameInfo.appId,
                    screenshotFilename = it.imageUrl,
                    inLibrary = it.inLibrary,
                    gameTags = it.gameTags
                )
            }
        }
    }
}

@Preview
@Composable
fun ColourPickerPreview() {
    ColourPicker(
        modifier = Modifier,
        gameList = Mock.gameListCommunityContent
    )
}