package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.theme.SpecialColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ic_default_icon
import pickusall.core.designsystem.generated.resources.ic_steam_icon

@Composable
fun UserMiniProfile(
    modifier: Modifier = Modifier,
    iconLink: String,
    username: String,
    isOnline: Boolean,
    status: String,
    onMessageClick: () -> Unit = {},
    onSteamProfileRedirectClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Shapes.smallShape))
            .background(MaterialTheme.colorScheme.inverseOnSurface),
        verticalAlignment = Alignment.CenterVertically
    ) {

        ///////////////////////////////////////////////
        // PFP
        ///////////////////////////////////////////////

        AsyncImage(
            model = iconLink,
            contentDescription = "mini_profile/$username",
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .padding(Dimensions.extraSmall)
                .size(64.dp)
                .clip(Shapes.circleShape),
            error = painterResource(Res.drawable.ic_default_icon)
        )


        ///////////////////////////////////////////////
        // Name / Status
        ///////////////////////////////////////////////

        Column(
            modifier = Modifier
                .padding(start = Dimensions.medium)
                .weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = username,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                softWrap = true
            )
            Row (
                modifier = Modifier.padding(vertical = Dimensions.extraSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    tint = if (isOnline) SpecialColors.onlineColor else SpecialColors.offlineColor,
                    contentDescription = "mini_profile/online_status",
                    modifier = Modifier.size(12.dp)
                )

                Spacer(Modifier.size(Dimensions.small))

                Text(
                    text = status,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    softWrap = true
                )
            }
        }

        ///////////////////////////////////////////////
        // Action Buttons
        ///////////////////////////////////////////////

        Box(
            Modifier
                .padding(horizontal = Dimensions.extraSmall)
                .clip(Shapes.circleShape)
                .clickable { onMessageClick() }
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "mini_profile/message",
                modifier = Modifier.padding(Dimensions.small)
            )
        }

        Box(
            Modifier
                .padding(end = Dimensions.extraSmall)
                .clip(Shapes.circleShape)
                .clickable { onSteamProfileRedirectClick() }
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_steam_icon),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "mini_profile/steam",
                modifier = Modifier.padding(Dimensions.small)
            )
        }
    }
}

@Preview
@Composable
fun UserMiniProfilePreview() {
    UserMiniProfile(
        modifier = Modifier,
        iconLink = "",
        username = "username",
        isOnline = true,
        status = "status",
        onMessageClick = {},
        onSteamProfileRedirectClick = {},
    )
}