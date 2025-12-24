package com.example.gulf_coast_hazard_briefs_kmp.data.nws

import com.example.gulf_coast_hazard_briefs_kmp.data.http.createHttpClient
import com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto.NwsAlertsDto
import com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto.NwsForecastDto
import com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto.NwsPointDto
import com.example.gulf_coast_hazard_briefs_kmp.domain.AlertSummary
import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import io.ktor.client.call.body
import io.ktor.client.request.get

class NwsApiClient {

    private val client = createHttpClient()

    suspend fun getForecast(lat: Double, lon: Double): ForecastResult {
        val point: NwsPointDto =
            client.get("https://api.weather.gov/points/$lat,$lon").body()

        val forecast: NwsForecastDto =
            client.get(point.properties.forecast).body()

        val days: List<ForecastDay> =
            periodsToForecastDays(forecast.properties.periods)

        return ForecastResult(days = days)
    }

    suspend fun getAlerts(lat: Double, lon: Double): List<AlertSummary> {
        val url = "https://api.weather.gov/alerts/active?point=$lat,$lon"
        val dto: NwsAlertsDto = client.get(url).body()

        return dto.features.mapNotNull { f ->
            val p = f.properties ?: return@mapNotNull null
            val event = p.event?.trim().orEmpty()
            if (event.isBlank()) return@mapNotNull null

            AlertSummary(
                event = event,
                headline = p.headline,
                severity = p.severity,
                urgency = p.urgency,
                area = p.areaDesc,
                effective = p.effective,
                ends = p.ends ?: p.expires,
                description = p.description,
                instruction = p.instruction
            )
        }
    }

    data class ForecastResult(
        val days: List<ForecastDay>
    )
}