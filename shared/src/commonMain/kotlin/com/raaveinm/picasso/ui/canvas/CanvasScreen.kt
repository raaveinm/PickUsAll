package com.raaveinm.picasso.ui.canvas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.picasso.ui.canvas.fragments.CanvasLibrary
import com.raaveinm.picasso.ui.canvas.fragments.ColourPicker
import com.raaveinm.picasso.ui.canvas.viewmodel.CanvasViewModel
import com.raaveinm.pickusall.core.designsystem.components.DropDownSelector
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
    val options = stringArrayResource(Res.array.switch_options)
    var selectedSwitch by remember { mutableStateOf(0) }

    Column(
        modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().sizeIn(maxWidth = 1024.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Switch(
                Modifier.sizeIn(maxWidth = 256.dp),
                selected = selectedSwitch,
                onSelected = { selectedSwitch = it },
                options = options,
            )
            if (selectedSwitch == 0) {
                val options = stringArrayResource(Res.array.sort_options)
                var selected by remember { mutableStateOf(0) }

                DropDownSelector(
                    modifier = Modifier
                        .zIndex(2f)
                        .sizeIn(maxWidth = 256.dp)
                        .padding(top = Dimensions.extraLarge, bottom = Dimensions.medium),
                    selectedOption = options[selected],
                    onOptionSelected = {
                        selected = options.indexOf(it)
                    },
                    optionsList = options
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().padding(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (selectedSwitch == 0) {
                CanvasLibrary(
                    modifier = Modifier.sizeIn(maxWidth = 1024.dp),
                    libraryList = uiState.userLibrary
                )
            } else {
                ColourPicker(
                    modifier = Modifier
                        .sizeIn(maxWidth = 1024.dp)
                        .padding(Dimensions.medium),
                    gameList = uiState.gameStore
                )
            }
        }
    }
}

@Preview
@Composable
fun CanvasScreenPreview() {
    CanvasScreen()
}