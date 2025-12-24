package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.data.api.WpcClientLite
import com.example.gulf_coast_hazard_briefs_kmp.data.http.createHttpClient
import com.example.gulf_coast_hazard_briefs_kmp.data.nws.NwsApiClient
import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import com.example.gulf_coast_hazard_briefs_kmp.domain.HazardLevel
import com.example.gulf_coast_hazard_briefs_kmp.domain.HazardRules
import com.example.gulf_coast_hazard_briefs_kmp.domain.RiskLevel
import com.example.gulf_coast_hazard_briefs_kmp.domain.RiskScorer
import kotlinx.datetime.Clock
import io.ktor.client.HttpClient

class BriefBuilder(
    private val forcePage2ForTest: Boolean = true,
    private val forcePage3ForTest: Boolean = true,
    private val nws: NwsApiClient = NwsApiClient(),
    private val httpClient: HttpClient = createHttpClient() // ✅ FIX
) {

    suspend fun buildBrief(
        northLat: Double, northLon: Double,
        southLat: Double, southLon: Double
    ): List<BriefPage> {

        // ✅ WPC Lite test (safe)
        val wpc = WpcClientLite(httpClient)
        val ero = wpc.fetchEroDay1JsonOrNull()
        println("WPC ERO keys: ${ero?.keys}")

        // --------- the rest of your existing code stays the same ----------
        val sources = mutableSetOf<DataSource>()
        val now = Clock.System.now()

        // ---- Forecast (Page 1) ----
        val northDays = HazardRules.apply(nws.getForecast(northLat, northLon).days)
        val southDays = HazardRules.apply(nws.getForecast(southLat, southLon).days)
        sources += DataSource.NWS_FORECAST

        val keyMessages = KeyMessageEngine.build(northDays, southDays)

        val popPeakValue = peakPopValue(northDays, southDays)
        val page1Confidence =
            if (popPeakValue >= 50) ConfidenceLevel.HIGH else ConfidenceLevel.MEDIUM

        val pages = mutableListOf<BriefPage>()
        pages += Page1Overview(
            keyMessages = keyMessages,
            northDays = northDays,
            southDays = southDays,
            sources = sources.toSet(),
            confidence = page1Confidence,
            generatedAt = now
        )

        // ---- Alerts (Page 2) ----
        val allAlerts =
            (nws.getAlerts(northLat, northLon) + nws.getAlerts(southLat, southLon))
                .distinctBy { (it.event.trim() + "|" + (it.headline ?: "").trim()).lowercase() }

        if (allAlerts.isNotEmpty()) sources += DataSource.NWS_ALERTS

        val risk = RiskScorer.score(allAlerts)
        val shouldAddPage2 = forcePage2ForTest || risk != RiskLevel.NONE

        if (shouldAddPage2) {
            val forcedRisk =
                if (forcePage2ForTest && risk == RiskLevel.NONE) RiskLevel.MEDIUM else risk

            val a0 = allAlerts.firstOrNull()
            val effective = a0?.effective?.takeIf { it.isNotBlank() } ?: "now"
            val ends = a0?.ends?.takeIf { it.isNotBlank() } ?: "unknown"
            val timing = "$effective → $ends"
            val affectedRegions = a0?.area?.takeIf { it.isNotBlank() } ?: "multiple areas"

            val actions = listOf(
                "Check official NWS alert details.",
                "Be ready for lightning, downpours, and sudden wind gusts.",
                "Avoid flooded roads; turn around, don’t drown."
            )

            val topAlerts = allAlerts.take(3)

            val alertText = buildString {
                topAlerts.forEach { a ->
                    append(a.event).append(" ")
                    if (!a.headline.isNullOrBlank()) append(a.headline).append(" ")
                }
                actions.forEach { append(it).append(" ") }
            }

            val stormType = classifyStormType(alertText)
            val impacts = impactsFrom(alertText)

            val popPeakDay = peakPopDay(northDays, southDays)
            val hazPeakDay = peakHazardDay(northDays, southDays)
            val headline = headlineTemplate(stormType, forcedRisk, popPeakDay, hazPeakDay)

            val confidence2 = confidenceFrom(
                risk = forcedRisk,
                alertText = alertText,
                popPeak = popPeakValue
            )

            pages += Page2Convective(
                riskLevel = forcedRisk,
                timing = timing,
                affectedRegions = affectedRegions,
                actions = actions,
                topAlerts = topAlerts,
                stormType = stormType,
                impacts = impacts,
                headline = headline,
                generatedAt = now,
                sources = sources.toSet(),
                confidence = confidence2
            )
        }

        // ---- Flooding (Page 3) ----
        val floodingSignals = FloodingSignals(
            eroLevel = 0, // TODO: real WPC ERO later
            qpfRangeInchesMin = null,
            qpfRangeInchesMax = null,
            sevenDayTotalInches = null,
            riverGauges = emptyList(),
            floodAlertEvents = allAlerts
                .map { it.event.trim() }
                .filter { e ->
                    val t = e.lowercase()
                    "flash flood" in t || "flood" in t
                }
                .distinct()
        )

        val floodRisk = FloodingEngine.riskFrom(floodingSignals)
        val floodShouldShow = forcePage3ForTest || floodRisk != RiskLevel.NONE

        val effectiveFloodRisk =
            if (forcePage3ForTest && floodRisk == RiskLevel.NONE) RiskLevel.MEDIUM else floodRisk

        val isForcedFloodPage = forcePage3ForTest && floodRisk == RiskLevel.NONE

        // Option A: page URLs (safe default)
        val wpcQpfPageUrl = "https://www.wpc.ncep.noaa.gov/qpf/day1-3.shtml"
        val radarLoopPageUrl = "https://radar.weather.gov/ridge/standard/KHGX_loop.gif"
        val graphicalWxPageUrl = "https://graphical.weather.gov/sectors/southplains.php?element=Wx"

        // Option B: direct image URLs
        val wpcQpfImageUrl: String? = "https://www.wpc.ncep.noaa.gov/qpf/d13_fill.gif"
        val radarLoopImageUrl: String? = "https://radar.weather.gov/ridge/standard/KHGX_loop.gif"
        val graphicalWxImageUrl: String? = null // HTML page, not a direct image

        if (floodShouldShow) {
            val floodSources = sources.toMutableSet()
            floodSources += DataSource.WPC
            floodSources += DataSource.AHPS // keep label for now (river source bucket)

            val floodHeadline =
                if (isForcedFloodPage) {
                    "Test Mode: Flooding page shown for layout review (no active flood signals detected)."
                } else {
                    FloodingEngine.headline(floodingSignals, floodRisk)
                }

            val wpcSummary =
                if (isForcedFloodPage) "No excessive rainfall risk indicated (test mode)."
                else FloodingEngine.wpcSummary(floodingSignals)

            val rainSentence =
                if (isForcedFloodPage) "Rain totals uncertain; monitor updates."
                else FloodingEngine.rainfallSentence(floodingSignals)

            val rivers = FloodingEngine.riverLines(floodingSignals)
            val floodAlerts = FloodingEngine.floodAlertText(floodingSignals)
            val floodImpacts = FloodingEngine.impacts(floodingSignals, floodRisk)

            val floodConfidence = FloodingEngine.confidenceFrom(floodingSignals)

            pages += Page3Flooding(
                riskLevel = effectiveFloodRisk,
                dateRange = "Next 7 Days",
                floodHeadline = floodHeadline,
                wpcExcessiveRainSummary = wpcSummary,
                rainfallPotential = rainSentence,
                riverLines = rivers,
                floodAlertText = floodAlerts,
                impacts = floodImpacts,

                // Map 1
                wpcQpfPageUrl = wpcQpfPageUrl,
                wpcQpfImageUrl = wpcQpfImageUrl,

                // Map 2 (using AHPS slots)
                ahpsPageUrl = radarLoopPageUrl,
                ahpsImageUrl = radarLoopImageUrl,

                // Map 3 (using NWS Rain slots)
                nwsRainPageUrl = graphicalWxPageUrl,
                nwsRainImageUrl = graphicalWxImageUrl,

                confidence = floodConfidence,
                sources = floodSources.toSet(),
                generatedAt = now
            )
        }

        return pages
    }
}

