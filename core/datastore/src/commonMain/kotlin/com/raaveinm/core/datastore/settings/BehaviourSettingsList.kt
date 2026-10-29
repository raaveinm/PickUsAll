package com.raaveinm.core.datastore.settings

import com.raaveinm.core.model.preferences.OnCloseAction

//
// Created by Kirill "Raaveinm" on 10/29/2026.
//

data class BehaviourSettingsList(
    val onCloseRequest: OnCloseAction = OnCloseAction.TO_TRAY
)
