package com.raaveinm.picasso.ui.canvas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.picasso.ui.canvas.fragments.CanvasLibrary
import com.raaveinm.picasso.ui.canvas.fragments.ColourPicker
import com.raaveinm.pickusall.core.designsystem.components.DropDownSelector
import com.raaveinm.pickusall.core.designsystem.components.Switch
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun CanvasScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val options = listOf("Library", "Play Next")
        var selectedSwitch by remember { mutableStateOf(0) }

        Switch(
            Modifier.sizeIn(maxWidth = 256.dp),
            selected = selectedSwitch,
            onSelected = { selectedSwitch = it },
            options = options,
        )

        if (selectedSwitch == 0) {
            val options = listOf("name", "time", "hours played", "last played")
            var selected by remember { mutableStateOf(0) }

            DropDownSelector(
                modifier = Modifier.zIndex(2f).sizeIn(maxWidth = 256.dp),
                selectedOption = options[selected],
                onOptionSelected = {
                    selected = options.indexOf(it)
                },
                optionsList = options
            )

            CanvasLibrary(
                modifier = Modifier.sizeIn(maxWidth = 1024.dp),
                libraryList = Mock().libraryList
            )
        } else {
            ColourPicker(
                modifier = Modifier,
                gameList = Mock().gameListCommunityContent
            )
        }
    }
}

@Preview
@Composable
fun CanvasScreenPreview() {
    CanvasScreen()
}