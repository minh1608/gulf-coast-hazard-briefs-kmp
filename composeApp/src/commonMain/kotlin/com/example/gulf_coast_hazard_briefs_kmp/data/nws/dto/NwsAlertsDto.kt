package com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NwsAlertsDto(
    val features: List<Feature> = emptyList()
) {
    @Serializable
    data class Feature(
        val properties: Properties? = null
    )

    @Serializable
    data class Properties(
        val event: String? = null,
        val headline: String? = null,
        val severity: String? = null,   // "Minor" "Moderate" "Severe" "Extreme"
        val urgency: String? = null,    // "Immediate" "Expected" ...
        val certainty: String? = null,  // "Observed" "Likely" ...
        val effective: String? = null,
        val ends: String? = null,
        val expires: String? = null,
        val areaDesc: String? = null,
        val description: String? = null,
        val instruction: String? = null
    )
}