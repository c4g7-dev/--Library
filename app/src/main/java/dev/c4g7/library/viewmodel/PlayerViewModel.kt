package dev.c4g7.library.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dev.c4g7.library.data.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progressFraction: Float = 0f,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
                if (isPlaying) startProgressUpdates() else stopProgressUpdates()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val idx = this@apply.currentMediaItemIndex
                val track = _state.value.queue.getOrNull(idx)
                _state.value = _state.value.copy(
                    currentTrack = track,
                    currentIndex = idx,
                    progressFraction = 0f,
                    positionMs = 0L,
                    durationMs = this@apply.duration.coerceAtLeast(0L)
                )
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _state.value = _state.value.copy(
                        durationMs = this@apply.duration.coerceAtLeast(0L)
                    )
                }
            }
        })
    }

    private var progressJob: Job? = null

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                val pos = player.currentPosition.coerceAtLeast(0L)
                val dur = player.duration.coerceAtLeast(1L)
                _state.value = _state.value.copy(
                    positionMs = pos,
                    durationMs = dur,
                    progressFraction = if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
                )
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        player.clearMediaItems()
        tracks.forEach { player.addMediaItem(MediaItem.fromUri(it.uri)) }
        player.seekToDefaultPosition(startIndex)
        player.prepare()
        player.play()
        _state.value = _state.value.copy(
            queue = tracks,
            currentTrack = tracks.getOrNull(startIndex),
            currentIndex = startIndex
        )
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(fraction: Float) {
        val dur = player.duration.coerceAtLeast(0L)
        player.seekTo((fraction * dur).toLong())
    }

    fun skipNext() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    fun skipPrev() {
        if (player.currentPosition > 3000) player.seekTo(0)
        else if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
