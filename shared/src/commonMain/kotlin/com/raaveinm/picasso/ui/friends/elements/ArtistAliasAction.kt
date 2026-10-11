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
import androidx.compose.ui.draw.alpha
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

private const val INACTIVE_ALPHA: Float = 0.4f

private data class Action(
    val level: ContactLevel?,
    val icon: ImageVector,
    val tag: String,
    val colorPrimary: Color,
    val colorSecondary: Color
)

/**
 * A mini profile whose buttons are the four relationship levels (blocked / stranger / ally /
 * friend). The one matching [currentLevel] is drawn at full strength, the rest dimmed;
 * tapping one reports the level the user wants to move to (null = stranger).
 */
@Composable
fun ArtistAliasAction(
    modifier: Modifier = Modifier,
    iconLink: String,
    username: String,
    isOnline: Boolean,
    status: String,
    currentLevel: ContactLevel?,                     // null = stranger (no contact row)
    onSetStatusClicker: (ContactLevel?) -> Unit = {},
) {
    UserMiniProfile(
        modifier = modifier,
        iconLink = iconLink,
        username = username,
        isOnline = isOnline,
        status = status
    ) {
        val actions = listOf(
            Action(ContactLevel.IMPOSTER, Icons.Outlined.ThumbDown, "alias_action/imposter", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer),
            Action(null, Icons.Outlined.Visibility, "alias_action/stranger", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant),
            Action(ContactLevel.ALLY, Icons.Outlined.ThumbUp, "alias_action/ally", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer),
            Action(ContactLevel.FRIEND, Icons.Outlined.Favorite, "alias_action/friend", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        )

        actions.forEach { elem ->
            Box(
                modifier = Modifier
                    .padding(end = Dimensions.small)
                    .size(48.dp)
                    .alpha(if (elem.level == currentLevel) 1f else INACTIVE_ALPHA)
                    .clip(Shapes.circleShape)
                    .background(elem.colorPrimary)
                    .clickable { onSetStatusClicker(elem.level) },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = elem.icon, contentDescription = elem.tag, tint = elem.colorSecondary)
            }
        }
    }
}

@Preview
@Composable
fun ArtistAliasActionPreview() {
    ArtistAliasAction(
        iconLink = "",
        username = "raaveinm",
        isOnline = true,
        status = "sample",
        currentLevel = ContactLevel.ALLY
    )
}
