package com.raaveinm.picasso

import androidx.compose.runtime.Composable
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import org.koin.compose.viewmodel.koinViewModel

//
// Created by Kirill "Raaveinm" on 10/10/26.
//


/**
 * used for side actions like accept / decline incoming call,
 * start a new chat from search
 *
 * Called from second undecorated application window
 */

@Composable
fun AppSideWindow() {
    val appViewModel = koinViewModel<AppViewModel>()

}