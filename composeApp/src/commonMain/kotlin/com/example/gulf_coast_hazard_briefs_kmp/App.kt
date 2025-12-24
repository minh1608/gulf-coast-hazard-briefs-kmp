package com.example.gulf_coast_hazard_briefs_kmp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.UriHandler
import com.example.gulf_coast_hazard_briefs_kmp.domain.ForecastDay
import com.example.gulf_coast_hazard_briefs_kmp.domain.HazardLevel
import com.example.gulf_coast_hazard_briefs_kmp.domain.RiskLevel
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.BriefBuilder
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.BriefPage
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.ConfidenceLevel
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.DataSource
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.Page1Overview
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.Page2Convective
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.Page3Flooding
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.RiskTagged
import com.example.gulf_coast_hazard_briefs_kmp.ui.RemoteImage
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.gulf_coast_hazard_briefs_kmp.domain.brief.BriefExporter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

private val Bg = Color(0xFFFFFEFC)
private val Accent = Color(0xFFFF3131)
private val LightAccent = Color(0xFFFFABAB)
private val GrayBarBg = Color(0xFFD9D9D9)
private val TextBlack = Color(0xFF000000)

private const val NORTH_LAT = 29.7604
private const val NORTH_LON = -95.3698
private const val SOUTH_LAT = 27.8006
private const val SOUTH_LON = -97.3964

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()

        val platformName = getPlatform().name
        val isDesktop = platformName.startsWith("Java")
        val bodySize = if (isDesktop) 16.sp else 12.sp

        val builder = remember { BriefBuilder() }

        var loading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        var pages by remember { mutableStateOf<List<BriefPage>>(emptyList()) }
        var pageIndex by remember { mutableStateOf(0) }

        var lastUpdatedAt by remember { mutableStateOf<Instant?>(null) }
        var nowTick by remember { mutableStateOf(Clock.System.now()) }
        var footerAnimKey by remember { mutableStateOf(0) }
        val clipboard = LocalClipboardManager.current

        LaunchedEffect(Unit) {
            while (true) {
                nowTick = Clock.System.now()
                delay(60_000)
            }
        }

        fun trimEndingPeriod(s: String): String {
            var t = s.trim()
            while (t.endsWith(".")) t = t.dropLast(1).trimEnd()
            return t
        }

        fun capFirst(s: String): String {
            val t = s.trim()
            if (t.isEmpty()) return t
            return t.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() }
        }

        fun formatInstantPretty(i: Instant): String {
            val dt = i.toLocalDateTime(TimeZone.currentSystemDefault())
            val month = dt.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            val hour12 = ((dt.hour + 11) % 12) + 1
            val ampm = if (dt.hour < 12) "AM" else "PM"
            val min = dt.minute.toString().padStart(2, '0')
            return "$month ${dt.dayOfMonth}, ${dt.year} $hour12:$min $ampm"
        }

        fun lastUpdatedPretty(last: Instant?, now: Instant): String? {
            if (last == null) return null
            val minutesAgo = kotlin.math.max(0L, (now.epochSeconds - last.epochSeconds) / 60L)
            return when {
                minutesAgo <= 0L -> "Just now"
                minutesAgo == 1L -> "1 min ago"
                minutesAgo < 60L -> "${minutesAgo} min ago"
                else -> {
                    val h = minutesAgo / 60L
                    if (h == 1L) "1 hr ago" else "${h} hr ago"
                }
            }
        }

        fun formatDate(d: LocalDate): String {
            val dow = d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            val month = d.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            return "$dow $month ${d.dayOfMonth}, ${d.year}"
        }

        fun riskColor(r: RiskLevel): Color = when (r) {
            RiskLevel.NONE -> Color(0xFF00BF63)
            RiskLevel.LOW -> Color(0xFFFFDE59)
            RiskLevel.MEDIUM -> Color(0xFFFF914D)
            RiskLevel.HIGH -> Accent
        }

        fun hazardColor(h: HazardLevel): Color = when (h) {
            HazardLevel.NONE -> Color(0xFF00BF63)
            HazardLevel.LOW -> Color(0xFFFFDE59)
            HazardLevel.MODERATE -> Color(0xFFFF914D)
            HazardLevel.HIGH -> Accent
        }

        fun forecastIcon(f: String): String {
            val t = f.lowercase()
            return when {
                "thunder" in t || "storm" in t -> "⛈️"
                "rain" in t || "shower" in t || "drizzle" in t -> "🌧️"
                "snow" in t || "sleet" in t || "ice" in t || "freez" in t -> "❄️"
                "fog" in t || "mist" in t || "haze" in t -> "🌫️"
                "wind" in t || "breezy" in t || "gust" in t -> "💨"
                "overcast" in t -> "☁️"
                "cloud" in t -> "☁️"
                "partly" in t -> "🌤️"
                "mostly" in t -> "🌤️"
                "sun" in t || "clear" in t -> "☀️"
                else -> "🌡️"
            }
        }

        @Composable
        fun PulsingDot(color: Color, pulse: Boolean) {
            val transition = rememberInfiniteTransition(label = "pulse")
            val s = if (pulse) {
                transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                ).value
            } else 1f

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(s)
                    .background(color, shape = CircleShape)
            )
        }

        @Composable
        fun RiskTag(risk: RiskLevel) {
            val c = riskColor(risk)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pulse = (risk == RiskLevel.MEDIUM || risk == RiskLevel.HIGH)
                PulsingDot(color = c, pulse = pulse)
                Text(
                    text = "Risk: " + capFirst(risk.name.lowercase()),
                    fontSize = bodySize,
                    fontWeight = FontWeight.Bold,
                    color = c,
                    maxLines = 1
                )
            }
        }

        @Composable
        fun HazardTagDotText(level: HazardLevel) {
            val color = hazardColor(level)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pulse = (level == HazardLevel.MODERATE || level == HazardLevel.HIGH)
                PulsingDot(color = color, pulse = pulse)
                Text(
                    text = "Hazard: " + capFirst(level.name.lowercase()),
                    color = color,
                    fontSize = bodySize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        @Composable
        fun LabeledLine(label: String, value: String) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextBlack)) {
                        append(label)
                        append(": ")
                    }
                    withStyle(SpanStyle(color = TextBlack)) {
                        if (label.equals("Forecast", ignoreCase = true)) {
                            append(forecastIcon(value))
                            append(" ")
                            append(trimEndingPeriod(value))
                        } else {
                            append(trimEndingPeriod(value))
                        }
                    }
                },
                fontSize = bodySize,
                color = TextBlack
            )
        }

        @Composable
        fun SummaryLine(text: String) {
            val parts = text.split(":", limit = 2)
            Text(
                text = buildAnnotatedString {
                    if (parts.size == 2) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextBlack)) {
                            append(parts[0].trim())
                            append(": ")
                        }
                        append(capFirst(trimEndingPeriod(parts[1])))
                    } else {
                        append(capFirst(trimEndingPeriod(text)))
                    }
                },
                fontSize = bodySize,
                color = TextBlack
            )
        }

        @Composable
        fun SummaryLineLabelValue(label: String, value: String) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${capFirst(label)}: ",
                    fontWeight = FontWeight.Bold,
                    fontSize = bodySize,
                    color = TextBlack
                )
                Text(
                    text = capFirst(trimEndingPeriod(value)),
                    fontSize = bodySize,
                    color = TextBlack
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        @Composable
        fun MapBox(
            title: String,
            pageUrl: String?,
            imageUrl: String?,
            uriHandler: UriHandler
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GrayBarBg)
                    .padding(12.dp)
                    .clickable(enabled = !pageUrl.isNullOrBlank()) {
                        uriHandler.openUri(pageUrl!!)
                    }
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = bodySize, color = TextBlack)
                Spacer(Modifier.height(8.dp))

                // preview only if direct image link (.gif/.png/.jpg)
                if (!imageUrl.isNullOrBlank()) {
                    RemoteImage(
                        url = imageUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Text(
                    text = pageUrl ?: "No link available yet",
                    fontSize = bodySize,
                    color = TextBlack,
                    textDecoration = if (pageUrl.isNullOrBlank()) null else TextDecoration.Underline
                )
            }
        }

        @Composable
        fun KeyMessageLine(text: String) {
            val raw = trimEndingPeriod(text.replace(";", ",").trim())
            val idx = raw.indexOf(':')

            Text(
                text = buildAnnotatedString {
                    if (idx > 0) {
                        val head = raw.substring(0, idx).trim()
                        val tail = raw.substring(idx + 1).trim()
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextBlack)) {
                            append(capFirst(head))
                            append(": ")
                        }
                        withStyle(SpanStyle(color = TextBlack)) { append(capFirst(tail)) }
                    } else {
                        append(capFirst(raw))
                    }
                },
                fontSize = bodySize,
                color = TextBlack
            )
        }

        @Composable
        fun ForecastColumn(title: String, days: List<ForecastDay>, modifier: Modifier) {
            Column(modifier = modifier.fillMaxHeight()) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(days.take(7)) { d ->
                        Column {
                            Text(
                                text = formatDate(d.date),
                                fontWeight = FontWeight.Bold,
                                fontSize = bodySize,
                                color = TextBlack
                            )
                            Spacer(Modifier.height(4.dp))

                            HazardTagDotText(d.hazard)
                            Spacer(Modifier.height(4.dp))

                            d.high?.let { LabeledLine("High", "${it}°") }
                            d.low?.let { LabeledLine("Low", "${it}°") }
                            d.pop?.let { LabeledLine("POP", "${it}%") }
                            d.forecast?.takeIf { it.isNotBlank() }?.let { LabeledLine("Forecast", it) }
                        }
                    }
                }
            }
        }

        @Composable
        fun Page1View(p: Page1Overview) {
            Column(modifier = Modifier.fillMaxSize()) {
                fun peakPop(days: List<ForecastDay>): Int = days.mapNotNull { it.pop }.maxOrNull() ?: 0
                fun peakHigh(days: List<ForecastDay>): Int? = days.mapNotNull { it.high }.maxOrNull()
                fun minLow(days: List<ForecastDay>): Int? = days.mapNotNull { it.low }.minOrNull()

                val northPeakPop = peakPop(p.northDays)
                val southPeakPop = peakPop(p.southDays)
                val northHigh = peakHigh(p.northDays)
                val southHigh = peakHigh(p.southDays)
                val northLow = minLow(p.northDays)
                val southLow = minLow(p.southDays)

                Text("KEY MESSAGES", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(4.dp))
                p.keyMessages.forEach { KeyMessageLine(it) }

                Spacer(Modifier.height(16.dp))

                Text("SUMMARY", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(4.dp))
                SummaryLine("Peak POP: North ${northPeakPop}% vs South ${southPeakPop}%")
                if (northHigh != null && southHigh != null) SummaryLine("Highs: North ${northHigh}° vs South ${southHigh}°")
                if (northLow != null && southLow != null) SummaryLine("Lows: North ${northLow}° vs South ${southLow}°")

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ForecastColumn("NORTH", p.northDays, Modifier.weight(1f))
                    ForecastColumn("SOUTH", p.southDays, Modifier.weight(1f))
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // ---------- PAGE 2 CHARTS ----------

        @Composable
        fun HazardTimeline7(north: List<ForecastDay>, south: List<ForecastDay>) {
            Text("HAZARD TIMELINE (7-DAY)", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
            Spacer(Modifier.height(8.dp))

            val colW = 32.dp
            val iconSize = bodySize
            val dotSize = 12.dp
            val iconDotGap = 6.dp

            @Composable
            fun RowIconDot(label: String, days: List<ForecastDay>) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(label, modifier = Modifier.width(60.dp), fontSize = bodySize, color = TextBlack)
                    Spacer(Modifier.width(10.dp))

                    days.take(7).forEach { d ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(colW)
                        ) {
                            val f = d.forecast ?: ""
                            Text(text = forecastIcon(f), fontSize = iconSize, color = TextBlack)

                            Spacer(Modifier.height(iconDotGap))

                            Box(
                                modifier = Modifier
                                    .size(dotSize)
                                    .background(hazardColor(d.hazard), CircleShape)
                            )
                        }
                    }
                }
            }

            RowIconDot("North", north)
            Spacer(Modifier.height(8.dp))
            RowIconDot("South", south)
        }

        @Composable
        fun TempRangeChart7(north: List<ForecastDay>, south: List<ForecastDay>) {
            val n = north.take(7)
            val s = south.take(7)

            val WarmOrange = Color(0xFFFF914D)
            val ColdBlue = Color(0xFFA7C7E7)

            val warmestIdx = (0 until 7).maxByOrNull { i ->
                val nh = n.getOrNull(i)?.high ?: Int.MIN_VALUE
                val sh = s.getOrNull(i)?.high ?: Int.MIN_VALUE
                maxOf(nh, sh)
            } ?: 0

            val coldestIdx = (0 until 7).minByOrNull { i ->
                val nl = n.getOrNull(i)?.low ?: Int.MAX_VALUE
                val sl = s.getOrNull(i)?.low ?: Int.MAX_VALUE
                minOf(nl, sl)
            } ?: 0

            val t = rememberInfiniteTransition(label = "tempBarsPulse")
            val pulseAlpha = t.animateFloat(
                initialValue = 1f,
                targetValue = 0.55f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            ).value

            val temps = (n + s).flatMap { listOfNotNull(it.low, it.high) }
            val tMin = temps.minOrNull() ?: 0
            val tMax = temps.maxOrNull()?.takeIf { it != tMin } ?: (tMin + 1)

            fun norm(v: Int) = ((v - tMin).toFloat() / (tMax - tMin).toFloat()).coerceIn(0f, 1f)

            Text("TEMPERATURE RANGE", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
            Spacer(Modifier.height(8.dp))

            val dayGap = 8.dp
            val northSouthGap = 4.dp
            val barH = 12.dp

            Column(verticalArrangement = Arrangement.spacedBy(dayGap)) {
                (0 until 7).forEach { i ->
                    val day = n.getOrNull(i)?.date?.dayOfWeek?.name?.take(3) ?: ""

                    val nLow = n.getOrNull(i)?.low
                    val nHigh = n.getOrNull(i)?.high
                    val sLow = s.getOrNull(i)?.low
                    val sHigh = s.getOrNull(i)?.high

                    Text(day.uppercase(), fontWeight = FontWeight.Bold, fontSize = bodySize, color = TextBlack)

                    @Composable
                    fun RangeRow(label: String, low: Int?, high: Int?, barColor: Color) {
                        if (low == null || high == null) return

                        val left = norm(low)
                        val right = norm(high)
                        val widthFrac = (right - left).coerceAtLeast(0f)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, modifier = Modifier.width(60.dp), fontSize = bodySize, color = TextBlack)

                            Text(
                                text = "${low}° – ${high}°",
                                modifier = Modifier.width(90.dp),
                                fontSize = bodySize,
                                color = TextBlack
                            )

                            BoxWithConstraints(
                                modifier = Modifier
                                    .height(barH)
                                    .weight(1f)
                                    .background(GrayBarBg)
                            ) {
                                val totalW = maxWidth
                                val xLow = totalW * left

                                val isWarm = (i == warmestIdx)
                                val isCold = (i == coldestIdx)

                                val baseColor = when {
                                    isWarm -> WarmOrange
                                    isCold -> ColdBlue
                                    else -> barColor
                                }

                                val a = if (isWarm || isCold) pulseAlpha else 1f

                                Box(
                                    modifier = Modifier
                                        .offset(x = xLow)
                                        .width(totalW * widthFrac)
                                        .fillMaxHeight()
                                        .background(baseColor.copy(alpha = a))
                                )
                            }
                        }
                    }

                    RangeRow("North", nLow, nHigh, Accent)
                    Spacer(Modifier.height(northSouthGap))
                    RangeRow("South", sLow, sHigh, LightAccent)
                }
            }
        }

        @Composable
        fun PopChart7(north: List<ForecastDay>, south: List<ForecastDay>) {
            val n = north.take(7)
            val s = south.take(7)
            val maxPop = (n + s).mapNotNull { it.pop }.maxOrNull()?.coerceAtLeast(1) ?: 1

            Text("POP (7-DAY)", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(10.dp).background(Accent))
                    Text("North", fontSize = bodySize, color = TextBlack)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(10.dp).background(LightAccent))
                    Text("South", fontSize = bodySize, color = TextBlack)
                }
            }

            Spacer(Modifier.height(8.dp))

            val maxH = 60.dp
            val minLabelH = 22.dp

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                (0 until 7).forEach { i ->
                    val nPop = n.getOrNull(i)?.pop ?: 0
                    val sPop = s.getOrNull(i)?.pop ?: 0
                    val day = n.getOrNull(i)?.date?.dayOfWeek?.name?.take(3) ?: ""

                    val hN = maxH * (nPop.toFloat() / maxPop)
                    val hS = maxH * (sPop.toFloat() / maxPop)

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(day, fontSize = bodySize, color = TextBlack, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(hN)
                                .background(Accent)
                        ) {
                            if (hN >= minLabelH && nPop > 0) {
                                Text(
                                    text = "${nPop}%",
                                    modifier = Modifier.align(Alignment.Center),
                                    fontSize = bodySize,
                                    fontWeight = FontWeight.Bold,
                                    color = Bg
                                )
                            }
                        }

                        Spacer(Modifier.height(5.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(hS)
                                .background(LightAccent)
                        ) {
                            if (hS >= minLabelH && sPop > 0) {
                                Text(
                                    text = "${sPop}%",
                                    modifier = Modifier.align(Alignment.Center),
                                    fontSize = bodySize,
                                    fontWeight = FontWeight.Bold,
                                    color = Bg
                                )
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun Page2View(p2: Page2Convective, p1: Page1Overview?) {
            val scroll = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
            ) {
                if (p2.headline.isNotBlank()) {
                    Text("HEADLINE", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                    Spacer(Modifier.height(8.dp))
                    Text(capFirst(trimEndingPeriod(p2.headline)), fontSize = bodySize, color = TextBlack)
                    Spacer(Modifier.height(16.dp))
                }

                Text("WHAT / WHERE / WHEN / IMPACTS", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))
                SummaryLine("What: ${p2.stormType}")
                SummaryLine("Where: ${p2.affectedRegions}")
                SummaryLine("When: ${p2.timing}")

                if (p2.impacts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    p2.impacts.forEach { line ->
                        Text(capFirst(trimEndingPeriod(line)), fontSize = bodySize, color = TextBlack)
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("SUMMARY", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))
                SummaryLine("Risk: " + capFirst(p2.riskLevel.name.lowercase()))

                Spacer(Modifier.height(16.dp))

                Text("ACTIONS", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))
                p2.actions.forEach { a ->
                    Text(capFirst(trimEndingPeriod(a)), fontSize = bodySize, color = TextBlack)
                    Spacer(Modifier.height(4.dp))
                }

                if (p1 != null) {
                    Spacer(Modifier.height(16.dp))
                    HazardTimeline7(p1.northDays, p1.southDays)

                    Spacer(Modifier.height(16.dp))
                    TempRangeChart7(p1.northDays, p1.southDays)

                    Spacer(Modifier.height(16.dp))
                    PopChart7(p1.northDays, p1.southDays)
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        @Composable
        fun Page3View(p3: Page3Flooding) {
            val scroll = rememberScrollState()
            val uriHandler = LocalUriHandler.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
            ) {
                Text("HEADLINE", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))

                // Bold "Test Mode" inside headline
                val headline = capFirst(trimEndingPeriod(p3.floodHeadline))
                Text(
                    text = buildAnnotatedString {
                        val key = "Test Mode"
                        val idx = headline.indexOf(key, ignoreCase = true)
                        if (idx >= 0) {
                            append(headline.substring(0, idx))
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(headline.substring(idx, idx + key.length))
                            }
                            append(headline.substring(idx + key.length))
                        } else {
                            append(headline)
                        }
                    },
                    fontSize = bodySize,
                    color = TextBlack
                )

                Spacer(Modifier.height(16.dp))

                Text("SUMMARY", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))
                SummaryLineLabelValue("Risk", capFirst(p3.riskLevel.name.lowercase()))
                SummaryLineLabelValue("WPC ERO", capFirst(trimEndingPeriod(p3.wpcExcessiveRainSummary)))
                SummaryLineLabelValue("Rainfall", capFirst(trimEndingPeriod(p3.rainfallPotential)))
                SummaryLineLabelValue("River Gauges Flagged", p3.riverLines.size.toString())
                SummaryLineLabelValue("Flood Alerts", if (p3.floodAlertText.isBlank()) "None" else "Active")

                Spacer(Modifier.height(16.dp))

                Text("FLASH FLOOD / EXCESSIVE RAIN RISK", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))
                SummaryLineLabelValue("WPC Excessive Rainfall Outlook", capFirst(trimEndingPeriod(p3.wpcExcessiveRainSummary)))
                SummaryLineLabelValue("Rainfall Potential", capFirst(trimEndingPeriod(p3.rainfallPotential)))

                Spacer(Modifier.height(16.dp))

                Text("RIVER & LAKE FLOODING STATUS", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))
                if (p3.riverLines.isNotEmpty()) {
                    p3.riverLines.forEach { line ->
                        Text("• " + capFirst(trimEndingPeriod(line)), fontSize = bodySize, color = TextBlack)
                        Spacer(Modifier.height(4.dp))
                    }
                } else {
                    Text(
                        "All TGCR river levels remain within normal range (no gauge flags)",
                        fontSize = bodySize,
                        color = TextBlack
                    )
                }
                Spacer(Modifier.height(8.dp))
                SummaryLineLabelValue("Flood Alerts", capFirst(trimEndingPeriod(p3.floodAlertText.ifBlank { "None" })))

                Spacer(Modifier.height(16.dp))

                Text("LOCAL FLOODING IMPACTS", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))
                if (p3.impacts.isNotEmpty()) {
                    p3.impacts.forEach { imp ->
                        Text("• " + capFirst(trimEndingPeriod(imp)), fontSize = bodySize, color = TextBlack)
                        Spacer(Modifier.height(4.dp))
                    }
                } else {
                    Text("• No flooding impacts indicated", fontSize = bodySize, color = TextBlack)
                }

                Spacer(Modifier.height(16.dp))

                Text("FORECAST MAPS", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Accent)
                Spacer(Modifier.height(8.dp))

                MapBox(
                    title = "WPC QPF (Day 1–3)",
                    pageUrl = p3.wpcQpfPageUrl,
                    imageUrl = p3.wpcQpfImageUrl,
                    uriHandler = uriHandler
                )
                Spacer(Modifier.height(10.dp))

                MapBox(
                    title = "NWS Radar Loop (KHGX — Houston/Galveston)",
                    pageUrl = p3.ahpsPageUrl,
                    imageUrl = p3.ahpsImageUrl,
                    uriHandler = uriHandler
                )
                Spacer(Modifier.height(10.dp))

                MapBox(
                    title = "NWS Graphical Forecast (South Plains — Weather/PoP)",
                    pageUrl = p3.nwsRainPageUrl,
                    imageUrl = p3.nwsRainImageUrl,
                    uriHandler = uriHandler
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        @Composable
        fun BodyRichText(text: String) {
            Text(text = capFirst(trimEndingPeriod(text)), fontSize = bodySize, color = TextBlack)
        }

        @Composable
        fun StickyFooter(page: BriefPage, animKey: Int) {
            val sources: Set<DataSource> = when (page) {
                is Page1Overview -> page.sources
                is Page2Convective -> page.sources
                is Page3Flooding -> page.sources
                else -> emptySet()
            }

            val confidence: ConfidenceLevel? = when (page) {
                is Page1Overview -> page.confidence
                is Page2Convective -> page.confidence
                is Page3Flooding -> page.confidence
                else -> null
            }

            val generatedAt: Instant? = when (page) {
                is Page1Overview -> page.generatedAt
                is Page2Convective -> page.generatedAt
                is Page3Flooding -> page.generatedAt
                else -> null
            }

            val updatedText = lastUpdatedPretty(lastUpdatedAt, nowTick)

            AnimatedContent(
                targetState = animKey,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn())
                        .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "footerAnim"
            ) { k ->
                key(k) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Bg)
                            .padding(top = 10.dp, bottom = 6.dp)
                    ) {
                        if (sources.isNotEmpty()) {
                            SummaryLine("Sources: " + sources.joinToString { it.label })
                        }
                        confidence?.let { SummaryLine("Confidence: ${it.label}") }
                        generatedAt?.let { ga -> SummaryLine("Generated: ${formatInstantPretty(ga)}") }

                        updatedText?.let { ut ->
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Updated: ") }
                                    append(ut)
                                },
                                fontSize = bodySize,
                                color = TextBlack
                            )
                        }
                    }
                }
            }
        }

        // ---------------- UI ----------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .padding(24.dp)
        ) {
            Text(
                "Weekly Weather Brief",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Accent
            )
            Spacer(Modifier.height(16.dp))

            // ✅ Refresh + Copy Brief on ONE line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                val built = builder.buildBrief(
                                    northLat = NORTH_LAT,
                                    northLon = NORTH_LON,
                                    southLat = SOUTH_LAT,
                                    southLon = SOUTH_LON
                                )
                                pages = built
                                pageIndex = 0
                                lastUpdatedAt = Clock.System.now()
                                footerAnimKey += 1
                            } catch (t: Throwable) {
                                val msg = when (t) {
                                    is HttpRequestTimeoutException,
                                    is SocketTimeoutException,
                                    is ConnectTimeoutException ->
                                        "Network timeout. NWS can be slow — please tap Refresh again"
                                    else -> (t.message ?: t.toString())
                                }
                                error = msg
                            } finally {
                                loading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (loading) LightAccent else Accent,
                        disabledContainerColor = LightAccent
                    )
                ) {
                    Text(if (loading) "Loading..." else "Refresh", color = Bg, fontSize = bodySize)
                }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = pages.isNotEmpty(),
                    onClick = {
                        val md = BriefExporter.exportMarkdown(pages)
                        clipboard.setText(AnnotatedString(md))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        disabledContainerColor = Accent.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Copy Brief", fontSize = bodySize, color = Bg)
                }
            }

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text("Error: $error", color = MaterialTheme.colorScheme.error, fontSize = bodySize)
            }

            Spacer(Modifier.height(16.dp))

            // ---- giữ nguyên phần render pages ở dưới này ----
            if (pages.isNotEmpty()) {
                val page = pages[pageIndex]
                val p1 = pages.firstOrNull { it is Page1Overview } as? Page1Overview

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Page ${page.pageNumber}",
                        fontSize = bodySize,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    if (page is RiskTagged) RiskTag(page.riskLevel)
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = page.title,
                    fontSize = bodySize,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (page) {
                            is Page1Overview -> Page1View(page)
                            is Page2Convective -> Page2View(page, p1)
                            is Page3Flooding -> Page3View(page)
                            else -> BodyRichText(page.renderBody())
                        }
                    }

                    StickyFooter(page, footerAnimKey)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { pageIndex = 0 },
                        enabled = pages.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            disabledContainerColor = Accent.copy(alpha = 0.3f)
                        )
                    ) { Text("Page 1", fontSize = bodySize, color = Bg) }

                    Button(
                        onClick = { pageIndex = 1 },
                        enabled = pages.size > 1,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            disabledContainerColor = Accent.copy(alpha = 0.3f)
                        )
                    ) { Text("Page 2", fontSize = bodySize, color = Bg) }

                    Button(
                        onClick = { pageIndex = 2 },
                        enabled = pages.size > 2,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            disabledContainerColor = Accent.copy(alpha = 0.3f)
                        )
                    ) { Text("Page 3", fontSize = bodySize, color = Bg) }
                }
            }
        }
    }
}