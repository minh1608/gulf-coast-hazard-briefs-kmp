package com.example.gulf_coast_hazard_briefs_kmp.model

/**
 * High-level hazard categories used across all 14 pages.
 */
enum class HazardType {
    SEVERE_STORMS,        // Thunderstorms, tornadoes, hail, damaging wind
    FLOODING,             // River, flash, urban flooding
    TEMPERATURE_EXTREMES, // Heat, cold, wind chill, freeze
    FIRE_WEATHER,         // Wildfire danger, burn bans
    COASTAL,              // Marine, surf, rip currents, coastal flooding
    TROPICAL,             // Tropical storms, hurricanes
    WINTER_WEATHER,       // Snow, sleet, ice
    AIR_QUALITY,          // Smoke, ozone, dust
    OTHER                 // Fog, high winds, misc. hazards
}