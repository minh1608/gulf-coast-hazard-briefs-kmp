package com.example.gulf_coast_hazard_briefs_kmp.data.http

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

internal fun HttpClientConfig<*>.installCommonPlugins() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }

    defaultRequest {
        header(HttpHeaders.UserAgent, "GulfCoastHazardBriefsKMP/1.0 (contact: you@example.com)")
        header(HttpHeaders.Accept, "application/geo+json,application/json")
    }

    install(HttpTimeout) {
        connectTimeoutMillis = 15.seconds.inWholeMilliseconds
        socketTimeoutMillis = 30.seconds.inWholeMilliseconds
        requestTimeoutMillis = 30.seconds.inWholeMilliseconds
    }

    install(HttpRequestRetry) {
        retryOnExceptionOrServerErrors(maxRetries = 2)
        exponentialDelay()
    }

    install(Logging) {
        level = LogLevel.INFO
    }
}