package dev.c4g7.library.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.c4g7.library.BuildConfig
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

    init {
        if (securePrefs.zipUriString.isNotEmpty()) {
            loadFromSaved()
        }
    }

    fun loadZip(zipUri: Uri) {
        securePrefs.zipUriString = zipUri.toString()
        loadFromUri(zipUri)
    }

    private fun loadFromSaved() {
        loadFromUri(Uri.parse(securePrefs.zipUriString))
    }

    private fun loadFromUri(zipUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LibraryState.Loading
            try {
                val files = zipExtractor.extractOpusFiles(zipUri, BuildConfig.ZIP_PASSWORD, musicDir)
                if (files.isEmpty()) {
                    _state.value = LibraryState.Error("No .opus files found in archive")
                    return@launch
                }
                val tracks = files.map { file ->
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
            } catch (e: SecurityException) {
                securePrefs.zipUriString = ""
                _state.value = LibraryState.Error("File permission expired — re-select the ZIP in Settings")
            } catch (e: Exception) {
                _state.value = LibraryState.Error(e.message ?: "Failed to load archive")
            }
        }
    }

    fun updateTrackProgress(trackId: String, fraction: Float) {
        _trackProgress.value = _trackProgress.value + (trackId to fraction)
    }
}
