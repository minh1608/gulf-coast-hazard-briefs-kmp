package com.example.gulf_coast_hazard_briefs_kmp.logic

import com.example.gulf_coast_hazard_briefs_kmp.model.BriefPage

class BriefBuilder {

    /**
     * Temporary stub implementation.
     * This will later orchestrate hazard detection, risk scoring,
     * and page activation.
     */
    fun build(): List<BriefPage> {
        return listOf(
            BriefPage.Page1Overview,
            BriefPage.Page2Severe,
            BriefPage.Page14Fun
        )
    }
}