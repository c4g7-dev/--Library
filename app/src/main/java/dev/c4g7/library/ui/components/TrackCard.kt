package dev.c4g7.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.c4g7.library.data.Track
import dev.c4g7.library.ui.theme.TextSecondary

@Composable
fun TrackCard(
    track: Track,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
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
                        .background(
                            Brush.linearGradient(colors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = track.title.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            ProgressRing(
                progress = progress,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
                strokeWidth = 5f
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
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
