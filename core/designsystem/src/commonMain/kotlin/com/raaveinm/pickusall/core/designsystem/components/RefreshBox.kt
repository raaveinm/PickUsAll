package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//
// Created by Kirill "Raaveinm" on 9/3/26.
//

@Composable
expect fun RefreshBox(
    modifier: Modifier = Modifier,
    onRefresh: ()->Unit,
    isRefreshing: Boolean = false,
    content: @Composable BoxScope.()->Unit
)
