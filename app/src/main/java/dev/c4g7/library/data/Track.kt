package dev.c4g7.library.data

import android.net.Uri

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val coverArtBytes: ByteArray? = null,
    val durationMs: Long = 0L,
    val progressFraction: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Track
        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
}
