package com.example.gulf_coast_hazard_briefs_kmp.domain.usecases

import com.example.gulf_coast_hazard_briefs_kmp.data.nws.NwsApiClient

class FetchNwsDebug(
    private val client: NwsApiClient = NwsApiClient()
) {
    suspend fun execute(lat: Double, lon: Double): NwsApiClient.ForecastResult {
        return client.getForecast(lat, lon)
    }
}