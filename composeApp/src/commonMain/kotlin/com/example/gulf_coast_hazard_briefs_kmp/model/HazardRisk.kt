package com.example.gulf_coast_hazard_briefs_kmp.model

import kotlinx.datetime.Instant

/**
 * Unified hazard object that all data feeds map into.
 */
data class HazardRisk(
    val type: HazardType,
    val headline: String,
    val description: String,
    val severity: Int,          // 0–4 (None, Minor, Moderate, Major, Extreme)
    val confidence: Int,        // 0–100 (%)
    val startTime: Instant?,
    val endTime: Instant?,
    val source: String,         // e.g. "NWS", "SPC", "NHC"
    val sourceUrl: String? = null
)