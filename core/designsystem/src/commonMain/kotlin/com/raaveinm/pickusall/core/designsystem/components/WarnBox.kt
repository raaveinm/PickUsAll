package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DoneAll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel

//
// Created by Kirill "Raaveinm" on 9/11/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Preview
@Composable
fun WarnBox(
    modifier: Modifier = Modifier,
    level: WarnLevel = WarnLevel.WARN,
    what: String = "unknown",
    onCopyClicked: () -> Unit = {},
    onDismissClicked: () -> Unit = {}
) {
    val colorScheme: Triple<Color, Color, Color> = when(level){
        WarnLevel.INFO -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.secondary.copy(alpha = .64f)
        )

        WarnLevel.WARN -> Triple(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary,
            MaterialTheme.colorScheme.primary.copy(alpha = .64f)
        )
        WarnLevel.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error.copy(alpha = .64f)
        )
    }

    Box(
        modifier = modifier
            .shadow(12.dp)
            .clip(Shapes.roundedSmall)
            .background(colorScheme.third)
    ) {
        Row(
            modifier = modifier
                .padding(Dimensions.correctionSpace)
                .clip(Shapes.roundedSmall)
                .background(colorScheme.first),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var icon by remember { mutableStateOf(Icons.Default.CopyAll) }
            Text(
                what,
                color = colorScheme.second,
                modifier = Modifier
                    .padding(Dimensions.small)
                    .weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(Shapes.circleShape)
                    .background(Color.Transparent)
                    .clickable { onCopyClicked(); icon = Icons.Default.DoneAll },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    tint = colorScheme.second,
                    contentDescription = "copy_err_message"
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(Shapes.circleShape)
                    .background(Color.Transparent)
                    .clickable { onDismissClicked() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = colorScheme.second,
                    contentDescription = "dismiss_button"
                )
            }
        }
    }
}