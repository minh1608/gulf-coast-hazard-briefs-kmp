package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

sealed class BriefPage {
    abstract val pageNumber: Int
    abstract val title: String
    abstract fun renderBody(): String
}