package com.example.gulf_coast_hazard_briefs_kmp.model

sealed class BriefPage(
    val pageNumber: Int,
    val title: String
) {

    data object Page1Overview : BriefPage(
        pageNumber = 1,
        title = "Weekly Overview"
    )

    data object Page2Severe : BriefPage(
        pageNumber = 2,
        title = "Severe Weather"
    )

    data object Page14Fun : BriefPage(
        pageNumber = 14,
        title = "Fun / Weird Weather"
    )

    fun renderBody(): String = when (this) {
        Page1Overview ->
            "Demo build: shared Kotlin Multiplatform logic is running successfully."

        Page2Severe ->
            "Stub: No active severe weather risk detected for the Gulf Coast this week."

        Page14Fun ->
            "Fun or unusual weather-related story will appear here."
    }
}