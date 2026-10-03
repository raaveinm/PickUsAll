package com.raaveinm.core.datastore.settings

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/**
 * A key combination as stored on disk. Deliberately plain primitives: this module can't depend on
 * Compose, so the UI-side `KeyChord` is mapped to and from this one by `KeyBindingRepository`.
 *
 * @param keyCode Compose `Key.keyCode`. Platform-specific encoding - fine, the file is per-device.
 * @param isPrimary Cmd on Apple, Ctrl elsewhere; same meaning as `KeyChord.isPrimary`.
 */
data class StoredChord(
    val keyCode: Long,
    val isPrimary: Boolean,
    val isShift: Boolean,
    val isAlt: Boolean,
    val isControl: Boolean
)

/**
 * Everything under Settings / Behaviour.
 *
 * @param keyBindings Only the chords the user *changed*, keyed by command id (`Commands.name`).
 * A command that is absent keeps its factory default, so adding a new command later needs no migration.
 */
data class BehaviourSettings(
    val keyBindings: Map<String, StoredChord> = emptyMap()
)

///////////////////////////////////////////////
/// Encoding
///////////////////////////////////////////////

private const val CHORD_SEPARATOR: Char = ','
private const val CHORD_FIELD_COUNT: Int = 5

/** `keyCode,primary,shift,alt,control` with the booleans as 0/1, e.g. `55834574848,1,0,0,0`. */
internal fun StoredChord.encode(): String = listOf(
    keyCode.toString(),
    isPrimary.toBit(),
    isShift.toBit(),
    isAlt.toBit(),
    isControl.toBit()
).joinToString(CHORD_SEPARATOR.toString())

/** Inverse of [encode]; `null` for anything malformed so a damaged entry is dropped, never thrown on. */
internal fun String.decodeStoredChord(): StoredChord? {
    val parts: List<String> = split(CHORD_SEPARATOR)
    if (parts.size != CHORD_FIELD_COUNT) return null
    val keyCode: Long = parts[0].toLongOrNull() ?: return null
    val flags: List<Boolean> = parts.drop(1).map { it.toBitOrNull() ?: return null }
    return StoredChord(
        keyCode = keyCode,
        isPrimary = flags[0],
        isShift = flags[1],
        isAlt = flags[2],
        isControl = flags[3]
    )
}

private fun Boolean.toBit(): String = if (this) "1" else "0"

private fun String.toBitOrNull(): Boolean? = when (this) {
    "1" -> true
    "0" -> false
    else -> null
}
