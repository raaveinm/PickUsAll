package com.raaveinm.pickusall.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes


@Composable
fun PicassoSearchBar(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    placeholder: String = "search",
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .clip(Shapes.roundedAverage)
            .background(color = backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ///////////////////////////////////////////////
        // Text
        ///////////////////////////////////////////////
        val textStyle = MaterialTheme.typography.bodyLarge
        TextField(
            state = textFieldState,
            modifier = Modifier.weight(1f),
            textStyle = textStyle,
            placeholder = { Text(placeholder) },
            trailingIcon = {
                if (textFieldState.text.isBlank())
                    Icon(Icons.Default.Search,null)
                else
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "search_bar/clear",
                        modifier = Modifier
                            .clip(Shapes.circleShape)
                            .clickable { textFieldState.clearText() }
                    )
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            scrollState = rememberScrollState(),
            contentPadding = PaddingValues(Dimensions.small),
            colors = TextFieldDefaults.colors(
                focusedPlaceholderColor = textColor,
                unfocusedPlaceholderColor = textColor,
                disabledPlaceholderColor = textColor,
                errorPlaceholderColor = textColor,
                focusedSupportingTextColor = Color.Transparent,
                unfocusedSupportingTextColor = Color.Transparent,
                disabledSupportingTextColor = Color.Transparent,
                errorSupportingTextColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )
        )
    }
}

@Preview
@Composable
fun SearchBarPreview() {
    val textFieldState = rememberTextFieldState()
    PicassoSearchBar(textFieldState = textFieldState)

}

