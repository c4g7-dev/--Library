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
import dev.c4g7.library.ui.theme.AccentRed
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

    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val zipPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingUri = uri
            showPasswordDialog = true
        }
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
                        "Library",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { zipPicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) }) {
                        Icon(Icons.Filled.FolderZip, contentDescription = "Open ZIP", tint = Color(0xFF888888))
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
                is LibraryState.Empty -> EmptyState(onPickZip = {
                    zipPicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                })
                is LibraryState.Loading -> CircularProgressIndicator(color = AccentRed)
                is LibraryState.Error -> ErrorState(message = s.message, onRetry = {
                    zipPicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                })
                is LibraryState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.tracks, key = { it.id }) { track ->
                            TrackCard(
                                track = track,
                                progress = trackProgress[track.id] ?: 0f,
                                onClick = {
                                    playerViewModel.playQueue(s.tracks, s.tracks.indexOf(track))
                                    onTrackClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPasswordDialog && pendingUri != null) {
        AlertDialog(
            containerColor = Color(0xFF141414),
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("ZIP Password", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Passphrase", color = Color(0xFF888888)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        androidx.compose.ui.text.input.VisualTransformation.None
                    else
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = Color(0xFF333333),
                        cursorColor = AccentRed
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingUri?.let { libraryViewModel.loadZip(it, passwordInput) }
                    showPasswordDialog = false
                    passwordInput = ""
                }) { Text("Unlock", color = AccentRed) }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel", color = Color(0xFF888888))
                }
            }
        )
    }
}

@Composable
private fun EmptyState(onPickZip: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.FolderZip, contentDescription = null, tint = Color(0xFF333333), modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(16.dp))
        Text("No library loaded", style = MaterialTheme.typography.titleMedium, color = Color(0xFF555555))
        Spacer(Modifier.height(8.dp))
        Text("Tap to open a password-protected ZIP\ncontaining .opus files", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF444444), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        FilledTonalButton(onClick = onPickZip, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1A1A1A))) {
            Text("Open ZIP", color = Color.White)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.MusicOff, contentDescription = null, tint = AccentRed.copy(alpha = 0.6f), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888888), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry, border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed)) {
            Text("Try Again", color = AccentRed)
        }
    }
}
