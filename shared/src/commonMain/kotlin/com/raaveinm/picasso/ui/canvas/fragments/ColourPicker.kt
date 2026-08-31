package com.raaveinm.picasso.ui.canvas.fragments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.core.database.entities.api.game.Games
import com.raaveinm.core.model.game.CommunityContent
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.pickusall.core.designsystem.components.GamePreview

@Composable
fun ColourPicker(
    modifier: Modifier = Modifier,
    gameList: List<Games>
) {
    LazyColumn(modifier) {
        item {
            gameList.forEach {
                GamePreview(
                    modifier = Modifier.fillMaxWidth(),
                    gameName = it.name,
                    gameId = it.steamAppId,
                    screenshotFilename = listOfNotNull(it.headerImage),
                    inLibrary = null,
                    gameTags = emptyList()
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun ColourPickerPreview() {
//    ColourPicker(
//        modifier = Modifier,
//        gameList = Mock.gameListCommunityContent
//    )
//}