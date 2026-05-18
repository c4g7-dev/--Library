package dev.c4g7.library.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.c4g7.library.ui.components.TrackCard
import dev.c4g7.library.ui.i18n.LocalStrings
import dev.c4g7.library.ui.theme.AccentBlue
import dev.c4g7.library.viewmodel.LibraryState
import dev.c4g7.library.viewmodel.LibraryViewModel
import dev.c4g7.library.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    onTrackClick: () -> Unit
) {
    val state by libraryViewModel.state.collectAsState()
    val trackProgress by libraryViewModel.trackProgress.collectAsState()
    val playerState by playerViewModel.state.collectAsState()
    val strings = LocalStrings.current

    val zipPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) libraryViewModel.loadZip(uri)
    }

    // Sync player progress back to library
    LaunchedEffect(playerState.progressFraction, playerState.currentTrack) {
        playerState.currentTrack?.let { track ->
            libraryViewModel.updateTrackProgress(track.id, playerState.progressFraction)
        }
    }

    Scaffold(
        containerColor = Color(0xFF000000),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.appTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            zipPicker.launch(
                                arrayOf("application/zip", "application/octet-stream", "*/*")
                            )
                        }
                    ) {
                        Icon(
                            Icons.Filled.FolderZip,
                            contentDescription = strings.openZip,
                            tint = Color(0xFF888888)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000000))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is LibraryState.Empty -> EmptyState(
                    label = strings.noLibraryLoaded,
                    desc = strings.openZipDesc,
                    buttonLabel = strings.openZip,
                    onPickZip = {
                        zipPicker.launch(
                            arrayOf("application/zip", "application/octet-stream", "*/*")
                        )
                    }
                )

                is LibraryState.Loading -> CircularProgressIndicator(color = AccentBlue)

                is LibraryState.Error -> ErrorState(
                    message = s.message,
                    buttonLabel = strings.tryAgain,
                    onRetry = {
                        zipPicker.launch(
                            arrayOf("application/zip", "application/octet-stream", "*/*")
                        )
                    }
                )

                is LibraryState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.tracks, key = { it.id }) { track ->
                            val isCurrentTrack = playerState.currentTrack?.id == track.id
                            TrackCard(
                                track = track,
                                progress = trackProgress[track.id] ?: 0f,
                                isCurrentlyPlaying = isCurrentTrack,
                                isActivePlaying = isCurrentTrack && playerState.isPlaying,
                                onClick = {
                                    if (!isCurrentTrack) {
                                        playerViewModel.playQueue(
                                            s.tracks,
                                            s.tracks.indexOf(track)
                                        )
                                    }
                                    onTrackClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    label: String,
    desc: String,
    buttonLabel: String,
    onPickZip: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Filled.FolderZip,
            contentDescription = null,
            tint = Color(0xFF2A2A2A),
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = Color(0xFF555555))
        Spacer(Modifier.height(8.dp))
        Text(
            desc,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF3A3A3A),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        FilledTonalButton(
            onClick = onPickZip,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Text(buttonLabel, color = Color.White)
        }
    }
}

@Composable
private fun ErrorState(message: String, buttonLabel: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Filled.MusicOff,
            contentDescription = null,
            tint = AccentBlue.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue)
        ) {
            Text(buttonLabel, color = AccentBlue)
        }
    }
}
