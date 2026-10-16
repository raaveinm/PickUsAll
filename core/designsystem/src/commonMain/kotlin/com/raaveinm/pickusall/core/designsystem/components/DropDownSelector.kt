package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

@Composable
fun DropDownSelector(
    modifier: Modifier = Modifier,
    optionsList: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }
    var headerSize: IntSize by remember { mutableStateOf(IntSize.Zero) }
    val density: Density = LocalDensity.current
    val gapPx: Int = with(density) { Dimensions.small.roundToPx() }
    val degrees by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "button_rotation"
    )

    Column(modifier.background(Color.Transparent)) {
        Row(
            modifier = Modifier
                .onSizeChanged { headerSize = it }
                .clip(Shapes.roundedSmall)
                .background(containerColor)
                .clickable { isExpanded = !isExpanded }
                .padding(Dimensions.correctionSpace),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                selectedOption,
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimensions.small),
                color = contentColor,
                softWrap = true,
                maxLines = 1
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.padding(Dimensions.small).rotate(degrees = degrees)
            )
        }

        if (isExpanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(x = 0, y = headerSize.height + gapPx), // right under the header
                onDismissRequest = { isExpanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                // enter animation only: the Popup leaves composition as soon as isExpanded is false
                AnimatedVisibility(
                    visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
                    modifier = Modifier
                        .width(with(density) { headerSize.width.toDp() }) // same width as the header
                        .clip(Shapes.roundedSmall)
                        .background(containerColor)
                        .padding(Dimensions.correctionSpace)
                ) {
                    Column {
                        optionsList.forEach { option ->
                            Box(
                                Modifier
                                    .background(Color.Transparent)
                                    .clickable {
                                        onOptionSelected(option)
                                        isExpanded = false
                                    }
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier
                                        .padding(Dimensions.small)
                                        .background(Color.Transparent)
                                        .fillMaxWidth(),
                                    color = contentColor,
                                    softWrap = true,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DropDownSelectorPreview() {
    DropDownSelector(
        optionsList = listOf("aaa", "bbb","ccc"),
        selectedOption = "aaa",
        onOptionSelected = {}
    )
}