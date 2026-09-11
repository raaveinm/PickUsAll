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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.stringResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.settings

@Preview
@Composable
fun SidebarMenu(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    space: Dp = 48.dp
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp,8.dp,8.dp,32.dp))
            .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = .72f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        Box(Modifier
            .clip(Shapes.roundedSmall)
            .background(MaterialTheme.colorScheme.inverseOnSurface)) {
            UserMiniProfile(
                modifier = Modifier.padding(top = Dimensions.medium),
                iconLink = "",
                username = "username",
                isOnline = true,
                status = "status",
                onMessageClick = {},
                onSteamProfileRedirectClick = {},
                hideActionButtons = true
            )
        }
        Spacer(Modifier.size(width = 2.dp, height = space))
        // For additional content
        Spacer(Modifier.size(width = 2.dp, height = space))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(Shapes.roundedSmall)
                .background(MaterialTheme.colorScheme.inverseOnSurface)
                .clickable{ onSettingsClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "settings_field",
                modifier = Modifier.padding(Dimensions.medium).size(36.dp)
            )
            val text = MaterialTheme.typography.bodyMedium
            Text(
                text = stringResource(Res.string.settings),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                fontSize = text.fontSize,
                fontStyle = text.fontStyle,
                fontWeight = text.fontWeight,
                fontFamily = text.fontFamily,
                textAlign = TextAlign.Center,
            )
        }
    }
}
