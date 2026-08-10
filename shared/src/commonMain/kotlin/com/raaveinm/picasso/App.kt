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
import androidx.compose.foundation.layout.size
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
import com.raaveinm.pickusall.core.designsystem.components.GamePreview
import com.raaveinm.pickusall.core.designsystem.components.NavBar
import com.raaveinm.pickusall.core.designsystem.components.Switch
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer

@Composable
@Preview
fun App() {
    PicassoTheme {
        CoilInitializer()
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
                            GameCard(modifier = Modifier.size(300.dp,450.dp), gameId = 1808500, text = "567.8 h")
                            GameCard(modifier = Modifier.size(300.dp,450.dp), gameId = 1091500)
                            GameCard(modifier = Modifier.size(300.dp,450.dp), gameId = -1)
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

                    item {
                        GamePreview(
                            modifier = Modifier.sizeIn(maxWidth = 1024.dp),
                            gameId = 620980,
                            gameName = "Beat Saber",
                            screenshotFilename = listOf(
                                "ss_542d092f42c779c866167bec05c1da488bcd91f8",
                                "ss_b65444cc4513f34bd41fa6b0fe96cf11d94fea8d",
                                "ss_7df971fd7781d69dc455b15a400a6973ed7d3f36"
                            ),
                            inLibrary = true,
                            gameTags = listOf("vr", "rythm", "music", "moddable")
                        )
                    }
                    item {
                        GamePreview(
                            modifier = Modifier.sizeIn(maxWidth = 1024.dp),
                            gameId = 1601580,
                            gameName = "Frostpunk 2",
                            screenshotFilename = listOf(
                                "5fcef70d6bc626f4c0cfc74826c3a27125bd1376/ss_5fcef70d6bc626f4c0cfc74826c3a27125bd1376",
                                "ss_a61f90216ab7b217a3d8faec25d3d0ba7a5683bc"
                            ),
                            inLibrary = false,
                            gameTags = listOf("city builder", "strategy", "survival")
                        )
                    }
                     item {
                         GamePreview(
                             modifier = Modifier.sizeIn(maxWidth = 1024.dp),
                             gameId = -1,
                             gameName = "Game Name",
                             screenshotFilename = listOf(
                                 "5fcef70d6bc626f4c0cfc74826c3a27125bd1376/ss_5fcef70d6bc626f4c0cfc74826c3a27125bd1376",
                                 "ss_a61f90216ab7b217a3d8faec25d3d0ba7a5683bc"
                             ),
                             gameTags = listOf("stat1", "stat2", "stat3", "stat4")
                         )
                    }
                }
            }
        }
    }
}