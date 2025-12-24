package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

enum class DataSource(val label: String) {
    NWS_FORECAST("National Weather Service (Forecast)"),
    NWS_ALERTS("National Weather Service (Alerts)"),
    SPC("Storm Prediction Center"),
    WPC("Weather Prediction Center"),
    AHPS("Advanced Hydrologic Prediction Service (AHPS)")
}