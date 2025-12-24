package com.example.gulf_coast_hazard_briefs_kmp.domain

import kotlinx.datetime.LocalDate

data class ForecastDay(
    val date: LocalDate,
    val high: Int? = null,
    val low: Int? = null,
    val pop: Int? = null,
    val forecast: String? = null,
    val hazard: HazardLevel = HazardLevel.NONE
)