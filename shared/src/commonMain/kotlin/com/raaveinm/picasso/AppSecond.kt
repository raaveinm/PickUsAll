package com.raaveinm.picasso

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.raaveinm.pickusall.core.designsystem.components.PicassoSearchBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.search_bar_hint

@Preview
@Composable
fun AppSecond() {
    MaterialTheme {
        val searchState = rememberTextFieldState()
        Box {
            PicassoSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(.5f)
                    .fillMaxWidth()
                    .padding(Dimensions.large),
                textFieldState = searchState,
                placeholder = stringResource(Res.string.search_bar_hint)
            )
        }
    }
}