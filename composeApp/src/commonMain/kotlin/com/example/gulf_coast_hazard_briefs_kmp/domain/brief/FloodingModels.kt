package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

data class FloodingSignals(
    val eroLevel: Int = 0,                 // 0..4
    val qpfRangeInchesMin: Double? = null,  // 1–3 day
    val qpfRangeInchesMax: Double? = null,  // 1–3 day
    val sevenDayTotalInches: Double? = null,
    val riverGauges: List<RiverGauge> = emptyList(),
    val floodAlertEvents: List<String> = emptyList()
)

data class RiverGauge(
    val name: String,
    val stageFt: Double? = null,
    val category: FloodCategory = FloodCategory.NORMAL,
    val trend: RiverTrend = RiverTrend.UNKNOWN
)

enum class FloodCategory { NORMAL, ACTION, MINOR, MODERATE, MAJOR }
enum class RiverTrend { RISING, FALLING, STEADY, UNKNOWN }