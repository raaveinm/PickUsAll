package com.raaveinm.picasso

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.navigation.SideWindowRequest
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    private val requestRecordAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            /* denied - mic capture just fails silently for now, no in-flow UX built yet */
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Eager request so mic capture works once a call is started - see features:impl-webrtc.
        requestRecordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)

        setContent {
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
}
