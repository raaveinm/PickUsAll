package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.ServerState
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 9/12/26.
//

@Preview
@Composable
fun ServerInfo(
    modifier: Modifier = Modifier,
    serverState: ServerState = ServerState( // Default value for preview only
        url = "127.0.0.1",
        addedAt = 83764598
    ),
    onEditClick: () -> Unit = {},
    onDeleteClick: (server: ServerState) -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(Shapes.roundedAverage)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(Dimensions.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val blinkingYellow by rememberInfiniteTransition().animateColor(
            initialValue = Color.Yellow.copy(alpha = 0.12f),
            targetValue = Color.Yellow,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1800,
                    delayMillis = 50,
                    easing = LinearOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BlinkingYellow"
        )
        val statusColor by animateColorAsState(
            targetValue = when(serverState.reachable) {
                true -> { Color.Green }
                false -> { Color.Red }
                else -> { blinkingYellow }
            },
            animationSpec = if (serverState.reachable == null){
                infiniteRepeatable(
                    tween(200),
                    repeatMode = RepeatMode.Reverse
                )
            } else {
                tween(200)
            },
            label = "server_state_anim"
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimensions.small)
        ) {
            Text(
                text = serverState.name?.takeIf { it.isNotBlank() } ?: serverState.url,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val typo = MaterialTheme.typography.labelSmall

            Row(verticalAlignment = Alignment.CenterVertically) {
//                Box(Modifier.size(8.dp).clip(Shapes.circleShape).background(statusColor))
                Icon(
                    imageVector = when(serverState.reachable) {
                        true -> { Icons.Default.CloudDone }
                        false -> { Icons.Default.CloudOff }
                        else -> { Icons.Default.CloudSync }
                    },
                    tint = statusColor,
                    contentDescription = "status_icon",
                    modifier = Modifier.size(16.dp)
                )
                if (serverState.name!= null) {
                    Text(
                        text = " ${serverState.url}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontFamily = typo.fontFamily,
                        fontStyle = typo.fontStyle,
                        fontWeight = typo.fontWeight,
                        fontSize = typo.fontSize,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Text(
                    text = " added at ${serverState.addedAt}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontFamily = typo.fontFamily,
                    fontStyle = typo.fontStyle,
                    fontWeight = typo.fontWeight,
                    fontSize = typo.fontSize,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "edit_server",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        IconButton(onClick = { onDeleteClick(serverState) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "delete_server",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
