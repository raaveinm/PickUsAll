package com.raaveinm.picasso.ui.settings.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.ServerState
import com.raaveinm.picasso.ui.settings.fragments.DeleteServerCard
import com.raaveinm.picasso.ui.settings.fragments.EditServerCard
import com.raaveinm.picasso.ui.settings.fragments.NewServerCard
import com.raaveinm.picasso.ui.settings.fragments.ServerInfo
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 9/12/26.
//

private sealed interface ServerDialog {
    data object New : ServerDialog
    data class Edit(val server: ServerState) : ServerDialog
    data class Delete(val server: ServerState) : ServerDialog
}

@Composable
fun ServerScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val serverStates by viewModel.serverStates.collectAsState()
    var dialog by remember { mutableStateOf<ServerDialog?>(null) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (dialog != null) 16f else 0f,
        animationSpec = tween(300)
    )

    Box(modifier.fillMaxSize().sizeIn(maxWidth = 1024.dp).padding(horizontal = Dimensions.medium)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().blur(animatedBlur.dp).padding(Dimensions.small),
            verticalArrangement = Arrangement.spacedBy(Dimensions.small)
        ) {
            items(serverStates, key = { it.id }) { serverState ->
                ServerInfo(
                    modifier = Modifier,
                    serverState = serverState,
                    onEditClick = { dialog = ServerDialog.Edit(serverState) },
                    onDeleteClick = { dialog = ServerDialog.Delete(serverState) }
                )
            }
            item {
                AddServerTile(onClick = { dialog = ServerDialog.New })
            }
        }

        val currentDialog = dialog
        if (currentDialog != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { dialog = null },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.clickable(onClick = {})) {
                    when (currentDialog) {
                        is ServerDialog.New -> NewServerCard(
                            onDismiss = { dialog = null },
                            onSubmit = { url, name ->
                                viewModel.addServer(url, name)
                                dialog = null
                            }
                        )

                        is ServerDialog.Edit -> EditServerCard(
                            server = currentDialog.server,
                            onDismiss = { dialog = null },
                            onSubmit = { url, name ->
                                viewModel.updateServer(currentDialog.server, url, name)
                                dialog = null
                            }
                        )

                        is ServerDialog.Delete -> DeleteServerCard(
                            server = currentDialog.server,
                            onDismiss = { dialog = null },
                            onConfirm = {
                                viewModel.deleteServer(currentDialog.server)
                                dialog = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddServerTile(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.roundedAverage)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
            .padding(Dimensions.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.small)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "add_server",
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "Add server",
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
