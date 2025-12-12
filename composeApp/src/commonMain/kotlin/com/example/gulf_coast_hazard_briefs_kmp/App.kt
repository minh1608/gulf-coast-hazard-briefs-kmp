package com.example.gulf_coast_hazard_briefs_kmp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gulf_coast_hazard_briefs_kmp.domain.usecases.FetchNwsDebug
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay

private const val NORTH_LAT = 29.7604   // Houston-ish
private const val NORTH_LON = -95.3698
private const val SOUTH_LAT = 27.8006   // South TX-ish
private const val SOUTH_LON = -97.3964

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var loading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var northText by remember { mutableStateOf("") }
        var southText by remember { mutableStateOf("") }

        fun render(
            location: String,
            days: List<ForecastDay>
        ): String {
            val top = "Location: $location\n\n"
            val body = days.take(7).joinToString("\n\n") { d ->
                buildString {
                    append("${d.label} (${d.date})\n")
                    append("High: ${d.highF ?: "?"}°F\n")
                    append("Low: ${d.lowF ?: "?"}°F\n")
                    append("Wind: ${d.wind ?: "?"}\n")
                    append("Forecast: ${d.shortForecast ?: ""}")
                }
            }
            return top + body
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Phase 2 — NWS Fetch (North + South)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Button(
                enabled = !loading,
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            val usecase = FetchNwsDebug()
                            val north = usecase.execute(NORTH_LAT, NORTH_LON)
                            val south = usecase.execute(SOUTH_LAT, SOUTH_LON)

                            northText = render(north.locationName, north.days)
                            southText = render(south.locationName, south.days)
                        } catch (t: Throwable) {
                            error = t.message ?: t.toString()
                        } finally {
                            loading = false
                        }
                    }
                }
            ) {
                Text(if (loading) "Loading..." else "Refresh (NWS)")
            }

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(end = 8.dp)
                ) {
                    Text("NORTH", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(northText.ifBlank { "Press Refresh…" })
                }

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 8.dp)
                ) {
                    Text("SOUTH", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(southText.ifBlank { "Press Refresh…" })
                }
            }
        }
    }
}