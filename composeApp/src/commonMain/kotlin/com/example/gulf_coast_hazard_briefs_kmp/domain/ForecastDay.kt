package com.example.gulf_coast_hazard_briefs_kmp.domain

import kotlinx.datetime.LocalDate

data class ForecastDay(
    val date: LocalDate,
    val label: String,              // e.g., "Mon"
    val highF: Int? = null,
    val lowF: Int? = null,
    val shortForecast: String? = null,
    val wind: String? = null
)