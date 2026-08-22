package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.painterResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ic_default_icon

@Composable
fun ChatTopBar(
    modifier: Modifier = Modifier,
    chatName: String,
    chatIcon: String? = null,
    onGoBackAction: () -> Unit = {},
    onChatIconAction: () -> Unit = {},
    hideUserIcon: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(Shapes.circleShape)
            .background(MaterialTheme.colorScheme.inverseOnSurface),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .background(Color.Transparent)
                .padding(Dimensions.small)
                .clip(Shapes.circleShape)
                .size(48.dp)
                .clickable { onGoBackAction() },
            onClick = { onGoBackAction() },
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "chat_top_bar_pop_back",
            )
        }

        Text(
            text = chatName,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            fontSize = MaterialTheme.typography.labelMedium.fontSize,
            fontStyle = MaterialTheme.typography.labelMedium.fontStyle,
            fontWeight = MaterialTheme.typography.labelMedium.fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            maxLines = 1
        )

        if (hideUserIcon) return@Row

        AsyncImage(
            model = chatIcon,
            contentDescription = "chat_top_bar_icon/$chatName",
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .padding(Dimensions.extraSmall)
                .size(64.dp)
                .clip(Shapes.circleShape)
                .clickable { onChatIconAction() },
            error = painterResource(Res.drawable.ic_default_icon)
        )
    }
}

@Preview
@Composable
fun ChatTopBarPreview() {
    ChatTopBar(
        modifier = Modifier,
        chatName = "Username",
        chatIcon = null,
        onGoBackAction = {},
        onChatIconAction = {}
    )
}