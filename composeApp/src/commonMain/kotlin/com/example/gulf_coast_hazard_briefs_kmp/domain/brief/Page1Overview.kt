package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay

data class Page1Overview(
    val keyMessages: List<String>,
    val northDays: List<ForecastDay>,
    val southDays: List<ForecastDay>,
    val northVsSouth: List<String>
) : BriefPage() {

    override val pageNumber: Int = 1
    override val title: String = "Weekly Overview"

    override fun renderBody(): String = buildString {
        appendLine("KEY MESSAGES")
        keyMessages.forEach { appendLine("• $it") }
        appendLine()

        appendLine("NORTH vs SOUTH")
        northVsSouth.forEach { appendLine("• $it") }
        appendLine()

        appendLine("NORTH (7-day)")
        northDays.take(7).forEach { d ->
            appendLine("${d.date}  High ${d.high ?: "-"}  Low ${d.low ?: "-"}  POP ${d.pop ?: "-"}  ${d.forecast.orEmpty()}")
        }
        appendLine()

        appendLine("SOUTH (7-day)")
        southDays.take(7).forEach { d ->
            appendLine("${d.date}  High ${d.high ?: "-"}  Low ${d.low ?: "-"}  POP ${d.pop ?: "-"}  ${d.forecast.orEmpty()}")
        }
    }
}