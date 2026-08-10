package com.raaveinm.picasso

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.pickusall.core.designsystem.components.DropDownSelector
import com.raaveinm.pickusall.core.designsystem.components.GameCard
import com.raaveinm.pickusall.core.designsystem.components.NavBar
import com.raaveinm.pickusall.core.designsystem.components.Switch
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer

@Composable
@Preview
fun App() {
    CoilInitializer()
    PicassoTheme {
        var showContent by remember { mutableStateOf(false) }
        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.inverseOnSurface
            )
        )
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {

                val greeting = remember { Greeting().greet() }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    item {
                        Row {
                            GameCard(gameId = 1808500, text = "567.8 h")
                            GameCard(gameId = 1091500)
                            GameCard(gameId = -1)
                        }
                    }


                    item {
                        val options = listOf("bbb","rrr","aaa","fff")
                        var selected by remember { mutableStateOf("aaa") }
                        DropDownSelector(
                            modifier = Modifier.zIndex(2f).sizeIn(maxWidth = 256.dp),
                            selectedOption = selected,
                            onOptionSelected = {
                                selected = it
                            },
                            optionsList = options
                        )
                    }
                    item {
                        val options = listOf("bbb", "rrr", "aaa")

                        var selectedSwitch by remember { mutableStateOf(0) }

                        Switch(
                            Modifier.sizeIn(maxWidth = 312.dp),
                            selected = selectedSwitch,
                            onSelected = { selectedSwitch = it },
                            options = options,
                        )
                    }

                    item {
                        Text("Compose: $greeting", color = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    item {
                        var selected by remember{mutableStateOf(0)}
                        NavBar(selectedId=selected, onItemClick = { selected = it })
                    }
                }
            }
        }
    }
}