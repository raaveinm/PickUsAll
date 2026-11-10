package com.raaveinm.core.model.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 11/09/2026.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

object ProtonDB { // The overall compatibility medal
    @Serializable
    enum class CompatibilityTier {
        @SerialName("platinum") PLATINUM,
        @SerialName("gold") GOLD,
        @SerialName("silver") SILVER,
        @SerialName("bronze") BRONZE,
        @SerialName("borked") BORKED,
        @SerialName("pending") PENDING,
    }

    @Serializable // ProtonDB's confidence in the summary
    enum class Confidence {
        @SerialName("strong") STRONG,
        @SerialName("good") GOOD,
        @SerialName("moderate") MODERATE,
        @SerialName("low") LOW,
    }

    @Serializable // Compatibility tier
    enum class Tier {
        T1,
        T2,
        T3,
        T4,
        T5,
        @SerialName("?") NULL,
    }

    @Serializable
    data class NumberTiers(
        val clickPlay: Tier? = null,                // Compatibility without tinkering
        val medals: CompatibilityTier? = null,      // readable compatibility rating
        val native: Tier? = null,                   // is native for Linux ("T1" - y, "?" - N)
        val tinker: Tier? = null,                   // tinkering level
        val deck: Tier? = null,                     // steam deck compatibility rating
    )
}
