package com.raaveinm.picasso.data.repository

import androidx.compose.ui.input.key.Key
import com.raaveinm.core.datastore.settings.BehaviourSettingsStore
import com.raaveinm.core.datastore.settings.StoredChord
import com.raaveinm.pickusall.core.designsystem.keybinding.Commands
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyChord
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/**
 * Same single-source-of-truth shape as the Room-backed repositories: [keyMap] is what everyone
 * observes, and [rebind] / [reset] only write - the flow re-emits once the write lands.
 *
 * This is also the one place that converts between the UI's `KeyChord` and the datastore's
 * `StoredChord` (the datastore module can't see Compose types).
 */
class KeyBindingRepository(private val store: BehaviourSettingsStore) {

    /** Factory defaults with the user's saved chords on top; invalid saved chords fall back to default. */
    val keyMap: Flow<KeyMap> = store.settings.map { settings ->
        val overrides: Map<Commands, KeyChord> = settings.keyBindings.mapNotNull { (id: String, stored: StoredChord) ->
            // an id with no matching command is a leftover of a removed command - ignore it
            val command: Commands = Commands.entries.firstOrNull { it.name == id } ?: return@mapNotNull null
            command to stored.toChord()
        }.toMap()
        KeyMap.withOverrides(overrides)
    }

    /** The caller is expected to have run `KeyMap.check` first; this persists whatever it is given. */
    suspend fun rebind(command: Commands, chord: KeyChord) {
        store.saveKeyBinding(command.name, chord.toStored())
    }

    suspend fun reset(command: Commands) {
        store.resetKeyBinding(command.name)
    }

    private fun KeyChord.toStored(): StoredChord = StoredChord(
        keyCode = key.keyCode,
        isPrimary = isPrimary,
        isShift = isShift,
        isAlt = isAlt,
        isControl = isControl
    )

    private fun StoredChord.toChord(): KeyChord = KeyChord(
        key = Key(keyCode),
        isPrimary = isPrimary,
        isShift = isShift,
        isAlt = isAlt,
        isControl = isControl
    )
}
