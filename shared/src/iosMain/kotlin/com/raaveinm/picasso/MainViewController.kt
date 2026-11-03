package com.raaveinm.picasso

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raaveinm.core.database.DatabaseFactory
import com.raaveinm.core.database.databaseModule
import com.raaveinm.core.datastore.auth.AuthDataStoreFactory
import com.raaveinm.core.datastore.authDataStoreModule
import com.raaveinm.picasso.di.initKoin
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.navigation.SideWindowRequest
import org.koin.compose.viewmodel.koinViewModel

fun MainViewController() = run {
    initKoin {
        modules(
            databaseModule(DatabaseFactory()),
            authDataStoreModule(AuthDataStoreFactory())
        )
    }
    ComposeUIViewController(
        configure = { onFocusBehavior = OnFocusBehavior.DoNothing }
    ) {
        val appViewModel: AppViewModel = koinViewModel<AppViewModel>()
        val appUiState by appViewModel.uiState.collectAsState()
        val request: SideWindowRequest? = appUiState.sideWindowRequest

        App(appViewModel = appViewModel)
        if (request != null) {
            Dialog(
                onDismissRequest = { appViewModel.resetSideWindowRequest(request.id) },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = false,
                ),
            ) {
                AppSideWindow(appViewModel = appViewModel)
            }
        }
    }
}
