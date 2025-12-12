package com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto

import kotlinx.serialization.Serializable

@Serializable
data class NwsForecastDto(
    val properties: Properties
) {
    @Serializable
    data class Properties(
        val periods: List<Period>
    )

    @Serializable
    data class Period(
        val name: String? = null,
        val startTime: String? = null,
        val temperature: Int? = null,
        val temperatureUnit: String? = null,
        val windSpeed: String? = null,
        val windDirection: String? = null,
        val shortForecast: String? = null,
        val probabilityOfPrecipitation: ProbabilityOfPrecipitation? = null
    )

    @Serializable
    data class ProbabilityOfPrecipitation(
        val value: Int? = null
    )
}