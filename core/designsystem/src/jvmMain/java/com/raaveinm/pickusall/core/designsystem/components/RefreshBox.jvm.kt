package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }

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
                .clickable(!isRefreshing) { onRefresh() }
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    when {
                        event.type == KeyEventType.KeyDown &&
                                (event.isCtrlPressed || event.isMetaPressed) &&
                                event.key == Key.R && !isRefreshing -> {
                            onRefresh()
                            true
                        }
                        else -> false
                    }
                }
                .onFocusChanged(
                    onFocusChanged = {
                        focusRequester.requestFocus()
                    }
                )
                .focusable(),
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
