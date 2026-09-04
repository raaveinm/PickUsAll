package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun RefreshBox(
    modifier: Modifier,
    onRefresh: ()->Unit,
    isRefreshing: Boolean,
    content: @Composable BoxScope.()->Unit
) {
}