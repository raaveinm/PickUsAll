package com.raaveinm.picasso.ui.friends.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

/** One round icon button for `UserMiniProfile`'s action slot, styled like its built-in message / Steam buttons. */
@Composable
fun MiniProfileButton(
    icon: ImageVector,
    tag: String,            // contentDescription, `snake_case/scope` like the built-in ones
    onClick: () -> Unit
) {
    Box(
        Modifier
            .padding(end = Dimensions.extraSmall)
            .clip(Shapes.circleShape)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = tag,
            modifier = Modifier.padding(Dimensions.small)
        )
    }
}
