package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/25/2026.
//

@Composable
fun AddServerTile(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.roundedAverage)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
            .padding(Dimensions.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.small)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "add_server",
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "Add server",
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}