package com.raaveinm.picasso.ui.canvas.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.add_game_to_complete
import pickusall.shared.generated.resources.cancel
import pickusall.shared.generated.resources.done
import pickusall.shared.generated.resources.game_id_field
import pickusall.shared.generated.resources.priority_field

@Composable
fun AddGameCard(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSubmit: (Int, Int) -> Unit
) {
    Card(modifier) {// 🖭
        var gameId by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("") }

        Column(
            Modifier.padding(Dimensions.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.add_game_to_complete),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )


            OutlinedTextField(
                // TODO make digits only
                value = gameId,
                onValueChange = { gameId = it },
                label = { Text(stringResource(Res.string.game_id_field)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField( // TODO make digits only
                value = priority,
                onValueChange = { priority = it },
                label = { Text(stringResource(Res.string.priority_field)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
                Spacer(Modifier.width(Dimensions.small))
                Button(
                    onClick = { onSubmit(gameId.toInt(), priority.toInt()) },
                    enabled = gameId.isNotBlank()
                ) {
                    Text(stringResource(Res.string.done))
                }
            }
        }
    }
}