package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.pickusall.core.designsystem.keybinding.IS_APPLE
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyChord
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

@Composable
fun ChordBadge(
    modifier: Modifier = Modifier,
    chord: KeyChord,
    content: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = Shapes.roundedSmall,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimensions.sMedium,
                vertical = Dimensions.extraSmall
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chord.display(IS_APPLE),
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            content()
        }
    }
}

@Preview
@Composable
fun ChordBadgePreview(){
    ChordBadge(
        modifier = Modifier,
        chord = KeyChord(
            key = Key.C,
            isPrimary = false,
            isShift = false,
            isAlt = true,
            isControl = false
        )
    )
}