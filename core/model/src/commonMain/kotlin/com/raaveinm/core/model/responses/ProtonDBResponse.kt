package com.raaveinm.core.model.responses

import com.raaveinm.core.model.game.ProtonDB
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 11/09/2026.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

/**
 * ProtonDB's community compatibility summary for one Steam app. The overall and recent medals
 * are separate ratings, and the numeric score can be fractional.
 *
 * @sample "https://www.protondb.com/data/reports/newSummaries/1808500.json"
 */
@Serializable
data class ProtonDBResponse(
    val bestReportedTier: ProtonDB.CompatibilityTier? = null,   // highest rank
    val confidence: ProtonDB.Confidence? = null,                // Confidence label assigned by ProtonDB
    val score: Float? = null,                                   // float compatibility score
    val tier: ProtonDB.CompatibilityTier,                       // compatibility medal
    val total: Int,                                             // amount of reports
    val trendingTier: ProtonDB.CompatibilityTier? = null,       // Compatibility medal based on recent reports.
    val numberTiers: ProtonDB.NumberTiers? = null,              // Additional ratings when supplied.
)
