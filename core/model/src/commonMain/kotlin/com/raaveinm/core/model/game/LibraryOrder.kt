package com.raaveinm.core.model.game

//
// Created by Kirill "Raaveinm" on 9/1/26.
//

import kotlinx.serialization.SerialName

enum class LibraryOrder {
    @SerialName("name")
    NAME,
    @SerialName("playtimeForever")
    PLAYTIME,
    @SerialName("rTimeLastPlayed")
    LAST_PLAYED
}