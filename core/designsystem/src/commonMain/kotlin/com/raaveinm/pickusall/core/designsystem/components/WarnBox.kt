package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onDismissClicked: () -> Unit = {}
) {
    val colorScheme: Triple<Color, Color, Color> = when(level){
        WarnLevel.INFO -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.secondary.copy(alpha = .48f)
        )

        WarnLevel.WARN -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primary.copy(alpha = .48f)
        )
        WarnLevel.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error.copy(alpha = .48f)
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
            Text(
                what,
                color = colorScheme.second,
                modifier = Modifier
                    .padding(Dimensions.small)
                    .weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(Shapes.circleShape)
                    .background(Color.Transparent)
                    .clickable { onDismissClicked() }
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