package com.raaveinm.picasso.ui.friends.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

private data class Action(
    val onClick: (ContactLevel?) -> Unit,
    val icon: ImageVector,
    val colorPrimary: Color,
    val colorSecondary: Color
)

@Preview
@Composable
fun ArtistAliasAction(
    modifier: Modifier = Modifier,
    onSetStatusClicker: (ContactLevel?) -> Unit = {},
) {
    UserMiniProfile(
        modifier = modifier,
        iconLink = "",
        username = "raaveinm",
        isOnline = true,
        status = "sample"
    ){
        val actions = listOf(
            Action({ onSetStatusClicker(ContactLevel.IMPOSTER) }, Icons.Outlined.ThumbDown, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer),
            Action({ onSetStatusClicker(null) }, Icons.Outlined.Visibility, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant),
            Action({ onSetStatusClicker(ContactLevel.ALLY) }, Icons.Outlined.ThumbUp, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer),
            Action({ onSetStatusClicker(ContactLevel.FRIEND) }, Icons.Outlined.Favorite, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        )

        actions.forEach { elem ->
            Box(
                modifier = Modifier
                    .padding(end = Dimensions.small)
                    .size(48.dp)
                    .clip(Shapes.circleShape)
                    .background(elem.colorPrimary)
                    .clickable { elem.onClick },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = elem.icon, contentDescription = null, tint = elem.colorSecondary)
            }
        }
    }
}
