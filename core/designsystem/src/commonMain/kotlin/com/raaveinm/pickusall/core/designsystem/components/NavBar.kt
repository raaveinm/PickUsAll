package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.vectorResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ic_sticker

@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    nestedModifier: Modifier = Modifier,
    fabModifier: Modifier = Modifier,
    selectedId: Int,
    onItemClick: (Int) -> Unit = {}
) {
    val elements = listOf<@Composable () -> Unit>(
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = vectorResource(Res.drawable.ic_sticker),
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Outlined.Forum,
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
        {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Outlined.Pets,
                contentDescription = "menu_item_home",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            nestedModifier
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = Dimensions.medium),
        ) {
            elements.forEach { content ->
                Box(Modifier
                    .padding(vertical = Dimensions.medium)
                    .size(48.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (selectedId == elements.indexOf(content)) MaterialTheme.colorScheme.tertiaryContainer
                        else Color.Transparent)
                    .clickable { onItemClick(elements.indexOf(content)) },
                    contentAlignment = Alignment.Center
                ) { content() }
            }
        }

        FloatingActionButton(
            onClick = { onItemClick(3) },
            modifier = fabModifier.padding(start = Dimensions.large),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "burger_menu_fab"
            )
        }
    }
}
