package dev.c4g7.library.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.c4g7.library.ui.components.VinylDisk
import dev.c4g7.library.ui.theme.AccentRed
import dev.c4g7.library.ui.theme.TextSecondary
import dev.c4g7.library.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(playerViewModel: PlayerViewModel) {
    val state by playerViewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(16.dp))

        // Vinyl disk
        VinylDisk(
            coverArtBytes = state.currentTrack?.coverArtBytes,
            isPlaying = state.isPlaying,
            size = 290.dp
        )

        // Track info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.currentTrack?.title ?: "Nothing playing",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = state.currentTrack?.artist ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Progress
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = state.progressFraction,
                onValueChange = { playerViewModel.seekTo(it) },
                colors = SliderDefaults.colors(
                    thumbColor = AccentRed,
                    activeTrackColor = AccentRed,
                    inactiveTrackColor = Color(0xFF2A2A2A)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(state.positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = formatMs(state.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { playerViewModel.skipPrev() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AccentRed),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { playerViewModel.togglePlayPause() }) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            IconButton(
                onClick = { playerViewModel.skipNext() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
