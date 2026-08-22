package com.raaveinm.picasso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raaveinm.picasso.ui.canvas.CanvasScreen
import com.raaveinm.picasso.ui.canvas.viewmodel.CanvasViewModel
import com.raaveinm.picasso.ui.chat.ChatScreen
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.navigation.Canvas
import com.raaveinm.picasso.ui.navigation.ChatList
import com.raaveinm.pickusall.core.designsystem.components.NavBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer

@Composable
@Preview
fun App(
    navController: NavHostController = rememberNavController()
) {
    val canvasViewModel = CanvasViewModel()
    val chatViewModel = ChatViewModel()

    PicassoTheme {
        CoilInitializer()

        var navigationSelected by remember { mutableStateOf(0) }

        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.inverseOnSurface
            )
        )

        Box {
            NavHost(
                navController = navController,
                modifier = Modifier.fillMaxSize().background(gradientBrush).systemBarsPadding(),
                contentAlignment = Alignment.Center,
                startDestination = Canvas
            ) {
                composable<Canvas> {
                    CanvasScreen(
                        modifier = Modifier,
                        canvasViewModel
                    )
                }
                composable<ChatList> {
                    ChatScreen(
                        modifier = Modifier.safeContentPadding(),
                        viewModel = chatViewModel,
                        onGroupClick = { },
                        nestedNavHostController = rememberNavController()
                    )
                }
            }

            NavBar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Dimensions.medium),
                nestedModifier = Modifier,
                fabModifier = Modifier,
                selectedId = navigationSelected,
                onItemClick = {
                    val screen = when (it) {
                        0 -> Canvas
                        1 -> ChatList
                        else -> Canvas
                    }
                    navController.navigate(screen)
                    navigationSelected = it
                },
            )
        }
    }
}
