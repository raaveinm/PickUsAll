package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.painterResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ic_default_icon

//
// Created by Kirill "Raaveinm" on 10/10/2026.
//

@Composable
fun CallAccept(
    callerName: String,
    callerIcon: String?,
    onAccept: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Card(Modifier.size(width = 400.dp, height = 600.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = Dimensions.sMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val textStyle = MaterialTheme.typography.headlineMedium
            AsyncImage(
                model = callerIcon,
                contentDescription = "caller_icon/$callerName",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .padding(Dimensions.extraSmall)
                    .size(256.dp)
                    .clip(Shapes.circleShape),
                error = painterResource(Res.drawable.ic_default_icon)
            )
            Text(
                text = callerName,
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
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onAccept() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Box(
                    Modifier
                        .size(80.dp)
                        .clip(Shapes.circleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CallAcceptPreview(){
    CallAccept(
        "raaveinm",
        "https://avatars.fastly.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_full.jpg"
    )
}