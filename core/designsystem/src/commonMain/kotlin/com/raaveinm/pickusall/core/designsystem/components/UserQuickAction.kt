package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.vectorResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ic_steam_icon

@Composable
fun UserQuickAction(
    modifier: Modifier = Modifier,
    onMessageClick: () -> Unit = {},
    onAddToGroupClick: () -> Unit = {},
    onSteamProfileRedirectClick: () -> Unit = {},
) {
    val elements = listOf<@Composable () -> Unit>(
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Outlined.GroupAdd,
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = vectorResource(Res.drawable.ic_steam_icon),
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    )

    val actions = listOf(onMessageClick(), onAddToGroupClick(), onSteamProfileRedirectClick())

    Row(
        modifier
            .clip(Shapes.circleShape)
            .shadow(12.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = Dimensions.medium)
    ) {
        elements.forEach { content ->
            Box(
                Modifier
                    .padding(vertical = Dimensions.medium)
                    .size(48.dp)
                    .clip(Shapes.circleShape)
                    .clickable { actions.indexOf(
                        when (content) {
                            elements[0] -> onMessageClick()
                            elements[1] -> onAddToGroupClick()
                            else -> onSteamProfileRedirectClick()
                        }
                    ) },
                contentAlignment = Alignment.Center
            ) { content() }
        }
    }

}

@Preview
@Composable
fun UserQuickActionPreview() {
    UserQuickAction(
        modifier = Modifier,
        onMessageClick = {},
        onAddToGroupClick = {},
        onSteamProfileRedirectClick = {},
    )
}