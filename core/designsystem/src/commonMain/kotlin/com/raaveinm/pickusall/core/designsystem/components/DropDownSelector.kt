package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun DropDownSelector(
    modifier: Modifier = Modifier,
    optionsList: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Column (modifier
        .clip(RoundedCornerShape(36.dp))
        .background(MaterialTheme.colorScheme.primaryContainer)
        .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(top = if (isExpanded) Dimensions.medium else Dimensions.none) ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                selectedOption,
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimensions.small),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                softWrap = true,
                maxLines = 1
            )

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(Dimensions.small)
            )
        }

        if (isExpanded) {
            optionsList.forEach { option ->
                Box(
                    Modifier
                        .background(Color.Transparent)
                        .clickable{
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        softWrap = true,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.padding(Dimensions.medium))
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