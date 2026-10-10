package com.raaveinm.picasso

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.pickusall.core.designsystem.components.AcceptChatRequest
import com.raaveinm.pickusall.core.designsystem.components.CallAccept
import org.koin.compose.viewmodel.koinViewModel

//
// Created by Kirill "Raaveinm" on 10/10/26.
//


/**
 * used for side actions like accept / decline incoming call,
 * start a new chat from search
 *
 * Called from second undecorated application window
 */

@Composable
fun AppSideWindow() {
    val appViewModel = koinViewModel<AppViewModel>()
    var isVisible by remember { mutableStateOf(true) }
    CallAccept(
        "raaveinm",
        "https://avatars.fastly.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_full.jpg"
    )
    if (isVisible) {
        AcceptChatRequest(
            "SNAKE",
            "https://avatars.fastly.steamstatic.com/869f38905d075f5cba191c845447b568aa6e44bf_full.jpg",
            onBlock = { isVisible = false },
            onAccept = { isVisible = false },
            onReject = { isVisible = false }
        )
    }
}