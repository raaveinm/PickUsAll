package com.raaveinm.picasso.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.raaveinm.picasso.data.repository.KeyBindingRepository
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyMap
import com.raaveinm.pickusall.core.designsystem.keybinding.LocalKeyMap
import org.koin.compose.koinInject

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

@Composable
fun ProvideKeyMap(content: @Composable () -> Unit) {
    val repository: KeyBindingRepository = koinInject()
    val keyMap: KeyMap by repository.keyMap.collectAsState(initial = KeyMap.defaults())

    CompositionLocalProvider(
        LocalKeyMap provides keyMap,
        content = content
    )
}
