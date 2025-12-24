package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import kotlinx.datetime.Instant
import kotlin.math.abs

data class Page1Overview(
    val keyMessages: List<String>,
    val northDays: List<ForecastDay>,
    val southDays: List<ForecastDay>,

    val sources: Set<DataSource> = emptySet(),
    val confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM, // ✅ comma was missing here
    val generatedAt: Instant? = null
) : BriefPage() {

    override val pageNumber: Int = 1
    override val title: String = "Weekly Overview"

    override fun renderBody(): String {
        fun maxPop(days: List<ForecastDay>) = days.mapNotNull { it.pop }.maxOrNull() ?: 0
        fun maxHigh(days: List<ForecastDay>) = days.mapNotNull { it.high }.maxOrNull()
        fun minLow(days: List<ForecastDay>) = days.mapNotNull { it.low }.minOrNull()

        val nPop = maxPop(northDays)
        val sPop = maxPop(southDays)

        val nHigh = maxHigh(northDays)
        val sHigh = maxHigh(southDays)

        val nLow = minLow(northDays)
        val sLow = minLow(southDays)

        val lines = mutableListOf<String>()
        lines += "North peak POP: ${nPop}% | South peak POP: ${sPop}%"
        if (abs(nPop - sPop) >= 25) lines += "Rain chance differs by about ${abs(nPop - sPop)}% (north vs south)."

        if (nHigh != null && sHigh != null) lines += "Highs: north ${nHigh}° vs south ${sHigh}° (gap ~${abs(nHigh - sHigh)}°)."
        if (nLow != null && sLow != null) lines += "Lows: north ${nLow}° vs south ${sLow}° (gap ~${abs(nLow - sLow)}°)."

        return lines.joinToString("\n")
    }
}