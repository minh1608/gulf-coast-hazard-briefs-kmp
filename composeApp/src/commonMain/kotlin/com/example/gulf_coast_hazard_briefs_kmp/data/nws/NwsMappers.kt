package com.example.gulf_coast_hazard_briefs_kmp.data.nws

import com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto.NwsForecastDto
import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun periodsToForecastDays(
    periods: List<NwsForecastDto.Period>
): List<ForecastDay> {

    val byDate: Map<LocalDate, List<NwsForecastDto.Period>> =
        periods
            .mapNotNull { p ->
                val iso = p.startTime ?: return@mapNotNull null

                val date = runCatching {
                    Instant.parse(iso)
                        .toLocalDateTime(TimeZone.UTC)
                        .date
                }.getOrNull() ?: return@mapNotNull null

                date to p
            }
            .groupBy({ it.first }, { it.second })

    return byDate
        .toSortedMap()
        .map { (date, ps) ->

            val day = ps.firstOrNull {
                !(it.name ?: "").contains("night", ignoreCase = true)
            }

            val night = ps.firstOrNull {
                (it.name ?: "").contains("night", ignoreCase = true)
            }

            ForecastDay(
                date = date,
                label = date.dayOfWeek.name.take(3), // MON / TUE
                highF = day?.temperature,
                lowF = night?.temperature,
                shortForecast = day?.shortForecast ?: night?.shortForecast,
                wind = day?.windSpeed ?: night?.windSpeed
            )
        }
}