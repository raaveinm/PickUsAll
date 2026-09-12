package com.raaveinm.picasso.ui.app.viewmodel

import androidx.lifecycle.ViewModel
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppMessage(
    val level: WarnLevel,
    val text: String
)

data class AppUiState(
    val selectedTab: Int = 0,
    val isSideBarExpanded: Boolean = false,
    val message: AppMessage? = null
)

/**
 * Holds UI state shared across the whole app shell (bottom nav selection,
 * sidebar visibility, the global warning/error banner) rather than any single
 * screen. Other viewmodels/repositories can be pointed at [postMessage] as a
 * sink for cross-cutting failures (failed sends, sync errors, ...) once there's
 * a call site for that.
 */
class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSideBarExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isSideBarExpanded = expanded) }
    }

    fun toggleSideBar() {
        _uiState.update { it.copy(isSideBarExpanded = !it.isSideBarExpanded) }
    }

    fun postMessage(level: WarnLevel, text: String) {
        _uiState.update { it.copy(message = AppMessage(level, text)) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
