package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.theme.SpecialColors
import com.raaveinm.pickusall.core.designsystem.utils.ImageUtility

// Below this width the layout stacks vertically instead of placing the
// screenshot carousel next to the cover art, matching Material's compact
// window size class cutoff.
private val CompactBreakpoint = 600.dp

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

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(Shapes.averageShape))
            .background(MaterialTheme.colorScheme.inverseOnSurface)
    ) {
        if (maxWidth < CompactBreakpoint) {
            GamePreviewCompact(
                gameName = gameName,
                gameId = gameId,
                inLibrary = inLibrary,
                gameTags = gameTags,
                screenshotLinks = linkLibrary
            )
        } else {
            GamePreviewExpanded(
                gameName = gameName,
                gameId = gameId,
                inLibrary = inLibrary,
                gameTags = gameTags,
                screenshotLinks = linkLibrary
            )
        }
    }
}

@Composable
private fun GamePreviewExpanded(
    gameName: String?,
    gameId: Int,
    inLibrary: Boolean?,
    gameTags: List<String>,
    screenshotLinks: List<String>
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GameCard(
            modifier = Modifier.size(180.dp, 270.dp),
            gameId = gameId,
            sourceSize = GameCardSize.LIBRARY_600_X_900,
            text = null
        )

        Column {
            GamePreviewTitle(gameName, Modifier.fillMaxWidth())

            HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = Dimensions.extraSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GamePreviewStatus(inLibrary, Modifier.padding(start = Dimensions.sMedium))
                Spacer(Modifier.weight(1f))
                GamePreviewTags(
                    gameTags = gameTags,
                    modifier = Modifier.padding(end = Dimensions.sMedium)
                )
            }

            HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = Dimensions.extraSmall))

            LazyRow(Modifier.fillMaxWidth()) {
                screenshotLinks.forEach { link ->
                    item {
                        ImageUtility(
                            imageUrl = link,
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
    }
}

@Composable
private fun GamePreviewCompact(
    gameName: String?,
    gameId: Int,
    inLibrary: Boolean?,
    gameTags: List<String>,
    screenshotLinks: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth().padding(Dimensions.small)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GameCard(
                modifier = Modifier.size(100.dp, 150.dp),
                gameId = gameId,
                sourceSize = GameCardSize.LIBRARY_300_X_450,
                text = null
            )

            Column(modifier = Modifier.padding(start = Dimensions.small)) {
                GamePreviewTitle(gameName, Modifier.fillMaxWidth(), maxLines = 2)
                Spacer(Modifier.size(Dimensions.extraSmall))
                GamePreviewStatus(inLibrary)
            }
        }

        if (gameTags.isNotEmpty()) {
            GamePreviewTags(
                gameTags = gameTags,
                modifier = Modifier.fillMaxWidth().padding(top = Dimensions.small)
            )
        }

        HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = Dimensions.small))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.extraSmall)
        ) {
            screenshotLinks.forEach { link ->
                item {
                    ImageUtility(
                        imageUrl = link,
                        modifier = Modifier.size(220.dp, 124.dp),
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
}

@Composable
private fun GamePreviewTitle(
    gameName: String?,
    modifier: Modifier = Modifier,
    maxLines: Int = 1
) {
    Text(
        text = gameName ?: "Unknown",
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
        fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
        fontWeight = FontWeight.Bold,
        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
        softWrap = true,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun GamePreviewStatus(inLibrary: Boolean?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = "GameCardPreview/Status",
            tint = when (inLibrary) {
                true -> SpecialColors.onlineColor
                false -> SpecialColors.offlineColor
                null -> SpecialColors.errorColor
            }
        )
        Text(
            text = when (inLibrary) {
                true -> "In Library"
                false -> "Not in Library"
                null -> "Unknown"
            },
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = Dimensions.extraSmall),
            fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
            fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
        )
    }
}

@Composable
private fun GamePreviewTags(gameTags: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.sMedium)
    ) {
        gameTags.forEach { tag ->
            Text(
                text = tag,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            )
        }
    }
}

@Preview
@Composable
fun GameCardPreview() {
    GamePreview(
        modifier = Modifier.size(1024.dp, 300.dp),
        gameId = 1601580,
        gameName = "Frostpunk 2",
        screenshotFilename = listOf(
            "5fcef70d6bc626f4c0cfc74826c3a27125bd1376/ss_5fcef70d6bc626f4c0cfc74826c3a27125bd1376",
            "ss_a61f90216ab7b217a3d8faec25d3d0ba7a5683bc"
        )
    )
}

@Preview
@Composable
fun GameCardPreviewCompact() {
    GamePreview(
        modifier = Modifier.size(360.dp, 420.dp),
        gameId = 1601580,
        gameName = "Frostpunk 2",
        gameTags = listOf("Strategy", "City Builder", "Survival"),
        screenshotFilename = listOf(
            "5fcef70d6bc626f4c0cfc74826c3a27125bd1376/ss_5fcef70d6bc626f4c0cfc74826c3a27125bd1376",
            "ss_a61f90216ab7b217a3d8faec25d3d0ba7a5683bc"
        )
    )
}
