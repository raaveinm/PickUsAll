package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

//
// Created by Kirill "Raaveinm" on 9/11/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

@Preview
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier.size(width = 200.dp, height = 100.dp),
    icon: ImageVector = Icons.Default.Lan,
    name: String = "Server",
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = modifier
            .hoverable(interactionSource),
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onClick,
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            this@Card.AnimatedVisibility(isHovered){
                Icon(
                    imageVector = icon,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .32f),
                    contentDescription = "${name}_background",
                    modifier = Modifier
                        .zIndex(-1f)
                        .fillMaxSize()
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = "${name}_icon",
                    modifier = Modifier
                )
                val typo = MaterialTheme.typography.bodyLarge
                Text(
                    name,
                    modifier = Modifier.padding(start = Dimensions.sMedium),
                    fontSize = typo.fontSize,
                    fontStyle = typo.fontStyle,
                    fontWeight = typo.fontWeight,
                    fontFamily = typo.fontFamily,
                    softWrap = true
                )
            }
        }
    }
}