package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import kotlin.math.abs

class KeyMessageEngine {

    fun buildKeyMessages(north: List<ForecastDay>, south: List<ForecastDay>): List<String> {
        val msgs = mutableListOf<String>()

        val maxPop = (north + south).maxOfOrNull { it.pop ?: 0 } ?: 0
        if (maxPop >= 50) msgs += "Rain chances are elevated at times this week (POP up to $maxPop%)."
        else if (maxPop in 20..49) msgs += "Spotty rain is possible this week (POP up to $maxPop%)."
        else msgs += "Most days look relatively quiet (low rain chances overall)."

        fun swing(days: List<ForecastDay>): Int {
            val highs = days.mapNotNull { it.high }
            if (highs.size < 2) return 0
            return highs.maxOrNull()!! - highs.minOrNull()!!
        }

        val northSwing = swing(north)
        val southSwing = swing(south)
        val biggestSwing = maxOf(northSwing, southSwing)
        if (biggestSwing >= 15) msgs += "Notable temperature swings are possible (up to ~${biggestSwing}°)."

        val coldLow = (north + south).mapNotNull { it.low }.minOrNull()
        if (coldLow != null && coldLow <= 40) msgs += "Cooler nights expected (lows down to ~${coldLow}°)."

        return msgs.take(5)
    }

    fun buildNorthVsSouth(north: List<ForecastDay>, south: List<ForecastDay>): List<String> {
        val bullets = mutableListOf<String>()

        val nMax = north.mapNotNull { it.high }.maxOrNull()
        val sMax = south.mapNotNull { it.high }.maxOrNull()
        if (nMax != null && sMax != null) {
            val diff = nMax - sMax
            if (abs(diff) >= 5) {
                bullets += if (diff > 0) "North trends warmer on the warmest day (~${nMax}° vs ~${sMax}°)."
                else "South trends warmer on the warmest day (~${sMax}° vs ~${nMax}°)."
            } else {
                bullets += "High temperatures are broadly similar north vs south."
            }
        }

        val nPop = north.maxOfOrNull { it.pop ?: 0 } ?: 0
        val sPop = south.maxOfOrNull { it.pop ?: 0 } ?: 0
        val popDiff = nPop - sPop
        if (abs(popDiff) >= 15) {
            bullets += if (popDiff > 0) "North has higher peak rain chances (POP ${nPop}% vs ${sPop}%)."
            else "South has higher peak rain chances (POP ${sPop}% vs ${nPop}%)."
        } else {
            bullets += "Rain chances are comparable north vs south."
        }

        return bullets.take(4)
    }
}