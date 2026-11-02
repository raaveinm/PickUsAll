package com.raaveinm.picasso

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.webview.AppWebView
import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.rememberAppWebViewState
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.webview_error
import pickusall.shared.generated.resources.webview_retry

private const val SAMPLE_PAGE_URL: String = "manjaro.org"

/**
 * used for side actions like accept / decline incoming call,
 * start a new chat from search
 *
 * Called from second undecorated application window
 */

@Composable
fun AppSideWindow() {
    var attempt: Int by remember { mutableStateOf(0) }
    key(attempt) {
        val state: AppWebViewState = rememberAppWebViewState(url = SAMPLE_PAGE_URL)
        MaterialTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = state.controller::goBack, enabled = state.canGoBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = state.controller::goForward, enabled = state.canGoForward) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(
                        onClick = {
                            if (state.isLoading) state.controller.stopLoading()
                            else state.controller.reload()
                        }
                    ) {
                        Icon(if (state.isLoading) Icons.Filled.Stop else Icons.Filled.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text(
                        text = state.title.ifBlank { state.url },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (state.isLoading) LinearProgressIndicator(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth())
                if (state.error != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(
                                Res.string.webview_error,
                                state.error?.description.orEmpty()
                            )
                        )
                        Button(onClick = { attempt += 1 }) {
                            Text(stringResource(Res.string.webview_retry))
                        }
                    }
                }
                AppWebView(
                    state = state,
                    modifier = Modifier.padding(top = 48.dp).fillMaxSize()
                )
            }
        }
    }
}
