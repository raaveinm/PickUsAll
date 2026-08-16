package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import org.jetbrains.compose.resources.painterResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ic_default_icon


@Composable
fun Messages (
    iconLink: String,
    username: String,
    textMessage: String,
    timestamp: String,
    isSender: Boolean,
    modifier: Modifier = Modifier,
    previousExisted: Boolean = false,
    isLast: Boolean = true
) {
    Column(modifier) {
        ///////////////////////////////////////////////
        // Author
        ///////////////////////////////////////////////
        if (!previousExisted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (!isSender) { Arrangement.Start } else { Arrangement.End },
                modifier = Modifier.fillMaxWidth().padding(bottom = Dimensions.small)
            ) {
                if (!isSender) {
                    AsyncImage(
                        model = iconLink,
                        contentDescription = "chat_icon/$username",
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(Shapes.circleShape)),
                        error = painterResource(Res.drawable.ic_default_icon)
                    )
                }
                Text(
                    text = username,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = Dimensions.small)
                )
                if (isSender) {
                    AsyncImage(
                        model = iconLink,
                        contentDescription = "chat_icon/$username",
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(Shapes.circleShape)),
                error = painterResource(Res.drawable.ic_default_icon)
                    )
                }
            }
        } else { Spacer(Modifier.size(Dimensions.small)) }

        ///////////////////////////////////////////////
        // Content
        ///////////////////////////////////////////////
        Box(
            Modifier
                .align(if (isSender) Alignment.End else Alignment.Start)
                .clip(shape = if (isSender) { Shapes.outcomeShape } else { Shapes.incomeChatShape })
                .background(if (!isSender) { MaterialTheme.colorScheme.primary }
                else { MaterialTheme.colorScheme.secondary })
        ) {
            Text(
                text = textMessage,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(Dimensions.small),
                softWrap = true,
                maxLines = 64
            )
        }

        ///////////////////////////////////////////////
        // timestamp
        ///////////////////////////////////////////////
        if (isLast) {
            Text(
                text = timestamp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(Dimensions.small),
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                textAlign = TextAlign.End
            )
        }
    }
}
