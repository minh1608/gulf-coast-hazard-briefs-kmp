package com.example.gulf_coast_hazard_briefs_kmp.domain.brief

import com.example.gulf_coast_hazard_briefs_kmp.domain.RiskLevel

interface RiskTagged {
    val riskLevel: RiskLevel
}