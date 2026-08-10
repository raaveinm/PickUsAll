package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.theme.SpecialColors
import com.raaveinm.pickusall.core.designsystem.utils.ImageUtility

@Composable
fun GamePreview(
    modifier: Modifier = Modifier,
    gameName: String?,
    gameId: Int,
    screenshotFilename: List<String>,
    inLibrary: Boolean? = null,
    gameTags: List<String> = listOf()
) {
    val linkLibrary: List<String> = screenshotFilename.map { path ->
        "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/$gameId/$path.1920x1080.jpg"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Shapes.averageShape))
            .background(MaterialTheme.colorScheme.inverseOnSurface)
    ) {
        // region: Main Card
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameCard(
                modifier = Modifier.size(180.dp, 270.dp),
                gameId = gameId,
                sourceSize = GameCardSize.LIBRARY_600_X_900,
                text = null
            )

            // region: Game Info
            Column {
                Text(
                    text = gameName?:"Unknown",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier,
                    fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    softWrap = true,
                    maxLines = 1
                )

                HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = Dimensions.extraSmall))

                // region: Status & Library
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Circle,
                        contentDescription = "GameCardPreview/Status",
                        tint = when(inLibrary) {
                            true -> SpecialColors.onlineColor
                            false -> SpecialColors.offlineColor
                            null -> SpecialColors.errorColor
                        }
                    )
                    Text(
                        text = when(inLibrary) {
                            true -> "In Library"
                            false -> "Not in Library"
                            null -> "Unknown"
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            top = Dimensions.correctionSpace, start = Dimensions.sMedium),
                        fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    )

                    Spacer(Modifier.weight(1f))

                    gameTags.forEach { tag ->
                        Text(
                            text = tag,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                top = Dimensions.correctionSpace, end = Dimensions.sMedium),
                            fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                            fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        )
                    }
                }
                // endregion

                HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = Dimensions.extraSmall))

                LazyRow(Modifier.fillMaxWidth()) {
                    linkLibrary.forEach { item ->
                        item {
                            ImageUtility(
                                imageUrl = item,
                                modifier = Modifier.size(320.dp, 180.dp),
                                contentDescription = "GameCardPreview/Library",
                                contentScale = ContentScale.Crop,
                                enableGlowEffect = false,
                                cornerRadius = Shapes.smallShape,
                                errorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                }
            }
            // endregion
        }
        // endregion
    }
}

@Preview
@Composable
fun GameCardPreview() {
    GamePreview(gameId = 1601580, gameName = "Frostpunk 2" , screenshotFilename = listOf("5fcef70d6bc626f4c0cfc74826c3a27125bd1376/ss_5fcef70d6bc626f4c0cfc74826c3a27125bd1376","ss_a61f90216ab7b217a3d8faec25d3d0ba7a5683bc"))
}