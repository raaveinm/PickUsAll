package com.raaveinm.picasso.ui.navigation

import com.raaveinm.picasso.data.auth.secureNonce

//
// Created by Kirill "Raaveinm" on 11/03/2026.
//

/**
 * One presentation of [destination].
 *
 * [id] identifies this opening, independently of its URL or call ID. Completion callbacks
 * must carry this ID so an older screen cannot dismiss its replacement.
 */
data class SideWindowRequest(
    val destination: SideWindow,      // Screen and its required parameters.
    val id: String = secureNonce(),   // Fresh presentation identity, including when reopening the same screen.
)
