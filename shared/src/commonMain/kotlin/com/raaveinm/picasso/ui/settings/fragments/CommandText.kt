package com.raaveinm.picasso.ui.settings.fragments

import androidx.compose.runtime.Composable
import com.raaveinm.pickusall.core.designsystem.keybinding.Commands
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.command_minimize_application_desc
import pickusall.shared.generated.resources.command_minimize_application_title
import pickusall.shared.generated.resources.command_quit_application_desc
import pickusall.shared.generated.resources.command_quit_application_title
import pickusall.shared.generated.resources.command_refresh_desc
import pickusall.shared.generated.resources.command_refresh_title

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

private fun Commands.titleRes(): StringResource = when (this) {
    Commands.REFRESH -> Res.string.command_refresh_title
    Commands.QUIT_APPLICATION -> Res.string.command_quit_application_title
    Commands.MINIMIZE_APPLICATION -> Res.string.command_minimize_application_title
}

private fun Commands.descriptionRes(): StringResource = when (this) {
    Commands.REFRESH -> Res.string.command_refresh_desc
    Commands.QUIT_APPLICATION -> Res.string.command_quit_application_desc
    Commands.MINIMIZE_APPLICATION -> Res.string.command_minimize_application_desc
}

@Composable
fun Commands.titleText(): String = stringResource(titleRes())

@Composable
fun Commands.descriptionText(): String = stringResource(descriptionRes())
