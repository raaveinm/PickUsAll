package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun RefreshBox(
    modifier: Modifier,
    onRefresh: ()->Unit,
    isRefreshing: Boolean,
    content: @Composable BoxScope.()->Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        content = content
    )
}