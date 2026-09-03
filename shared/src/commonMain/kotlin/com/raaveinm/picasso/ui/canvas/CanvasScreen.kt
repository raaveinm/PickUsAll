package com.raaveinm.picasso.ui.canvas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.game.LibraryOrder
import com.raaveinm.picasso.ui.canvas.fragments.CanvasLibrary
import com.raaveinm.picasso.ui.canvas.fragments.ColourPicker
import com.raaveinm.picasso.ui.canvas.viewmodel.CanvasViewModel
import com.raaveinm.pickusall.core.designsystem.components.DropDownSelector
import com.raaveinm.pickusall.core.designsystem.components.RefreshBox
import com.raaveinm.pickusall.core.designsystem.components.Switch
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringArrayResource
import org.koin.compose.viewmodel.koinViewModel
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.sort_options
import pickusall.shared.generated.resources.switch_options

@Composable
fun CanvasScreen(
    modifier: Modifier = Modifier,
    viewModel: CanvasViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val options = stringArrayResource(Res.array.switch_options)
    var selectedSwitch by remember { mutableStateOf(0) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (selectedSwitch == 0) {
                RefreshBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .sizeIn(maxWidth = 1024.dp),
                    onRefresh = { viewModel.refreshLibrary() },
                    isRefreshing = isRefreshing
                ) {
                    CanvasLibrary(
                        modifier = Modifier.fillMaxSize(),
                        libraryList = uiState.userLibrary
                    )
                }
            } else {
                ColourPicker(
                    modifier = Modifier
                        .fillMaxSize()
                        .sizeIn(maxWidth = 1024.dp)
                        .padding(top = Dimensions.extraLarge * 2, start = Dimensions.medium, end = Dimensions.medium),
                    gameList = uiState.gameStore
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .padding(top = Dimensions.medium)
                .sizeIn(maxWidth = 280.dp),
            shape = FloatingActionButtonDefaults.shape, // RoundedCornerShape(16.dp)
            color = Color.Transparent,
            shadowElevation = 6.dp,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.small),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.small)
            ) {
                Switch(
                    modifier = Modifier.fillMaxWidth(),
                    selected = selectedSwitch,
                    onSelected = { selectedSwitch = it },
                    options = options,
                )

                AnimatedVisibility (selectedSwitch == 0) {
                    val sortOptions = stringArrayResource(Res.array.sort_options)

                    DropDownSelector(
                        modifier = Modifier.fillMaxWidth(),
                        selectedOption = sortOptions[uiState.libraryOrder.ordinal],
                        onOptionSelected = {
                            val newOrder = when (sortOptions.indexOf(it)) {
                                0 -> LibraryOrder.NAME
                                1 -> LibraryOrder.PLAYTIME
                                2 -> LibraryOrder.LAST_PLAYED
                                else -> LibraryOrder.NAME
                            }
                            viewModel.onOrderChanged(newOrder)
                        },
                        optionsList = sortOptions
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CanvasScreenPreview() {
    CanvasScreen()
}