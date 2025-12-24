package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.RiskLevel

object FloodingEngine {

    fun riskFrom(s: FloodingSignals): RiskLevel {
        val hasFloodAlert = s.floodAlertEvents.any { it.contains("flood", ignoreCase = true) }
        val maxQpf = s.qpfRangeInchesMax ?: 0.0
        val ero = s.eroLevel.coerceIn(0, 4)

        val gaugeRisk = s.riverGauges.any { g ->
            g.category == FloodCategory.MINOR ||
                    g.category == FloodCategory.MODERATE ||
                    g.category == FloodCategory.MAJOR
        }

        return when {
            hasFloodAlert || ero >= 3 || maxQpf >= 6.0 || gaugeRisk -> RiskLevel.HIGH
            ero == 2 || maxQpf >= 3.0 -> RiskLevel.MEDIUM
            ero == 1 || maxQpf >= 1.5 -> RiskLevel.LOW
            else -> RiskLevel.NONE
        }
    }

    fun headline(s: FloodingSignals, risk: RiskLevel): String {
        return when (risk) {
            RiskLevel.NONE -> "No flooding concerns expected"
            RiskLevel.LOW -> "Localized ponding possible in heavier showers"
            RiskLevel.MEDIUM -> "Localized flooding possible with heavier rain"
            RiskLevel.HIGH -> "Flooding likely in low-lying areas; monitor alerts closely"
        }
    }

    fun wpcSummary(s: FloodingSignals): String {
        val ero = s.eroLevel.coerceIn(0, 4)
        return when (ero) {
            0 -> "No excessive rainfall risk indicated"
            1 -> "Marginal excessive rainfall risk"
            2 -> "Slight excessive rainfall risk"
            3 -> "Moderate excessive rainfall risk"
            else -> "High excessive rainfall risk"
        }
    }

    fun rainfallSentence(s: FloodingSignals): String {
        val min = s.qpfRangeInchesMin
        val max = s.qpfRangeInchesMax
        val seven = s.sevenDayTotalInches

        return when {
            min != null && max != null ->
                "Forecast rainfall range: ${min}–${max} in (1–3 days)"
            seven != null ->
                "7-day rainfall total may reach ~${seven} in"
            else ->
                "Rain totals uncertain; monitor updates"
        }
    }

    fun riverLines(s: FloodingSignals): List<String> {
        if (s.riverGauges.isEmpty()) return emptyList()

        return s.riverGauges.map { g ->
            val stage = g.stageFt?.let { "${it} ft" } ?: "stage unknown"
            val cat = when (g.category) {
                FloodCategory.NORMAL -> "Normal"
                FloodCategory.ACTION -> "Action"
                FloodCategory.MINOR -> "Minor"
                FloodCategory.MODERATE -> "Moderate"
                FloodCategory.MAJOR -> "Major"
            }
            val trend = when (g.trend) {
                RiverTrend.RISING -> "Rising"
                RiverTrend.FALLING -> "Falling"
                RiverTrend.STEADY -> "Steady"
                RiverTrend.UNKNOWN -> "Unknown trend"
            }
            "${g.name}: $cat ($stage), $trend"
        }
    }

    fun floodAlertText(s: FloodingSignals): String {
        val events = s.floodAlertEvents.distinct()
        return if (events.isEmpty()) "None" else events.joinToString(", ")
    }

    fun impacts(s: FloodingSignals, risk: RiskLevel): List<String> {
        val out = mutableListOf<String>()
        if (risk == RiskLevel.NONE) return out

        val maxQpf = s.qpfRangeInchesMax ?: 0.0
        if (maxQpf >= 3.0) out += "Reduced visibility and ponding on roads"
        if (maxQpf >= 6.0) out += "Localized road flooding likely in low-lying areas"

        val gaugeFlag = s.riverGauges.any { it.category >= FloodCategory.MINOR }
        if (gaugeFlag) out += "Rivers/streams may rise quickly; monitor gauge trends"

        out += "Avoid flooded roads; turn around, don’t drown"
        return out.distinct().take(5)
    }

    fun confidenceFrom(s: FloodingSignals): ConfidenceLevel {
        // only MEDIUM/HIGH (per your rule)
        val highSignal =
            s.eroLevel >= 2 ||
                    (s.qpfRangeInchesMax ?: 0.0) >= 3.0 ||
                    s.floodAlertEvents.isNotEmpty() ||
                    s.riverGauges.any { it.category >= FloodCategory.MINOR }

        return if (highSignal) ConfidenceLevel.HIGH else ConfidenceLevel.MEDIUM
    }
}