package com.example.gulf_coast_hazard_briefs_kmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "gulf_coast_hazard_briefs_kmp",
    ) {
        App()
    }
}