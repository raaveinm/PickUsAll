package com.raaveinm.picasso

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.pickusall.core.designsystem.components.GameCard
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
@Preview
fun App() {
    val hazeState = rememberHazeState()
    CoilInitializer()
    PicassoTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .safeContentPadding()
                .fillMaxSize()
                .hazeSource(state = hazeState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row {
                        GameCard(gameId = 1808500,hazeState = hazeState, text = "567.8 h")
                        GameCard(gameId = 1091500,hazeState = hazeState)
                        GameCard(gameId = -1,hazeState = hazeState)
                    }
                    Text("Compose: $greeting",color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}