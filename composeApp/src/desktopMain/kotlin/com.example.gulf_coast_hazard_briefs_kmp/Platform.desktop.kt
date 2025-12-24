package com.example.gulf_coast_hazard_briefs_kmp

private class DesktopPlatform : Platform {
    override val name: String = "Java (Desktop)"
}

actual fun getPlatform(): Platform = DesktopPlatform()