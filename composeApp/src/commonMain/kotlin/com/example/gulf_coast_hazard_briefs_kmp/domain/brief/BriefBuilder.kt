package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.usecases.FetchNwsDebug

private const val NORTH_LAT = 29.7604
private const val NORTH_LON = -95.3698
private const val SOUTH_LAT = 27.8006
private const val SOUTH_LON = -97.3964

class BriefBuilder(
    private val fetch: FetchNwsDebug = FetchNwsDebug(),
    private val km: KeyMessageEngine = KeyMessageEngine()
) {
    suspend fun buildBrief(): List<BriefPage> {
        val north = fetch.execute(NORTH_LAT, NORTH_LON)
        val south = fetch.execute(SOUTH_LAT, SOUTH_LON)

        val keyMessages = km.buildKeyMessages(north.days, south.days)
        val northVsSouth = km.buildNorthVsSouth(north.days, south.days)

        val page1 = Page1Overview(
            keyMessages = keyMessages,
            northDays = north.days,
            southDays = south.days,
            northVsSouth = northVsSouth
        )

        return listOf(page1)
    }
}