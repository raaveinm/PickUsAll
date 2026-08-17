package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.obj.GameCardSize
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.utils.ImageUtility

//
// Created by Kirill "Raaveinm" on 8/4/26.
//

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    gameId: Int,
    sourceSize: String = GameCardSize.LIBRARY_600_X_900,
    text: String? = "blep"
) {
    val imageLink =
        "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$gameId/$sourceSize"

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {

        ///////////////////////////////////////////////
        // Card
        ///////////////////////////////////////////////

        ImageUtility(
            imageUrl = imageLink,
            modifier = Modifier,
            contentDescription = "game_card",
            contentScale = ContentScale.Crop,
            enableGlowEffect = true,
            cornerRadius = Shapes.averageShape
        )

        if (text == null) return@Box // do not displaying text card if text is null

        ///////////////////////////////////////////////
        // Low Bar Text Chip
        ///////////////////////////////////////////////

        Card(
            modifier = Modifier.size(128.dp, 24.dp),
            shape = RoundedCornerShape(Shapes.smallShape),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .84f),
                contentColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.surface
            ),
            content = {
                Text(
                    text = text,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}

@Preview(device = Devices.DEFAULT) @Composable
fun GameCardPreviewArcRaiders() { GameCard(gameId = 1808500) }

@Preview(device = Devices.DEFAULT) @Composable
fun GameCardPreviewCyberpunk() { GameCard(gameId = 1091500) }

@Preview(device = Devices.DEFAULT) @Composable
fun GameCardPreviewPlain() { GameCard(gameId = -1) }
