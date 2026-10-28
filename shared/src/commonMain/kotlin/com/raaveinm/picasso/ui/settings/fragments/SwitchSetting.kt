package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.components.Switch
import com.raaveinm.pickusall.core.designsystem.components.ToggleSwitch
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.toggle_setting

//
// Created by Kirill "Raaveinm" on 10/28/2026.
//

@Preview
@Composable
fun SwitchSetting(
    modifier: Modifier = Modifier,
    listOfOptions: List<String> = listOf("dark", "light", "auto"),
    selectedOption: Int = 0,
    onSelected: (Int) -> Unit = {},
    settingNameRes: StringResource = Res.string.toggle_setting,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    foregroundColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Float = 12f
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(size = shape),
        colors = CardDefaults.cardColors(contentColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimensions.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(settingNameRes),
                color = foregroundColor,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(.1f))
            Switch(
                modifier = Modifier.weight(1f),
                selected = selectedOption,
                onSelected = { onSelected(it) },
                options = listOfOptions,
                backgroundColor = backgroundColor,
                foregroundColor = foregroundColor
            )
        }
    }
}