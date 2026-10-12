package com.raaveinm.picasso.ui.actions

import androidx.compose.foundation.Indication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

actual fun Modifier.Companion.sideAction(
    interactionSource: MutableInteractionSource?,
    indication: Indication?,
    enabled: Boolean,
    onClickLabel: String?,
    role: Role?,
    onSideActionLabel: String?,
    onSideAction: (() -> Unit)?,
    onDoubleClick: (() -> Unit)?,
    hapticFeedbackEnabled: Boolean,
    onClick: () -> Unit
): Modifier = Modifier.combinedClickable(
    interactionSource = interactionSource,
    indication = indication,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onLongClickLabel = onSideActionLabel,
    onLongClick = onSideAction,
    onDoubleClick = onDoubleClick,
    hapticFeedbackEnabled = hapticFeedbackEnabled,
    onClick = onClick
)