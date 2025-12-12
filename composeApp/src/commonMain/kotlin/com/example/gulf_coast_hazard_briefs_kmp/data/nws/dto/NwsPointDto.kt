package com.example.gulf_coast_hazard_briefs_kmp.data.nws.dto

import kotlinx.serialization.Serializable

@Serializable
data class NwsPointDto(
    val properties: Properties
) {
    @Serializable
    data class Properties(
        val relativeLocation: RelativeLocation? = null,
        val forecast: String
    )

    @Serializable
    data class RelativeLocation(
        val properties: RelativeLocationProps? = null
    )

    @Serializable
    data class RelativeLocationProps(
        val city: String? = null,
        val state: String? = null
    )
}