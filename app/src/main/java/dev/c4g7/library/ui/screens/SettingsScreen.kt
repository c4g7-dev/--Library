package dev.c4g7.library.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.c4g7.library.ui.theme.AccentRed
import dev.c4g7.library.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    libraryViewModel: LibraryViewModel,
    onLoad: () -> Unit
) {
    var zipUri by remember { mutableStateOf(libraryViewModel.savedZipUri) }
    var password by remember { mutableStateOf(libraryViewModel.savedPassword) }
    var passwordVisible by remember { mutableStateOf(false) }
    var gapless by remember { mutableStateOf(false) }

    val zipPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) zipUri = uri.toString()
    }

    Scaffold(
        containerColor = Color(0xFF000000),
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineLarge, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF000000))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item { SettingsSectionHeader("Source") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = zipUri,
                            onValueChange = { zipUri = it },
                            label = { Text("ZIP file path / URI", color = Color(0xFF888888)) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    zipPicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                                }) {
                                    Icon(Icons.Filled.FolderZip, contentDescription = "Pick ZIP", tint = Color(0xFF888888))
                                }
                            },
                            colors = outlinedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Decrypt passphrase", color = Color(0xFF888888)) },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF888888))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null,
                                        tint = Color(0xFF888888)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = outlinedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (zipUri.isNotEmpty() && password.isNotEmpty()) {
                                    libraryViewModel.loadZip(Uri.parse(zipUri), password)
                                    onLoad()
                                }
                            },
                            enabled = zipUri.isNotEmpty() && password.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentRed,
                                disabledContainerColor = Color(0xFF2A2A2A)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unlock & Load Library", color = Color.White)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item { SettingsSectionHeader("Playback") }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Gapless Playback", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Text("No silence between tracks", style = MaterialTheme.typography.labelSmall, color = Color(0xFF666666))
                        }
                        Switch(
                            checked = gapless,
                            onCheckedChange = { gapless = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentRed,
                                uncheckedThumbColor = Color(0xFF666666),
                                uncheckedTrackColor = Color(0xFF222222)
                            )
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item { SettingsSectionHeader("About") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingsAboutRow("App", "Library")
                        SettingsAboutRow("Format", ".opus in AES-256 ZIP")
                        SettingsAboutRow("Version", "1.0.0")
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF555555),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0D0D0D),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SettingsAboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888888))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = AccentRed,
    unfocusedBorderColor = Color(0xFF333333),
    cursorColor = AccentRed,
    focusedLabelColor = AccentRed,
)
