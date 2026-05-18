package dev.c4g7.library.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun VinylDisk(
    coverArtBytes: ByteArray?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(
                        durationMillis = 3200,
                        easing = LinearEasing
                    )
                )
            }
        } else {
            rotation.stop()
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 32.dp, shape = CircleShape, ambientColor = Color(0x44EF5350), spotColor = Color(0x44EF5350))
            .rotate(rotation.value),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outerRadius = this.size.minDimension / 2

            // Base vinyl
            drawCircle(color = Color(0xFF0A0A0A), radius = outerRadius)

            // Groove rings
            val grooveColor = Color(0xFF1E1E1E)
            var r = outerRadius * 0.95f
            while (r > outerRadius * 0.58f) {
                drawCircle(
                    color = grooveColor,
                    radius = r,
                    style = Stroke(width = 1.2f)
                )
                r -= 5f
            }

            // Label area (inner circle background)
            drawCircle(
                color = Color(0xFF141414),
                radius = outerRadius * 0.54f
            )

            // Center hole
            drawCircle(
                color = Color(0xFF000000),
                radius = outerRadius * 0.045f
            )
        }

        // Album art in center
        val artSize = size * 0.48f
        Box(
            modifier = Modifier
                .size(artSize)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (coverArtBytes != null) {
                AsyncImage(
                    model = coverArtBytes,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF2A2A2A), Color(0xFF0A0A0A))
                        )
                    )
                }
            }
        }
    }
}
