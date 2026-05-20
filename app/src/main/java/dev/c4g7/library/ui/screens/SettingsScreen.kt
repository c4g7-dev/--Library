package dev.c4g7.library.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.c4g7.library.ui.i18n.LocalStrings
import dev.c4g7.library.ui.theme.AccentBlue
import dev.c4g7.library.viewmodel.LibraryState
import dev.c4g7.library.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    libraryViewModel: LibraryViewModel,
    language: String,
    onLanguageChange: (String) -> Unit,
    onLoad: () -> Unit
) {
    val strings = LocalStrings.current
    var zipUri by remember { mutableStateOf(libraryViewModel.savedZipUri) }
    var gapless by remember { mutableStateOf(false) }

    val libraryState by libraryViewModel.state.collectAsState()
    val isLoading = libraryState is LibraryState.Loading

    val zipPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) zipUri = uri.toString()
    }

    Scaffold(
        containerColor = Color(0xFF000000),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.settings,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                },
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
            item { Spacer(Modifier.height(4.dp)) }

            item { SettingsSectionHeader(strings.source) }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = zipUri,
                            onValueChange = { zipUri = it },
                            label = { Text(strings.zipFile, color = Color(0xFF888888)) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        zipPicker.launch(
                                            arrayOf(
                                                "application/zip",
                                                "application/octet-stream",
                                                "*/*"
                                            )
                                        )
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.FolderZip,
                                        contentDescription = "Pick ZIP",
                                        tint = Color(0xFF888888)
                                    )
                                }
                            },
                            colors = textFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (zipUri.isNotEmpty()) {
                                        libraryViewModel.loadZip(Uri.parse(zipUri))
                                        onLoad()
                                    }
                                },
                                enabled = zipUri.isNotEmpty() && !isLoading,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentBlue,
                                    contentColor = Color(0xFF000000),
                                    disabledContainerColor = Color(0xFF1E1E1E),
                                    disabledContentColor = Color(0xFF444444)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(strings.loadLibrary, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            IconButton(
                                onClick = {
                                    zipUri = ""
                                    libraryViewModel.clearLibrary()
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color(0xFF1E1E1E),
                                    contentColor = Color(0xFFCC3333)
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = strings.clearLibrary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(4.dp)) }
            item { SettingsSectionHeader(strings.playback) }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Gapless toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    strings.gaplessPlayback,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    strings.gaplessDesc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF666666)
                                )
                            }
                            Switch(
                                checked = gapless,
                                onCheckedChange = { gapless = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AccentBlue,
                                    uncheckedThumbColor = Color(0xFF666666),
                                    uncheckedTrackColor = Color(0xFF222222)
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFF1A1A1A))

                        // Language toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                strings.language,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier
                                    .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                LanguageChip(
                                    label = "EN",
                                    selected = language == "en",
                                    onClick = { onLanguageChange("en") }
                                )
                                LanguageChip(
                                    label = "DE",
                                    selected = language == "de",
                                    onClick = { onLanguageChange("de") }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(4.dp)) }
            item { SettingsSectionHeader(strings.about) }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SettingsAboutRow("App", strings.appTitle)
                        SettingsAboutRow(strings.format, ".opus · AES-256 ZIP")
                        SettingsAboutRow(strings.version, "1.0.0")
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun LanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) AccentBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color(0xFF000000) else Color(0xFF888888)
        )
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
        shape = RoundedCornerShape(12.dp),
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
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = Color(0xFF333333),
    cursorColor = AccentBlue,
    focusedLabelColor = AccentBlue,
)
