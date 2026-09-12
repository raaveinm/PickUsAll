package com.raaveinm.core.model

//
// Created by Kirill "Raaveinm" on 9/12/26.
//
data class ServerState(
    val id: Long = 0,
    val url: String,
    val name: String? = null,
    val addedAt: Long,
    val reachable: Boolean? = false,
)
