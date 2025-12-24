package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.AlertSummary
import com.example.gulf_coast_hazard_briefs_kmp.domain.RiskLevel
import kotlinx.datetime.Instant

data class Page2Convective(
    override val pageNumber: Int = 2,
    override val title: String = "Convective Weather (Storms & Heavy Rain)",
    override val riskLevel: RiskLevel,

    val timing: String,
    val affectedRegions: String,
    val actions: List<String>,

    // ✅ FIX: use AlertSummary (real type in your project)
    val topAlerts: List<AlertSummary> = emptyList(),

    // ✅ NEW
    val stormType: String = "Storms",
    val impacts: List<String> = emptyList(),
    val headline: String = "",
    val generatedAt: Instant? = null,

    // ✅ if you already added these fields, keep them here too
    val sources: Set<DataSource> = emptySet(),
    val confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM
) : BriefPage(), RiskTagged {

    override fun renderBody(): String {
        val lines = mutableListOf<String>()
        lines += "Risk: ${riskLevel.name}"
        lines += "Timing: $timing"
        lines += "Areas: $affectedRegions"
        lines += ""
        lines += "Actions:"
        actions.forEach { lines += "• $it" }

        if (topAlerts.isNotEmpty()) {
            lines += ""
            lines += if (topAlerts.size > 1) "Top alerts:" else "Top alert:"
            topAlerts.take(3).forEach { a ->
                // AlertSummary has event + then headline/title depending on your model
                val head = a.headline?.takeIf { it.isNotBlank() }
                lines += "• ${a.event}${head?.let { " — $it" } ?: ""}"
            }
        }
        return lines.joinToString("\n")
    }
}