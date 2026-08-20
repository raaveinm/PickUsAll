package com.raaveinm.picasso

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.raaveinm.picasso.ui.canvas.CanvasScreen
import com.raaveinm.pickusall.core.designsystem.components.ChatPreview
import com.raaveinm.pickusall.core.designsystem.components.ChatTopBar
import com.raaveinm.pickusall.core.designsystem.components.DropDownSelector
import com.raaveinm.pickusall.core.designsystem.components.GameCard
import com.raaveinm.pickusall.core.designsystem.components.GamePreview
import com.raaveinm.pickusall.core.designsystem.components.Messages
import com.raaveinm.pickusall.core.designsystem.components.NavBar
import com.raaveinm.pickusall.core.designsystem.components.Switch
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile
import com.raaveinm.pickusall.core.designsystem.components.UserQuickAction
import com.raaveinm.pickusall.core.designsystem.obj.UserStatus
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
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
                .systemBarsPadding()
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
                            gameTags = listOf("vr", "rhythm", "music", "moddable")
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

                    item {
                        ChatPreview(
                            Modifier.sizeIn(maxWidth = 728.dp).fillMaxWidth(),
                            chatTitle = "username",
                            iconLink = "err",
                            lastMessage = "The lunatic is in my head The lunatic is in my head" +
                                    "  You raise the blade You make the change You rearrange me " +
                                    "'til I'm sane You lock the door And throw away the key There's" +
                                    " someone in my head, but it's not me"
                        )
                    }

                    item {
                        val url = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_full.jpg"
                        ChatPreview(
                            Modifier.sizeIn(maxWidth = 728.dp).fillMaxWidth(),
                            chatTitle = "Nick\uD83D\uDC3E",
                            iconLink = url,
                            lastMessage = "Mornin' <3"
                        )
                    }

                    item {
                        Column(Modifier.sizeIn(maxWidth = 728.dp).fillMaxWidth().padding(Dimensions.medium)) {

                            Messages(
                                iconLink = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_medium.jpg",
                                username = "raaveinm",
                                textMessage = "The lunatic is on the grass\n" +
                                        "Remembering games\n" +
                                        "And daisy chains and laughs\n" +
                                        "Got to keep the loonies on the path\n" +
                                        "The lunatic is in the hall\n" +
                                        "The lunatics are in my hall\n" +
                                        "The paper holds their folded faces to the floor \n" +
                                        "And every day the paper boy brings more",
                                timestamp = "11:12",
                                isSender = true,
                                modifier = Modifier,
                                previousExisted = false,
                                isLast = true
                            )

                            Messages(
                                iconLink = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_medium.jpg",
                                username = "raaveinm",
                                textMessage = "And if the dam breaks open many years too soon And " +
                                        "if there is no room upon the hill And if your head explodes " +
                                        "with dark forebodings too I'll see you on the dark side of the moon",
                                timestamp = "11:10",
                                isSender = false,
                                modifier = Modifier,
                                previousExisted = false,
                                isLast = false
                            )

                            Messages(
                                iconLink = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_medium.jpg",
                                username = "raaveinm",
                                textMessage = "The lunatic is in my head The lunatic is in my head " +
                                        "You raise the blade You make the change You rearrange me '" +
                                        "til I'm sane You lock the door And throw away the key There's " +
                                        "someone in my head, but it's not me",
                                timestamp = "11:12",
                                previousExisted = true,
                                isSender = false
                            )
                        }
                    }

                    item {
                        ChatTopBar(
                            modifier = Modifier.sizeIn(maxWidth = 512.dp).fillMaxWidth().padding(horizontal = 24.dp),
                            chatName = "Username",
                            chatIcon = null,
                            onGoBackAction = {},
                            onChatIconAction = {}
                        )
                    }

                    item {
                        UserQuickAction(
                            modifier = Modifier,
                            onMessageClick = {},
                            onAddToGroupClick = {},
                            onSteamProfileRedirectClick = {},
                        )
                    }

                    item {
                        UserMiniProfile(
                            modifier = Modifier.sizeIn(maxWidth = 512.dp),
                            iconLink = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_medium.jpg",
                            username = "Raaveinm",
                            isOnline = true,
                            status = UserStatus.onlineStatusList.random(),
                            onMessageClick = {},
                            onSteamProfileRedirectClick = {},
                        )
                    }

                    item {
                        UserMiniProfile(
                            modifier = Modifier.sizeIn(maxWidth = 512.dp),
                            iconLink = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_medium.jpg",
                            username = "Raaveinm",
                            isOnline = false,
                            status = UserStatus.offlineStatusList.random(),
                            onMessageClick = {},
                            onSteamProfileRedirectClick = {},
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                item {
                    Text(
                        "SCREEN PREVIEW",
                        fontSize = MaterialTheme.typography.labelLarge.fontSize,
                        fontWeight = MaterialTheme.typography.labelLarge.fontWeight,
                        fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            CanvasScreen(Modifier.fillMaxWidth().padding(horizontal = Dimensions.small))
        }
    }
}