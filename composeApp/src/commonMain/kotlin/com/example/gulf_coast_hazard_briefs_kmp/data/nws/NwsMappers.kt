package com.example.gulf_coast_hazard_briefs_kmp.data.nws

import com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto.NwsForecastDto
import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import kotlinx.datetime.LocalDate

private fun parseLocalDateFromStartTime(startTime: String?): LocalDate? {
    // Example: "2025-12-12T06:00:00-06:00"
    val datePart = startTime?.take(10) ?: return null
    return runCatching { LocalDate.parse(datePart) }.getOrNull()
}

internal fun periodsToForecastDays(
    periods: List<NwsForecastDto.Period>
): List<ForecastDay> {

    val byDate: Map<LocalDate, List<NwsForecastDto.Period>> =
        periods
            .mapNotNull { p ->
                val date = parseLocalDateFromStartTime(p.startTime) ?: return@mapNotNull null
                date to p
            }
            .groupBy({ it.first }, { it.second })

    return byDate
        .toSortedMap()
        .map { (date, ps) ->

            val day = ps.firstOrNull { p ->
                val n = (p.name ?: "").lowercase()
                !n.contains("night") && !n.contains("tonight")
            }

            val night = ps.firstOrNull { p ->
                val n = (p.name ?: "").lowercase()
                n.contains("night") || n.contains("tonight")
            }

            val fallback = ps.firstOrNull()

            ForecastDay(
                date = date,
                high = day?.temperature ?: fallback?.temperature,
                low = night?.temperature,
                pop = day?.probabilityOfPrecipitation?.value
                    ?: night?.probabilityOfPrecipitation?.value
                    ?: fallback?.probabilityOfPrecipitation?.value,
                forecast = day?.shortForecast
                    ?: night?.shortForecast
                    ?: fallback?.shortForecast
                // 🚫 NO hazard here
            )
        }
}