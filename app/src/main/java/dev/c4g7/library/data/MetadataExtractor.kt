package dev.c4g7.library.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

class MetadataExtractor(private val context: Context) {

    fun extractFromFile(file: File): TrackMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            TrackMetadata(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: file.nameWithoutExtension,
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: "Unknown Artist",
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    ?: "Unknown Album",
                durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L,
                coverArtBytes = retriever.embeddedPicture
            )
        } catch (e: Exception) {
            TrackMetadata(title = file.nameWithoutExtension)
        } finally {
            retriever.release()
        }
    }

    data class TrackMetadata(
        val title: String = "",
        val artist: String = "Unknown Artist",
        val album: String = "Unknown Album",
        val durationMs: Long = 0L,
        val coverArtBytes: ByteArray? = null
    )
}
