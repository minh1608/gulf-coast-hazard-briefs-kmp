package com.example.gulf_coast_hazard_briefs_kmp.data.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class WpcClientLite(private val http: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchEroDay1JsonOrNull(): JsonObject? {
        val url = "https://www.wpc.ncep.noaa.gov/wwd/day1_ero.json"

        val resp: HttpResponse = http.get(url)

        // nếu 404/500 thì khỏi parse
        if (!resp.status.isSuccess()) return null

        val text = resp.bodyAsText()
        return runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull()
    }
}