package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.picasso.ui.settings.viewmodel.Notice
import com.raaveinm.pickusall.core.designsystem.keybinding.Commands
import com.raaveinm.pickusall.core.designsystem.keybinding.IS_APPLE
import com.raaveinm.pickusall.core.designsystem.keybinding.RebindProblem
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.status_cancelled
import pickusall.shared.generated.resources.status_hint
import pickusall.shared.generated.resources.status_rebound
import pickusall.shared.generated.resources.status_recording
import pickusall.shared.generated.resources.status_rejected_needs_modifier
import pickusall.shared.generated.resources.status_rejected_reserved
import pickusall.shared.generated.resources.status_rejected_taken
import pickusall.shared.generated.resources.status_reset

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/**
 * One line telling the user what the key binding list is waiting for, or what the last attempt did.
 * [recordingFor] wins over [notice]: while a chord is being recorded nothing else matters.
 */
@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    recordingFor: Commands?,
    notice: Notice?,
) {
    val message: String = when {
        recordingFor != null -> stringResource(Res.string.status_recording, recordingFor.titleText())
        notice == null -> stringResource(Res.string.status_hint)
        else -> notice.messageText()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recordingFor != null) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        ),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = Dimensions.medium, vertical = Dimensions.sMedium),
            text = message,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun Notice.messageText(): String = when (this) {
    is Notice.Rebound -> stringResource(Res.string.status_rebound, command.titleText(), chord.display(IS_APPLE))
    is Notice.Reset -> stringResource(Res.string.status_reset, command.titleText(), chord.display(IS_APPLE))
    Notice.Cancelled -> stringResource(Res.string.status_cancelled)
    is Notice.Rejected -> {
        val pressed: String = chord.display(IS_APPLE)
        when (val reason: RebindProblem = problem) {
            RebindProblem.Reserved -> stringResource(Res.string.status_rejected_reserved, pressed)
            RebindProblem.NeedsModifier -> stringResource(Res.string.status_rejected_needs_modifier, pressed)
            is RebindProblem.Taken -> stringResource(Res.string.status_rejected_taken, pressed, reason.by.titleText())
        }
    }
}
