package dev.c4g7.library.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.c4g7.library.ui.components.VinylDisk
import dev.c4g7.library.ui.theme.AccentRed
import dev.c4g7.library.ui.theme.TextSecondary
import dev.c4g7.library.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(playerViewModel: PlayerViewModel) {
    val state by playerViewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Subtle radial ambient glow behind the disk
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentRed.copy(alpha = if (state.isPlaying) 0.07f else 0.03f),
                            Color.Transparent
                        ),
                        radius = 700f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // "NOW PLAYING" label
            Text(
                text = "NOW PLAYING",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp,
                color = Color(0xFF555555)
            )

            Spacer(Modifier.height(28.dp))

            // Vinyl disk
            VinylDisk(
                coverArtBytes = state.currentTrack?.coverArtBytes,
                isPlaying = state.isPlaying,
                size = 294.dp
            )

            Spacer(Modifier.height(36.dp))

            // Track title
            Text(
                text = state.currentTrack?.title ?: "Nothing playing",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.3).sp
            )

            Spacer(Modifier.height(6.dp))

            // Artist
            Text(
                text = state.currentTrack?.artist ?: "",
                fontSize = 14.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Album (if different from artist)
            if (state.currentTrack?.album?.isNotEmpty() == true &&
                state.currentTrack?.album != state.currentTrack?.artist
            ) {
                Text(
                    text = state.currentTrack?.album ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFF444444),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(32.dp))

            // Progress bar + timestamps
            var isDragging by remember { mutableStateOf(false) }
            var dragValue by remember { mutableStateOf(0f) }

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (isDragging) dragValue else state.progressFraction,
                    onValueChange = { v ->
                        isDragging = true
                        dragValue = v
                    },
                    onValueChangeFinished = {
                        playerViewModel.seekTo(dragValue)
                        isDragging = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = AccentRed,
                        inactiveTrackColor = Color(0xFF262626)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatMs(state.positionMs), fontSize = 11.sp, color = Color(0xFF555555))
                    Text(formatMs(state.durationMs), fontSize = 11.sp, color = Color(0xFF555555))
                }
            }

            Spacer(Modifier.height(28.dp))

            // Controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle (decorative)
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = { playerViewModel.skipPrev() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Play / Pause — main button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(AccentRed),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { playerViewModel.togglePlayPause() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = { playerViewModel.skipNext() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Repeat (decorative)
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
