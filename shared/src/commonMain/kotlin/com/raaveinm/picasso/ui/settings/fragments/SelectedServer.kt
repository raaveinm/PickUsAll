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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.ServerState
import com.raaveinm.picasso.ui.actions.TimeConverter.toDate
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/25/2026.
//

@Preview
@Composable
fun SelectedServer(
    modifier: Modifier = Modifier,
    selectedServer: ServerState = ServerState( // Default value for preview only
        url = "127.0.0.1",
        name = "sample",
        addedAt = 83764598
    ),
    servers: List<ServerState> = listOf(selectedServer),
    onNetworkPing: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onServerSelect: (ServerState) -> Unit = {},
    onServerDelete: (ServerState) -> Unit = {} // never called for [selectedServer]
) {
    var isDropdownExpanded: Boolean by remember { mutableStateOf(false) }

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
        targetValue = when(selectedServer.reachable) {
            true -> { Color.Green }
            false -> { Color.Red }
            else -> { blinkingYellow }
        },
        animationSpec = if (selectedServer.reachable == null){
            infiniteRepeatable(
                tween(200),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(200)
        },
        label = "server_state_anim"
    )

    Box(
        modifier = modifier
            .clip(Shapes.roundedSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Dimensions.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(selectedServer.reachable) {
                            true -> { Icons.Default.CloudDone }
                            false -> { Icons.Default.CloudOff }
                            else -> { Icons.Default.CloudSync }
                        },
                        tint = statusColor,
                        contentDescription = "status_icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedServer.name?:selectedServer.url,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = Dimensions.small)
                    )
                    if (!selectedServer.name.isNullOrEmpty()){
                        Text(
                            text = " (${selectedServer.url})",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    text = if (selectedServer.ping == null) "Unreachable;" else "Ping: ${selectedServer.ping} ms;", // todo replace with string resource
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Added at ${selectedServer.addedAt.toDate}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                Row {
                    IconButton(onClick = onNetworkPing) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "ping_server",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "edit_server",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Box {
                Card(
                    modifier = Modifier,
                    onClick = { isDropdownExpanded = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = Shapes.roundedExtraSmall
                ) {
                    Row(Modifier.padding(Dimensions.small)) {
                        Text(
                            text = selectedServer.name?:selectedServer.url,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            imageVector = Icons.Default.UnfoldMore,
                            contentDescription = "change_server",
                            modifier = Modifier.padding(start = Dimensions.small)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    servers.forEach { server ->
                        val isSelected: Boolean = server.id == selectedServer.id
                        DropdownMenuItem(
                            text = { Text(text = server.name?.takeIf { it.isNotBlank() } ?: server.url) },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "selected_server"
                                    )
                                }
                            },
                            trailingIcon = {
                                // the selected server can't be deleted, so it gets no button at all
                                if (!isSelected) {
                                    IconButton(
                                        onClick = {
                                            isDropdownExpanded = false
                                            onServerDelete(server)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "delete_server",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            },
                            onClick = {
                                isDropdownExpanded = false
                                if (!isSelected) onServerSelect(server)
                            }
                        )
                    }
                }
            }
        }
    }
}