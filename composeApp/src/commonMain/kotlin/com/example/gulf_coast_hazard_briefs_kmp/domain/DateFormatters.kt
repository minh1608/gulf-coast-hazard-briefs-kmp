package com.example.gulf_coast_hazard_briefs_kmp.domain

import kotlinx.datetime.LocalDate

private val MONTHS = listOf(
    "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
)

fun LocalDate.prettyUs(): String {
    val dow = this.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val mon = MONTHS[this.monthNumber - 1]
    return "$dow $mon ${this.dayOfMonth}, ${this.year}"
}