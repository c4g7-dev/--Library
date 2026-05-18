package dev.c4g7.library.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.c4g7.library.ui.components.VinylDisk
import dev.c4g7.library.ui.i18n.LocalStrings
import dev.c4g7.library.ui.theme.AccentBlue
import dev.c4g7.library.ui.theme.AccentBlueDim
import dev.c4g7.library.ui.theme.TextSecondary
import dev.c4g7.library.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(playerViewModel: PlayerViewModel) {
    val state by playerViewModel.state.collectAsState()
    val strings = LocalStrings.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Subtle blue ambient behind the vinyl
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentBlue.copy(alpha = if (state.isPlaying) 0.06f else 0.025f),
                            Color.Transparent
                        ),
                        radius = 720f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // "NOW PLAYING" label
            Text(
                text = strings.nowPlaying,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp,
                color = Color(0xFF444444)
            )

            Spacer(Modifier.height(24.dp))

            // Vinyl disk
            VinylDisk(
                coverArtBytes = state.currentTrack?.coverArtBytes,
                isPlaying = state.isPlaying,
                size = 292.dp
            )

            Spacer(Modifier.height(28.dp))

            // Track info + controls card — Material 3, squarish corners
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0D0D0D),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = state.currentTrack,
                        transitionSpec = {
                            val dir = playerViewModel.lastSkipDir
                            if (dir >= 0)
                                (slideInHorizontally(tween(350)) { it / 3 } + fadeIn(tween(300)))
                                    .togetherWith(slideOutHorizontally(tween(250)) { -(it / 3) } + fadeOut(tween(200)))
                            else
                                (slideInHorizontally(tween(350)) { -(it / 3) } + fadeIn(tween(300)))
                                    .togetherWith(slideOutHorizontally(tween(250)) { it / 3 } + fadeOut(tween(200)))
                        },
                        label = "trackInfo"
                    ) { track ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = track?.title ?: strings.nothingPlaying,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = (-0.2).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = track?.artist ?: "",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!track?.album.isNullOrEmpty() && track?.album != track?.artist) {
                                Text(
                                    text = track?.album ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFF444444),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider(color = Color(0xFF1A1A1A))

                    Spacer(Modifier.height(14.dp))

                    // Progress — seekable
                    var isDragging by remember { mutableStateOf(false) }
                    var dragValue by remember { mutableStateOf(0f) }

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
                            thumbColor = AccentBlue,
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = Color(0xFF252525)
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

                    Spacer(Modifier.height(16.dp))

                    // Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle (decorative)
                        IconButton(onClick = {}, modifier = Modifier.size(44.dp)) {
                            Icon(
                                Icons.Filled.Shuffle,
                                contentDescription = strings.shuffle,
                                tint = Color(0xFF303030),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Previous — squarish filled icon button
                        FilledTonalIconButton(
                            onClick = { playerViewModel.skipPrev() },
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFF1A1A1A)
                            )
                        ) {
                            Icon(
                                Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Play / Pause — squarish, primary blue
                        FilledIconButton(
                            onClick = { playerViewModel.togglePlayPause() },
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = AccentBlue
                            )
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = Color(0xFF000000),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Next — squarish filled icon button
                        FilledTonalIconButton(
                            onClick = { playerViewModel.skipNext() },
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFF1A1A1A)
                            )
                        ) {
                            Icon(
                                Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Repeat (decorative)
                        IconButton(onClick = {}, modifier = Modifier.size(44.dp)) {
                            Icon(
                                Icons.Filled.Repeat,
                                contentDescription = strings.repeat,
                                tint = Color(0xFF303030),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
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
