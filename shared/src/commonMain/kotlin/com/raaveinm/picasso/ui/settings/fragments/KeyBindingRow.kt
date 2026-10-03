package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.picasso.ui.settings.viewmodel.ChordBindings
import com.raaveinm.pickusall.core.designsystem.keybinding.Commands
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyChord
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

@Composable
fun KeyBindingRow(
    modifier: Modifier = Modifier,
    binding: ChordBindings,
    isRecording: Boolean,
    onRebindClick: () -> Unit,
    onCancelClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.roundedAverage)
            .background(
                if (isRecording) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            )
            .padding(Dimensions.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.medium)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimensions.extraSmall)
        ) {
            Text(
                text = binding.command.titleText(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = binding.command.descriptionText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.small),
            horizontalAlignment = Alignment.End
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.small)
            ) {
                ChordBadge(
                    modifier = Modifier.clip(Shapes.roundedSmall).clickable { onRebindClick() },
                    chord = binding.chord,
                    content = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "chord_reset",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp).padding(start = Dimensions.small)
                        )
                    }
                )
                if (!binding.isDefault && !isRecording) {
                    Icon(
                        imageVector = Icons.Filled.RestartAlt,
                        contentDescription = "chord_reset",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp).clip(Shapes.circleShape).clickable { onResetClick() }
                    )
                }
                if (isRecording) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "chord_reset",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp).clip(Shapes.circleShape).clickable { onCancelClick() }
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun KeyBindingRowPreview() {
    KeyBindingRow(
        binding = ChordBindings(
            command = Commands.REFRESH,
            chord = KeyChord(
                key = Key.R,
                isControl = true
            ),
            isDefault = false
        ),
        isRecording = false,
        onRebindClick = {},
        onCancelClick = {},
        onResetClick = {}
    )
}