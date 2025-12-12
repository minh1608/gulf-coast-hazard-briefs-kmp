package com.example.gulf_coast_hazard_briefs_kmp.domain.usecases

import com.example.gulf_coast_hazard_briefs_kmp.logic.BriefBuilder
import com.example.gulf_coast_hazard_briefs_kmp.model.BriefPage

class BuildWeeklyBrief(
    private val briefBuilder: BriefBuilder = BriefBuilder()
) {
    fun execute(): List<BriefPage> {
        return briefBuilder.build()
    }
}