// ---------------- helpers ----------------

private fun classifyStormType(text: String): String {
    val t = text.lowercase()
    return when {
        "tornado" in t -> "Tornado risk"
        ("severe" in t && ("thunder" in t || "storm" in t)) -> "Severe thunderstorms"
        (("thunder" in t || "storm" in t) && ("hail" in t || "damaging wind" in t || "gust" in t)) -> "Strong storms"
        ("thunder" in t || "storm" in t) -> "Thunderstorms"
        ("heavy rain" in t || "downpour" in t || "soaking rain" in t) -> "Heavy rain"
        ("flash flood" in t || "flood" in t) -> "Flooding concern"
        else -> "Storms"
    }
}

private fun impactsFrom(text: String): List<String> {
    val t = text.lowercase()
    val out = mutableListOf<String>()
    fun addIf(cond: Boolean, msg: String) { if (cond) out += msg }

    addIf(("flash flood" in t) || ("flood" in t), "Localized flooding possible")
    addIf(("heavy rain" in t) || ("downpour" in t), "Reduced visibility and ponding on roads")
    addIf("hail" in t, "Hail may damage vehicles and roofs")
    addIf(("damaging wind" in t) || ("gust" in t) || ("strong wind" in t), "Downed trees and power outages possible")
    addIf("tornado" in t, "Tornadoes possible; review shelter plan")
    addIf("lightning" in t, "Lightning hazard for outdoor activities")

    return out.distinct().take(5)
}

