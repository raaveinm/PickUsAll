package com.raaveinm.core.database.entities.server

import com.raaveinm.core.model.ServerState

fun Servers.toModel(reachable: Boolean? = null): ServerState = ServerState(
    id = id,
    url = url,
    name = name,
    addedAt = added,
    reachable = reachable,
)
