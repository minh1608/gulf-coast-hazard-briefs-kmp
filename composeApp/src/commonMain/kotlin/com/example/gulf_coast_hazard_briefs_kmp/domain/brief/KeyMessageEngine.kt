package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import com.example.gulf_coast_hazard_briefs_kmp.domain.HazardLevel
import com.example.gulf_coast_hazard_briefs_kmp.domain.HazardRules
import kotlin.math.abs
import kotlin.math.max

object KeyMessageEngine {

    fun build(north: List<ForecastDay>, south: List<ForecastDay>): List<String> {
        val n7 = north.take(7)
        val s7 = south.take(7)

        fun maxPop(days: List<ForecastDay>) = days.mapNotNull { it.pop }.maxOrNull() ?: 0
        fun maxHigh(days: List<ForecastDay>) = days.mapNotNull { it.high }.maxOrNull()
        fun minLow(days: List<ForecastDay>) = days.mapNotNull { it.low }.minOrNull()

        fun day3(d: ForecastDay): String =
            d.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)

        fun peakPopDay(days: List<ForecastDay>): Pair<String, Int>? {
            val idx = days.indices.maxByOrNull { i -> days[i].pop ?: 0 } ?: return null
            val v = days[idx].pop ?: return null
            return day3(days[idx]) to v
        }

        fun tempSwing(days: List<ForecastDay>): Int? {
            val hi = maxHigh(days) ?: return null
            val lo = minLow(days) ?: return null
            return hi - lo
        }

        fun hazardWord(level: HazardLevel): String =
            level.name.lowercase().replaceFirstChar { it.uppercase() }

        fun levelTag(level: HazardLevel): String = when (level) {
            HazardLevel.NONE -> "None"
            HazardLevel.LOW -> "Low"
            HazardLevel.MODERATE -> "Moderate"
            HazardLevel.HIGH -> "High"
        }

        fun sentence(s: String): String {
            val t = s.trim().removeSuffix(".")
            return t.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() }
        }

        // ---------- Better: typed candidates + scoring ----------
        data class Candidate(
            val topic: String,     // "hazard" | "rain" | "temps" | "cold"
            val score: Int,        // higher = more important
            val text: String
        )

        val cands = mutableListOf<Candidate>()

        // ---------- Hazard (prefer peak over overall) ----------
        val nOverall = HazardRules.overall(n7)
        val sOverall = HazardRules.overall(s7)
        val overallMax = maxOf(nOverall, sOverall)

        val nPeak = HazardRules.peak(n7)
        val sPeak = HazardRules.peak(s7)
        val peak = listOfNotNull(nPeak, sPeak).maxByOrNull { it.hazard.ordinal }

        if (peak != null && peak.hazard != HazardLevel.NONE) {
            val sc = when (peak.hazard) {
                HazardLevel.HIGH -> 100
                HazardLevel.MODERATE -> 80
                HazardLevel.LOW -> 60
                HazardLevel.NONE -> 0
            }
            cands += Candidate(
                topic = "hazard",
                score = sc,
                text = sentence("Hazard (${levelTag(peak.hazard)}): Peak hazard ${hazardWord(peak.hazard)} on ${day3(peak)}.")
            )
        } else {
            // if no peak hazard, show overall (still useful)
            val sc = when (overallMax) {
                HazardLevel.HIGH -> 90
                HazardLevel.MODERATE -> 70
                HazardLevel.LOW -> 50
                HazardLevel.NONE -> 10
            }
            cands += Candidate(
                topic = "hazard",
                score = sc,
                text = sentence("Hazard (${levelTag(overallMax)}): North ${hazardWord(nOverall)}, South ${hazardWord(sOverall)}.")
            )
        }

        // ---------- Rain (merge peak + split into ONE bullet) ----------
        val nPop = maxPop(n7)
        val sPop = maxPop(s7)
        val popPeak = max(nPop, sPop)
        val popDiff = abs(nPop - sPop)

        val popDayN = peakPopDay(n7)
        val popDayS = peakPopDay(s7)
        val popDay = listOfNotNull(popDayN, popDayS).maxByOrNull { it.second }?.first

        if (popPeak >= 30 || popDiff >= 25) {
            val level = when {
                popPeak >= 70 -> "High"
                popPeak >= 50 -> "Elevated"
                else -> "Low"
            }
            val sc = when {
                popPeak >= 70 -> 95
                popPeak >= 50 -> 75
                popPeak >= 30 -> 45
                else -> 25
            } + if (popDiff >= 25) 10 else 0

            val peakPart =
                if (popPeak >= 30) "Peak POP ~${popPeak}%${popDay?.let { " on $it" } ?: ""}" else null
            val splitPart =
                if (popDiff >= 25) "North vs South differs by ~${popDiff}%" else null

            val joined = listOfNotNull(peakPart, splitPart).joinToString("; ")
            cands += Candidate(
                topic = "rain",
                score = sc,
                text = sentence("Rain ($level): $joined.")
            )
        }

        // ---------- Temps ----------
        val nMax = maxHigh(n7)
        val sMax = maxHigh(s7)
        val splitHi = if (nMax != null && sMax != null) abs(nMax - sMax) else null

        val swing = listOfNotNull(tempSwing(n7), tempSwing(s7)).maxOrNull()

        if (splitHi != null && splitHi >= 10) {
            cands += Candidate(
                topic = "temps",
                score = 35 + (splitHi - 10),
                text = sentence("Temps (Split): Daytime highs differ by about ${splitHi}°.")
            )
        }

        if (swing != null && swing >= 18) {
            cands += Candidate(
                topic = "temps",
                score = 30 + (swing - 18),
                text = sentence("Temps (Swing): Week range near ${swing}°.")
            )
        }

        // ---------- Cold nights ----------
        val cold = listOfNotNull(minLow(n7), minLow(s7)).minOrNull()
        if (cold != null && cold <= 40) {
            val level = if (cold <= 32) "Freeze" else "Cool"
            val sc = if (cold <= 32) 85 else 25
            cands += Candidate(
                topic = "cold",
                score = sc,
                text = sentence("Cold Nights ($level): Lows near ${cold}°.")
            )
        }

        // ---------- Better: calm-week cap ----------
        val calmWeek = (overallMax == HazardLevel.NONE && popPeak < 30)

        if (calmWeek) {
            cands += Candidate(
                topic = "calm",
                score = 200,
                text = sentence("Calm week: No major hazards expected; routine conditions.")
            )
        }

        val maxBullets = if (calmWeek) 3 else 5

// ---------- Better: de-dup by topic (keep best per topic) ----------
        val bestPerTopic = cands
            .groupBy { it.topic }
            .mapValues { (_, list) -> list.maxByOrNull { it.score }!! }
            .values
            .toList()

// ---------- Sort by score desc, then output ----------
        return bestPerTopic
            .sortedByDescending { it.score }
            .map { it.text }
            .distinct()
            .take(maxBullets)
    }
}