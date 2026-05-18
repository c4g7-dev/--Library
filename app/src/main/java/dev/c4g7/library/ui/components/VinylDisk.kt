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
import androidx.compose.ui.graphics.*
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
    size: Dp = 288.dp
) {
    val rotation = remember { Animatable(0f) }
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.25f,
        animationSpec = tween(700, easing = EaseInOutQuad),
        label = "glow"
    )

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(durationMillis = 3400, easing = LinearEasing)
                )
            }
        } else {
            rotation.stop()
        }
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        // Glow halo — blue tint, visible on AMOLED
        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = (40 * glowAlpha).dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF5BA4F5).copy(alpha = 0.28f * glowAlpha),
                    spotColor = Color(0xFF82B4FF).copy(alpha = 0.45f * glowAlpha)
                )
        )

        Box(
            modifier = Modifier
                .size(size)
                .rotate(rotation.value),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = this.size.width / 2
                val cy = this.size.height / 2
                val outerR = this.size.minDimension / 2f

                // Vinyl body — dark charcoal radial gradient (clearly NOT black on AMOLED)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2C2C2C), Color(0xFF141414)),
                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                        radius = outerR
                    ),
                    radius = outerR
                )

                // Strong outer chrome rim — makes the disk edge pop against AMOLED black
                drawCircle(
                    color = Color(0xFF484848),
                    radius = outerR - 1f,
                    style = Stroke(width = 3.5f)
                )

                // Second inner rim ring
                drawCircle(
                    color = Color(0xFF2E2E2E),
                    radius = outerR - 5f,
                    style = Stroke(width = 1f)
                )

                // Groove rings — high contrast alternating for depth
                var r = outerR * 0.94f
                var toggle = false
                while (r > outerR * 0.565f) {
                    drawCircle(
                        color = if (toggle) Color(0xFF323232) else Color(0xFF161616),
                        radius = r,
                        style = Stroke(width = 1.6f)
                    )
                    r -= 4.5f
                    toggle = !toggle
                }

                // Shine arc (upper-left, 10% alpha — visible highlight)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0f),
                        ),
                        center = androidx.compose.ui.geometry.Offset(cx, cy)
                    ),
                    startAngle = 195f,
                    sweepAngle = 110f,
                    useCenter = false,
                    style = Stroke(width = outerR * 0.07f)
                )

                // Label area — dark navy (visually distinct from the grooves)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF162040), Color(0xFF0A1428)),
                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                        radius = outerR * 0.545f
                    ),
                    radius = outerR * 0.545f
                )

                // Label ring — subtle blue edge
                drawCircle(
                    color = Color(0xFF2A4A88),
                    radius = outerR * 0.545f,
                    style = Stroke(width = 1.5f)
                )

                // Spindle outer ring
                drawCircle(color = Color(0xFF444444), radius = outerR * 0.055f)
                // Spindle highlight
                drawCircle(color = Color(0xFF606060), radius = outerR * 0.040f)
                // Spindle hole
                drawCircle(color = Color(0xFF080808), radius = outerR * 0.028f)
            }

            // Album art circle — centered in the label
            val artSize = size * 0.50f
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
                                colors = listOf(Color(0xFF1E3A6E), Color(0xFF0A1428))
                            )
                        )
                    }
                }
            }
        }
    }
}
