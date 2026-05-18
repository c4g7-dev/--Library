package dev.c4g7.library.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.c4g7.library.data.Track
import dev.c4g7.library.ui.theme.AccentRed
import dev.c4g7.library.ui.theme.TextSecondary

@Composable
fun TrackCard(
    track: Track,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentlyPlaying: Boolean = false,
    isActivePlaying: Boolean = false
) {
    Column(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .then(
                    if (isCurrentlyPlaying) Modifier.border(
                        width = 2.dp,
                        color = AccentRed.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(14.dp)
                    ) else Modifier
                )
        ) {
            if (track.coverArtBytes != null) {
                AsyncImage(
                    model = track.coverArtBytes,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val colors = gradientColorsForId(track.id)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(colors)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = track.title.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Animated equalizer icon when this track is active
            if (isCurrentlyPlaying) {
                EqualizerBars(
                    isPlaying = isActivePlaying,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                )
            }

            // Progress ring bottom-right
            ProgressRing(
                progress = progress,
                modifier = Modifier
                    .size(34.dp)
                    .align(Alignment.BottomEnd)
                    .padding(5.dp),
                strokeWidth = 4.5f
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = track.title,
            fontSize = 12.sp,
            color = if (isCurrentlyPlaying) Color.White else Color(0xFFCCCCCC),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Text(
            text = track.artist,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun EqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse),
        label = "b3"
    )

    Box(
        modifier = modifier
            .background(Color(0x99000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(bar1, bar2, bar3).forEach { h ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height((if (isPlaying) h else 4f).dp)
                        .background(AccentRed, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

private fun gradientColorsForId(id: String): List<Color> {
    val pairs = listOf(
        listOf(Color(0xFF1A1A2E), Color(0xFF16213E)),
        listOf(Color(0xFF2D1B69), Color(0xFF11998E)),
        listOf(Color(0xFF1A1A1A), Color(0xFF3D3D3D)),
        listOf(Color(0xFF0F3460), Color(0xFF533483)),
        listOf(Color(0xFF4A0E0E), Color(0xFF2D1B00)),
    )
    return pairs[id.hashCode().and(0x7FFFFFFF) % pairs.size]
}
