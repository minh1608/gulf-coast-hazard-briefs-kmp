package com.example.gulf_coast_hazard_briefs_kmp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
        modifier = modifier,
        contentScale = contentScale
    )
}