package com.example.gulf_coast_hazard_briefs_kmp.domain

data class AlertSummary(
    val event: String,
    val headline: String?,
    val severity: String?,
    val urgency: String?,
    val area: String?,
    val effective: String?,
    val ends: String?,
    val description: String?,
    val instruction: String?
)