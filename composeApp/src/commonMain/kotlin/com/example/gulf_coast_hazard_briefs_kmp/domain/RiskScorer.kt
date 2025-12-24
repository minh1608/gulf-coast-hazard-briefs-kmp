package com.example.gulf_coast_hazard_briefs_kmp.domain

object RiskScorer {

    fun score(alerts: List<AlertSummary>): RiskLevel {
        if (alerts.isEmpty()) return RiskLevel.NONE

        // Highest risk wins
        return alerts
            .map { scoreOne(it) }
            .maxBy { it.ordinal }
    }

    private fun scoreOne(a: AlertSummary): RiskLevel {
        val text = (a.event + " " + (a.headline ?: "")).lowercase()

        // Primary mapping by keywords + severity-like intent
        val isWarning = text.contains("warning") || a.severity.equals("Severe", true) || a.severity.equals("Extreme", true)
        val isWatch = text.contains("watch") || a.severity.equals("Moderate", true)
        val isAdvisory = text.contains("advisory") || a.severity.equals("Minor", true)

        return when {
            isWarning -> RiskLevel.HIGH
            isWatch -> RiskLevel.MEDIUM
            isAdvisory -> RiskLevel.LOW
            else -> RiskLevel.LOW // unknown active alert -> still not NONE
        }
    }
}