package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.raaveinm.pickusall.core.designsystem.components.ToggleSwitch
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.toggle_setting

//
// Created by Kirill "Raaveinm" on 10/26/2026.
//

@Preview
@Composable
fun ToggleSetting(
    modifier: Modifier = Modifier,
    onToggle: () -> Unit = {},
    isChecked: Boolean = false,
    isInProgress: Boolean = false,
    settingNameRes: StringResource = Res.string.toggle_setting,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
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
            ToggleSwitch(
                isChecked = isChecked,
                isInProgression = isInProgress,
                onToggle = onToggle
            )
        }
    }
}
