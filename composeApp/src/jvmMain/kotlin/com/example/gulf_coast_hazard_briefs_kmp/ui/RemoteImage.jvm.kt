package com.example.gulf_coast_hazard_briefs_kmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
actual fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    KamelImage(
        resource = asyncPainterResource(url),
        contentDescription = contentDescription,
        modifier = modifier.heightIn(min = 160.dp),
        contentScale = contentScale,
        onLoading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(160.dp)
                    .background(Color(0xFFD9D9D9)),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        },
        onFailure = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(160.dp)
                    .background(Color(0xFFD9D9D9)),
                contentAlignment = Alignment.Center
            ) { Text("Preview unavailable") }
        }
    )
}