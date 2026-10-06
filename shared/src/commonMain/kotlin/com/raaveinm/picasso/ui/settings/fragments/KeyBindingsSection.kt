package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import com.raaveinm.picasso.ui.settings.viewmodel.BehaviourState
import com.raaveinm.pickusall.core.designsystem.keybinding.Commands
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyChord
import com.raaveinm.pickusall.core.designsystem.keybinding.toChordOrNull
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.key_binding

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/**
 * The whole "Key binding" block - title, status line and one row per command - laid out as a
 * single unit so it can sit in one `LazyColumn` item. It also owns key capture: while a rebind is
 * pending every key event inside it is turned into a chord and handed to [onChordCaptured].
 */
@Composable
fun KeyBindingsSection(
    modifier: Modifier = Modifier,
    state: BehaviourState,
    onRebindClick: (Commands) -> Unit,
    onCancelClick: () -> Unit,
    onResetClick: (Commands) -> Unit,
    onChordCaptured: (KeyChord) -> Unit,
    onLeave: () -> Unit,
) {
    val focusRequester: FocusRequester = remember { FocusRequester() }
    val isRecording: Boolean = state.recordingFor != null
    val latestOnLeave: () -> Unit by rememberUpdatedState(onLeave)

    LaunchedEffect(isRecording) {
        if (isRecording) focusRequester.requestFocus()
    }
    DisposableEffect(Unit) {
        onDispose { latestOnLeave() }
    }

    Column(
        modifier = modifier
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                if (!isRecording) return@onPreviewKeyEvent false
                event.toChordOrNull()?.let(onChordCaptured)
                true
            }
            .focusable(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.small)
    ) {
        Text(
            modifier = Modifier.padding(start = Dimensions.small),
            text = stringResource(Res.string.key_binding),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        state.bindings.forEach { binding ->
            KeyBindingRow(
                binding = binding,
                isRecording = state.recordingFor == binding.command,
                onRebindClick = { onRebindClick(binding.command) },
                onCancelClick = onCancelClick,
                onResetClick = { onResetClick(binding.command) }
            )
        }
    }
}
