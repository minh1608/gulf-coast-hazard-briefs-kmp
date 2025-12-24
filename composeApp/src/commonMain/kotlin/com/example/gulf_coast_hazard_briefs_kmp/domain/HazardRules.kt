package com.example.gulf_coast_hazard_briefs_kmp.domain

object HazardRules {

    // --- Tunable thresholds (aim: fewer "LOW" spam days) ---
    private const val POP_LOW = 40
    private const val POP_MODERATE = 60
    private const val POP_HIGH = 80

    private const val HEAT_MODERATE = 90
    private const val HEAT_HIGH = 95

    private const val COLD_MODERATE = 40
    private const val COLD_HIGH = 32

    fun apply(days: List<ForecastDay>): List<ForecastDay> =
        days.map { it.copy(hazard = compute(it)) }

    fun overall(days: List<ForecastDay>): HazardLevel =
        days.map { it.hazard }.maxByOrNull { it.ordinal } ?: HazardLevel.NONE

    fun peak(days: List<ForecastDay>): ForecastDay? =
        days.maxByOrNull { it.hazard.ordinal }

    private fun compute(d: ForecastDay): HazardLevel {
        val pop = d.pop ?: 0
        val high = d.high
        val low = d.low

        val popLevel = when {
            pop >= POP_HIGH -> HazardLevel.HIGH
            pop >= POP_MODERATE -> HazardLevel.MODERATE
            pop >= POP_LOW -> HazardLevel.LOW
            else -> HazardLevel.NONE
        }

        val heatLevel = when {
            high != null && high >= HEAT_HIGH -> HazardLevel.HIGH
            high != null && high >= HEAT_MODERATE -> HazardLevel.MODERATE
            else -> HazardLevel.NONE
        }

        val coldLevel = when {
            low != null && low <= COLD_HIGH -> HazardLevel.HIGH
            low != null && low <= COLD_MODERATE -> HazardLevel.MODERATE
            else -> HazardLevel.NONE
        }

        return listOf(popLevel, heatLevel, coldLevel).maxBy { it.ordinal }
    }
}