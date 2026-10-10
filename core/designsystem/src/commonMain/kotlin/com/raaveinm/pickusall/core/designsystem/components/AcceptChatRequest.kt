package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ban_artist_suggestion
import pickusall.core.designsystem.generated.resources.ic_default_icon
import pickusall.core.designsystem.generated.resources.incoming_chat_request

//
// Created by Kirill "Raaveinm" on 10/10/2026.
//

@Composable
fun AcceptChatRequest(
    inviteeName: String,
    inviteeIcon: String?,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},  // Just stays as stranger
    onBlock: () -> Unit = {}    // Become Imposter
) {
    Card(Modifier.size(width = 400.dp, height = 600.dp)) {
        Box {
            var isBlockConfirmation by remember { mutableStateOf(false) }
            val blurLevel by animateFloatAsState(
                targetValue = if (isBlockConfirmation) 12f else 0f,
                animationSpec = tween(300)
            )
            Column(
                modifier = Modifier
                    .blur(blurLevel.dp)
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null,
                        enabled = isBlockConfirmation
                    ) { onReject() }
                    .fillMaxSize()
                    .padding(vertical = Dimensions.sMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val textStyle = MaterialTheme.typography.headlineMedium
                Text(
                    text = stringResource(Res.string.incoming_chat_request),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = textStyle,
                    maxLines = 1,
                    softWrap = true
                )
                AsyncImage(
                    model = inviteeIcon,
                    contentDescription = "caller_icon/$inviteeName",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .padding(Dimensions.extraSmall)
                        .size(256.dp)
                        .clip(Shapes.circleShape),
                    error = painterResource(Res.drawable.ic_default_icon)
                )
                Text(
                    text = inviteeName,
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = textStyle,
                    maxLines = 1,
                    softWrap = true
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        Modifier
                            .size(80.dp)
                            .clip(Shapes.circleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .clickable { isBlockConfirmation = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            null,
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Box(
                        Modifier
                            .size(80.dp)
                            .clip(Shapes.circleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onAccept() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            this@Card.AnimatedVisibility(
                visible = isBlockConfirmation,
                modifier = Modifier.align(Alignment.Center),
                enter = expandVertically(tween(300))
            ) { BlockRequest({ onReject(); isBlockConfirmation = false }, onBlock) }
        }
    }
}

@Preview
@Composable
private fun BlockRequest(onReject: () -> Unit = {}, onBlock: () -> Unit = {}) {
    Card(
        Modifier.size(width = 324.dp, height = 128.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        val textStyle = MaterialTheme.typography.bodyLarge
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = Dimensions.sMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = stringResource(Res.string.ban_artist_suggestion),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                style = textStyle,
                maxLines = 1,
                softWrap = true,
                textAlign = TextAlign.Center
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(Shapes.circleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable { onReject() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(Shapes.circleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onBlock() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AcceptChatRequestPreview() {
    AcceptChatRequest("SNAKE", "https://avatars.fastly.steamstatic.com/869f38905d075f5cba191c845447b568aa6e44bf_full.jpg")
}