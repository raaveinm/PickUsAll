package com.raaveinm.picasso.ui.actions

import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

//
// Created by Kirill "Raaveinm" on 10/12/2026.
//

expect fun Modifier.Companion.sideAction(
    interactionSource: MutableInteractionSource? = MutableInteractionSource(),
    indication: Indication? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onSideActionLabel: String? = null,
    onSideAction: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    hapticFeedbackEnabled: Boolean = true,
    onClick: () -> Unit,
): Modifier