private fun peakPopValue(north: List<ForecastDay>, south: List<ForecastDay>): Int {
    val n = minOf(7, north.size, south.size)
    if (n <= 0) return 0
    return (0 until n).maxOfOrNull { i -> maxOf(north[i].pop ?: 0, south[i].pop ?: 0) } ?: 0
}

private fun peakPopDay(north: List<ForecastDay>, south: List<ForecastDay>): String {
    val n = minOf(7, north.size, south.size)
    if (n <= 0) return ""
    val idx = (0 until n).maxByOrNull { i -> maxOf(north[i].pop ?: 0, south[i].pop ?: 0) } ?: 0
    return north.getOrNull(idx)?.date?.dayOfWeek?.name?.take(3) ?: ""
}

private fun peakHazardDay(north: List<ForecastDay>, south: List<ForecastDay>): String {
    fun score(h: HazardLevel) = when (h) {
        HazardLevel.NONE -> 0
        HazardLevel.LOW -> 1
        HazardLevel.MODERATE -> 2
        HazardLevel.HIGH -> 3
    }
    val n = minOf(7, north.size, south.size)
    if (n <= 0) return ""
    val idx = (0 until n).maxByOrNull { i -> maxOf(score(north[i].hazard), score(south[i].hazard)) } ?: 0
    return north.getOrNull(idx)?.date?.dayOfWeek?.name?.take(3) ?: ""
}

private fun headlineTemplate(
    stormType: String,
    risk: RiskLevel,
    peakPopDay: String,
    peakHazardDay: String
): String {
    val riskWord = risk.name.lowercase().replaceFirstChar { it.uppercase() }
    return when {
        peakPopDay.isNotBlank() && peakHazardDay.isNotBlank() && peakPopDay != peakHazardDay ->
            "$stormType possible with $riskWord risk; highest rain chances $peakPopDay and peak hazard $peakHazardDay."
        peakPopDay.isNotBlank() ->
            "$stormType possible with $riskWord risk; highest rain chances $peakPopDay."
        else ->
            "$stormType possible with $riskWord risk this week."
    }
}

private fun confidenceFrom(
    risk: RiskLevel,
    alertText: String,
    popPeak: Int
): ConfidenceLevel {
    val t = alertText.lowercase()
    val highSignal =
        ("tornado" in t) ||
                ("flash flood" in t) ||
                ("considerable" in t) ||
                ("destructive" in t) ||
                (risk == RiskLevel.HIGH) ||
                (popPeak >= 50)

    return if (highSignal) ConfidenceLevel.HIGH else ConfidenceLevel.MEDIUM
}