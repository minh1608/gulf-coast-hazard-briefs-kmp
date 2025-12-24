package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.RiskLevel
import kotlinx.datetime.Instant

data class Page3Flooding(
    override val riskLevel: RiskLevel,
    val dateRange: String,

    // SECTION 1
    val floodHeadline: String,

    // SECTION 2
    val wpcExcessiveRainSummary: String,
    val rainfallPotential: String,

    // SECTION 3
    val riverLines: List<String>,
    val floodAlertText: String,

    // SECTION 4
    val impacts: List<String>,

    // SECTION 5 (Option A + B)
    // Option A: page URLs (safe default)
    val wpcQpfPageUrl: String? = null,
    val ahpsPageUrl: String? = null,
    val nwsRainPageUrl: String? = null,

    // Option B: direct image URLs (only if you have real static image links)
    val wpcQpfImageUrl: String? = null,
    val ahpsImageUrl: String? = null,
    val nwsRainImageUrl: String? = null,

    // SECTION 6/7
    val confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM,
    val sources: Set<DataSource> = emptySet(),
    val generatedAt: Instant? = null
) : BriefPage(), RiskTagged {

    override val pageNumber: Int = 3
    override val title: String = "Flooding (Rivers, Lakes, Excessive Rain)"
    override fun renderBody(): String {
        val lines = mutableListOf<String>()

        // SECTION 1
        lines += "HEADLINE"
        lines += floodHeadline
        lines += ""

        // SECTION 2
        lines += "FLASH FLOOD / EXCESSIVE RAIN RISK"
        lines += "WPC Excessive Rainfall Outlook: $wpcExcessiveRainSummary"
        lines += "Rainfall Potential: $rainfallPotential"
        lines += ""

        // SECTION 3
        lines += "RIVER & LAKE FLOODING STATUS"
        if (riverLines.isNotEmpty()) {
            lines += "River Levels:"
            riverLines.forEach { lines += "• $it" }
        } else {
            lines += "River Levels: No gauge concerns available."
        }
        lines += "Flood Alerts: $floodAlertText"
        lines += ""

        // SECTION 4
        lines += "LOCAL FLOODING IMPACTS"
        if (impacts.isNotEmpty()) {
            impacts.forEach { lines += "• $it" }
        } else {
            lines += "• No flooding impacts indicated."
        }
        lines += ""

        // SECTION 5 (maps)
        lines += "FORECAST MAPS"
        lines += "WPC QPF page: ${wpcQpfPageUrl ?: "None"}"
        lines += "WPC QPF image: ${wpcQpfImageUrl ?: "None"}"
        lines += "AHPS page: ${ahpsPageUrl ?: "None"}"
        lines += "AHPS image: ${ahpsImageUrl ?: "None"}"
        lines += "NWS rainfall page: ${nwsRainPageUrl ?: "None"}"
        lines += "NWS rainfall image: ${nwsRainImageUrl ?: "None"}"
        lines += ""

        // SECTION 6/7
        lines += "Forecast Confidence: ${confidence.label}"
        if (sources.isNotEmpty()) {
            lines += "Sources: " + sources.joinToString { it.label }
        }
        generatedAt?.let { lines += "Generated: $it" }

        return lines.joinToString("\n")
    }
}