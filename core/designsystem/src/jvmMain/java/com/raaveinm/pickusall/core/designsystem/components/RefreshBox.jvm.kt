package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

@Composable
actual fun RefreshBox(
    modifier: Modifier,
    onRefresh: ()->Unit,
    isRefreshing: Boolean,
    content: @Composable BoxScope.()->Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimensions.large)
                .size(48.dp)
                .clip(Shapes.roundedAverage)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .48f))
                .clickable(!isRefreshing) {
                onRefresh()
            },
            contentAlignment = Alignment.Center
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "refresh_library_jvm",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}