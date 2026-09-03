package com.raaveinm.pickusall.core.designsystem.components

@androidx.compose.runtime.Composable
actual fun RefreshBox(
    modifier: Modifier
    onRefresh: ()->Unit,
    isRefreshing: Boolean,
    content: @Composable BoxScope.()->Unit
) {
}