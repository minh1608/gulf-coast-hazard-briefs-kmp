package com.example.gulf_coast_hazard_briefs_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform