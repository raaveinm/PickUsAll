package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.ServerState
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 9/12/26.
//

@Composable
private fun ServerFormCard(
    modifier: Modifier = Modifier,
    title: String,
    initialUrl: String = "",
    initialName: String = "",
    onDismiss: () -> Unit,
    onSubmit: (url: String, name: String) -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }
    var name by remember { mutableStateOf(initialName) }

    Column(
        modifier = modifier
            .widthIn(min = 280.dp, max = 360.dp)
            .clip(Shapes.roundedAverage)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Dimensions.large),
        verticalArrangement = Arrangement.spacedBy(Dimensions.medium)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Server URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Spacer(Modifier.width(Dimensions.small))
            Button(
                onClick = { onSubmit(url.trim(), name.trim()) },
                enabled = url.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun NewServerCard(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSubmit: (url: String, name: String) -> Unit
) {
    ServerFormCard(
        modifier = modifier,
        title = "Add server",
        onDismiss = onDismiss,
        onSubmit = onSubmit
    )
}

@Composable
fun EditServerCard(
    modifier: Modifier = Modifier,
    server: ServerState,
    onDismiss: () -> Unit,
    onSubmit: (url: String, name: String) -> Unit
) {
    ServerFormCard(
        modifier = modifier,
        title = "Edit server",
        initialUrl = server.url,
        initialName = server.name ?: "",
        onDismiss = onDismiss,
        onSubmit = onSubmit
    )
}

@Composable
fun DeleteServerCard(
    modifier: Modifier = Modifier,
    server: ServerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = modifier
            .widthIn(min = 280.dp, max = 360.dp)
            .clip(Shapes.roundedAverage)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Dimensions.large),
        verticalArrangement = Arrangement.spacedBy(Dimensions.medium)
    ) {
        Text(
            text = "Delete server?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "This will permanently delete " +
                "\"${server.name?.takeIf { it.isNotBlank() } ?: server.url}\" " +
                "and all chats and palettes cached from it. This can't be undone.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Spacer(Modifier.width(Dimensions.small))
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Delete")
            }
        }
    }
}
