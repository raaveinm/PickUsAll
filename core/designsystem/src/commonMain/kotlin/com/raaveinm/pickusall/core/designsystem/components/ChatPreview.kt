package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.painterResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ic_default_icon

@Composable
fun ChatPreview(
    modifier: Modifier = Modifier,
    chatTitle: String,
    iconLink: String,
    lastMessage: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .sizeIn(minHeight = 80.dp)
            .clip(RoundedCornerShape(Shapes.smallShape))
            .background(color = MaterialTheme.colorScheme.inverseOnSurface)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        //region: PFP
        Box(
            modifier = Modifier
                .padding(Dimensions.small)
                .size(64.dp)
                .clip(Shapes.circleShape),
            Alignment.Center
        ) {  // return@Box
            AsyncImage(
                model = iconLink,
                contentDescription = "chat_icon/$chatTitle",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .heightIn(min = 180.dp)
                    .clip(Shapes.circleShape),
                error = painterResource(Res.drawable.ic_default_icon)
            )
            //endregion
        }
        //region: Chat Title
        Column(
            modifier = Modifier
                .padding(start = Dimensions.medium)
                .wrapContentSize()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = chatTitle,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                softWrap = true
            )

            Spacer(Modifier.weight(1f))

            if (lastMessage != null) {
                Text(
                    text = lastMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    softWrap = true
                )
            }
        }
        //endregion
    }
}