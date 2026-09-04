package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes

//
// Created by Kirill "Raaveinm" on 9/4/26.
//

@Preview
@Composable
fun ChatTextBar(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState = rememberTextFieldState(),
    onKeyboardAction: ()->Unit = {},
    hint: String = "sample"
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val textStyle = MaterialTheme.typography.bodyMedium
        TextField(
            state = textFieldState,
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            textStyle = textStyle,
            placeholder = { Text(hint) },
            onKeyboardAction = { onKeyboardAction() },
            scrollState = rememberScrollState(),
            shape = Shapes.roundedSmoother,
            colors = TextFieldDefaults.colors(
                errorIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        AnimatedVisibility(
            visible = !textFieldState.text.isEmpty(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = Dimensions.small)
        ) {
            Box(
                modifier = Modifier
                    .rotate(-20f)
                    .padding(bottom = 3.dp)
                    .clip(Shapes.circleShape)
                    .clickable {
                        onKeyboardAction()
                        textFieldState.clearText()
                    }

            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}