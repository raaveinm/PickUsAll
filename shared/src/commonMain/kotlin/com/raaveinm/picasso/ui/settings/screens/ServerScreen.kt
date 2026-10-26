package com.raaveinm.picasso.ui.settings.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.ServerState
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.settings.fragments.AddServerTile
import com.raaveinm.picasso.ui.settings.fragments.DeleteServerCard
import com.raaveinm.picasso.ui.settings.fragments.EditServerCard
import com.raaveinm.picasso.ui.settings.fragments.NewServerCard
import com.raaveinm.picasso.ui.settings.fragments.SelectedServer
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

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
    viewModel: SettingsViewModel,
    appViewModel: AppViewModel
) {
    val serverStates by viewModel.serverStates.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    var dialog by remember { mutableStateOf<ServerDialog?>(null) }

    LaunchedEffect(serverStates.map { it.id }) {
        serverStates.forEach { server ->
            if (server.reachable != true) viewModel.pingServer(server)
        }
    }

    val animatedBlur by animateFloatAsState(
        targetValue = if (dialog != null) 16f else 0f,
        animationSpec = tween(300)
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .blur(animatedBlur.dp)
                .sizeIn(maxWidth = 1024.dp)
                .padding(horizontal = Dimensions.medium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.small),
            contentPadding = PaddingValues(vertical = Dimensions.extraLarge)
        ) {
            item {
                val currentServer: ServerState = selectedServer ?: return@item

                SelectedServer(
                    modifier = Modifier.padding(bottom = Dimensions.small),
                    selectedServer = currentServer,
                    servers = serverStates,
                    onNetworkPing = { viewModel.pingServer(
                        server = currentServer,
                        postMessage = { l, m -> appViewModel.postMessage(l, m) }
                    ) },
                    onEditClick = { dialog = ServerDialog.Edit(currentServer) },
                    onServerSelect = { server -> viewModel.selectServer(server) },
                    onServerDelete = { server -> dialog = ServerDialog.Delete(server) }
                )
            }

            item { AddServerTile(onClick = { dialog = ServerDialog.New }) }
        }

        val currentDialog = dialog
        if (currentDialog != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(indication = null, interactionSource = MutableInteractionSource()) { dialog = null },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.clickable(indication = null, interactionSource = MutableInteractionSource(), onClick = {})) {
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