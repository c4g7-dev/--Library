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
        targetValue = if (isPlaying) 1f else 0.3f,
        animationSpec = tween(600, easing = EaseInOutQuad),
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

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = (36 * glowAlpha).dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFFEF5350).copy(alpha = 0.35f * glowAlpha),
                    spotColor = Color(0xFFEF5350).copy(alpha = 0.5f * glowAlpha)
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

                // Base vinyl body
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF111111), Color(0xFF080808)),
                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                        radius = outerR
                    ),
                    radius = outerR
                )

                // Groove rings — two interleaved shades for depth
                var r = outerR * 0.97f
                var toggle = false
                while (r > outerR * 0.565f) {
                    drawCircle(
                        color = if (toggle) Color(0xFF1C1C1C) else Color(0xFF161616),
                        radius = r,
                        style = Stroke(width = 1.5f)
                    )
                    r -= 4.2f
                    toggle = !toggle
                }

                // Outer edge highlight ring
                drawCircle(
                    color = Color(0xFF2A2A2A),
                    radius = outerR - 1.5f,
                    style = Stroke(width = 2.5f)
                )

                // Shine arc (upper-left quadrant, very subtle)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.04f),
                            Color.White.copy(alpha = 0.07f),
                            Color.White.copy(alpha = 0.04f),
                            Color.White.copy(alpha = 0f),
                        ),
                        center = androidx.compose.ui.geometry.Offset(cx, cy)
                    ),
                    startAngle = 200f,
                    sweepAngle = 100f,
                    useCenter = false,
                    style = Stroke(width = outerR * 0.06f)
                )

                // Label circle background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E1E1E), Color(0xFF141414)),
                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                        radius = outerR * 0.54f
                    ),
                    radius = outerR * 0.54f
                )

                // Label edge ring
                drawCircle(
                    color = Color(0xFF282828),
                    radius = outerR * 0.54f,
                    style = Stroke(width = 1f)
                )

                // Center spindle — outer ring
                drawCircle(color = Color(0xFF3A3A3A), radius = outerR * 0.055f)
                // Center spindle — hole
                drawCircle(color = Color(0xFF000000), radius = outerR * 0.032f)
            }

            // Album art clipped circle in center
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
                                colors = listOf(Color(0xFF2A2A2A), Color(0xFF080808))
                            )
                        )
                    }
                }
            }
        }
    }
}
