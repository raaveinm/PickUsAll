package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.obj.UserStatus
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.stringResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.log_in
import pickusall.core.designsystem.generated.resources.log_in_pending
import pickusall.core.designsystem.generated.resources.log_off
import pickusall.core.designsystem.generated.resources.settings
import pickusall.core.designsystem.generated.resources.settings_avatar
import pickusall.core.designsystem.generated.resources.settings_general
import pickusall.core.designsystem.generated.resources.settings_mini_profile
import pickusall.core.designsystem.generated.resources.settings_privacy
import pickusall.core.designsystem.generated.resources.settings_profile_background
import pickusall.core.designsystem.generated.resources.username_default

@Preview
@Composable
fun SidebarMenu(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAuthClick: () -> Unit = {},
    isAuthInProgress: Boolean = false,
    space: Dp = 48.dp,
    profileId: Long? = 1234,
    profileIcon: String = "",
    profileName: String = stringResource(Res.string.username_default),
    personaState: Int = 0
) {
    val text = MaterialTheme.typography.bodyMedium
    val uriHandler = LocalUriHandler.current
    val isLoggedIn = profileId != null
    val inverseOnSurface = MaterialTheme.colorScheme.inverseOnSurface

    Column(
        modifier = modifier
            .clip(Shapes.sideBarCardShape)
            .background(inverseOnSurface.copy(alpha = .72f))
            .clickable(false) {},
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        Box(Modifier
            .clip(Shapes.roundedSmall)
            .clickable { onProfileClick() }
            .background(inverseOnSurface)) {
            UserMiniProfile(
                modifier = Modifier.padding(top = Dimensions.extraSmall),
                iconLink = profileIcon,
                username = profileName,
                isOnline = personaState in 1..3,
                status = UserStatus.getOnlineStatus(personaState),
                hideActionButtons = true
            )
        }
        Spacer(Modifier.size(width = 2.dp, height = space))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.small)
        ) {
            val color = MaterialTheme.colorScheme.onSurface
            val settingList = listOf(
                Triple(Icons.Outlined.Public, stringResource(Res.string.settings_general), "info"),
                Triple(Icons.Outlined.AccountCircle, stringResource(Res.string.settings_avatar), "avatar"),
                Triple(Icons.Outlined.Wallpaper, stringResource(Res.string.settings_profile_background), "background"),
                Triple(Icons.Outlined.ManageAccounts, stringResource(Res.string.settings_mini_profile), "miniprofile"),
                Triple(Icons.Outlined.Shield, stringResource(Res.string.settings_privacy), "settings")
            )
            AnimatedVisibility(
                visible = isLoggedIn,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.small)
                ) {
                    settingList.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimensions.small)
                                .clip(Shapes.roundedSmall)
                                .clickable { uriHandler.openUri("https://steamcommunity.com/profiles/$profileId/edit/${it.third}") }
                                .background(inverseOnSurface)
                                .padding(Dimensions.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = it.first, tint = color, contentDescription = null)
                            Text(
                                text = it.second,
                                color = color,
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
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.small)
                    .clip(Shapes.roundedSmall)
                    .clickable(enabled = !isAuthInProgress) { onAuthClick() }
                    .background(if (isLoggedIn) MaterialTheme.colorScheme.errorContainer else inverseOnSurface)
                    .padding(Dimensions.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        if (isLoggedIn) Icons.AutoMirrored.Outlined.Logout
                        else Icons.AutoMirrored.Outlined.Login,
                    tint = color, contentDescription = null)
                Text(
                    text = stringResource(
                        when {
                            isAuthInProgress -> Res.string.log_in_pending
                            isLoggedIn -> Res.string.log_off
                            else -> Res.string.log_in
                        }
                    ),
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

        Spacer(Modifier.size(width = 2.dp, height = space))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(Shapes.roundedSmall)
                .background(inverseOnSurface)
                .clickable{ onSettingsClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "settings_field",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(Dimensions.medium).size(36.dp)
            )
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
