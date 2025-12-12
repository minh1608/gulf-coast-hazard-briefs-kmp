package com.example.gulf_coast_hazard_briefs_kmp.data.nws

import com.example.gulf_coast_hazard_briefs_kmp.data.http.createHttpClient
import com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto.NwsForecastDto
import com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto.NwsPointDto
import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import io.ktor.client.call.body
import io.ktor.client.request.get

class NwsApiClient {
    private val client = createHttpClient()

    suspend fun getForecast(lat: Double, lon: Double): ForecastResult {
        val pointUrl = "https://api.weather.gov/points/$lat,$lon"
        val point: NwsPointDto = client.get(pointUrl).body()

        val locationName = buildString {
            val city = point.properties.relativeLocation?.properties?.city
            val state = point.properties.relativeLocation?.properties?.state
            if (!city.isNullOrBlank()) append(city)
            if (!state.isNullOrBlank()) append(if (isNotEmpty()) ", $state" else state)
            if (isEmpty()) append("($lat,$lon)")
        }

        val forecastUrl = point.properties.forecast
        val forecast: NwsForecastDto = client.get(forecastUrl).body()

        return ForecastResult(
            locationName = locationName,
            days = periodsToForecastDays(forecast.properties.periods)
        )
    }

    data class ForecastResult(
        val locationName: String,
        val days: List<ForecastDay>
    )
}