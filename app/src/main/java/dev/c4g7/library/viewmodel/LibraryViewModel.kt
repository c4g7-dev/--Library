package dev.c4g7.library.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.c4g7.library.data.MetadataExtractor
import dev.c4g7.library.data.SecurePreferences
import dev.c4g7.library.data.Track
import dev.c4g7.library.data.ZipExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class LibraryState {
    object Empty : LibraryState()
    object Loading : LibraryState()
    data class Success(val tracks: List<Track>) : LibraryState()
    data class Error(val message: String) : LibraryState()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val securePrefs = SecurePreferences(application)
    private val zipExtractor = ZipExtractor(application)
    private val metadataExtractor = MetadataExtractor(application)
    private val musicDir = File(application.filesDir, "music")

    private val _state = MutableStateFlow<LibraryState>(LibraryState.Empty)
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private val _trackProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val trackProgress: StateFlow<Map<String, Float>> = _trackProgress.asStateFlow()

    val savedZipUri: String get() = securePrefs.zipUriString
    val savedPassword: String get() = securePrefs.zipPassword

    init {
        if (securePrefs.zipUriString.isNotEmpty() && securePrefs.zipPassword.isNotEmpty()) {
            loadFromSaved()
        }
    }

    fun loadZip(zipUri: Uri, password: String) {
        securePrefs.zipUriString = zipUri.toString()
        securePrefs.zipPassword = password
        loadFromUri(zipUri, password)
    }

    private fun loadFromSaved() {
        val uri = Uri.parse(securePrefs.zipUriString)
        loadFromUri(uri, securePrefs.zipPassword)
    }

    private fun loadFromUri(zipUri: Uri, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LibraryState.Loading
            try {
                val files = zipExtractor.extractOpusFiles(zipUri, password, musicDir)
                if (files.isEmpty()) {
                    _state.value = LibraryState.Error("No .opus files found or wrong password")
                    return@launch
                }
                val tracks = files.mapIndexed { index, file ->
                    val meta = metadataExtractor.extractFromFile(file)
                    Track(
                        id = file.absolutePath,
                        title = meta.title,
                        artist = meta.artist,
                        album = meta.album,
                        uri = Uri.fromFile(file),
                        coverArtBytes = meta.coverArtBytes,
                        durationMs = meta.durationMs
                    )
                }.sortedBy { it.title }
                _state.value = LibraryState.Success(tracks)
            } catch (e: Exception) {
                _state.value = LibraryState.Error(e.message ?: "Failed to load ZIP")
            }
        }
    }

    fun updateTrackProgress(trackId: String, fraction: Float) {
        _trackProgress.value = _trackProgress.value + (trackId to fraction)
    }
}
