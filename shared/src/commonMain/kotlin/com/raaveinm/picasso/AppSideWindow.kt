package com.raaveinm.picasso

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.webview.AppWebView
import com.raaveinm.webview.wv.AppWebViewState
import com.raaveinm.webview.wv.rememberAppWebViewState
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.webview_back
import pickusall.shared.generated.resources.webview_error
import pickusall.shared.generated.resources.webview_forward
import pickusall.shared.generated.resources.webview_reload
import pickusall.shared.generated.resources.webview_retry
import pickusall.shared.generated.resources.webview_stop

private const val SAMPLE_PAGE_URL: String = "https://pickusall.raaveinm.space"

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
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = state.controller::goBack, enabled = state.canGoBack) {
                            Text(stringResource(Res.string.webview_back))
                        }
                        TextButton(onClick = state.controller::goForward, enabled = state.canGoForward) {
                            Text(stringResource(Res.string.webview_forward))
                        }
                        TextButton(
                            onClick = {
                                if (state.isLoading) state.controller.stopLoading()
                                else state.controller.reload()
                            },
                        ) {
                            Text(stringResource(if (state.isLoading) Res.string.webview_stop else Res.string.webview_reload))
                        }
                    }
                    Text(
                        text = state.title.ifBlank { state.url },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        maxLines = 1,
                    )
                    if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    if (state.error != null) {
                        Text(
                            text = stringResource(Res.string.webview_error, state.error?.description.orEmpty()),
                            modifier = Modifier.padding(16.dp),
                        )
                        Button(onClick = { attempt += 1 }) {
                            Text(stringResource(Res.string.webview_retry))
                        }
                    }
                    AppWebView(
                        state = state,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                }
            }
        }
    }
}
