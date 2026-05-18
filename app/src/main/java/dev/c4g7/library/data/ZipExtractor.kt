package dev.c4g7.library.data

import android.content.Context
import android.net.Uri
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.FileHeader
import java.io.File
import java.io.InputStream

class ZipExtractor(private val context: Context) {

    fun extractOpusFiles(zipUri: Uri, password: String, destDir: File): List<File> {
        destDir.deleteRecursively()
        destDir.mkdirs()

        val tempZip = File(context.cacheDir, "temp_library.zip")
        context.contentResolver.openInputStream(zipUri)?.use { input ->
            tempZip.outputStream().use { output -> input.copyTo(output) }
        } ?: return emptyList()

        return try {
            val zipFile = ZipFile(tempZip, password.toCharArray())
            val opusHeaders: List<FileHeader> = zipFile.fileHeaders
                .filter { it.fileName.endsWith(".opus", ignoreCase = true) }

            opusHeaders.mapNotNull { header ->
                val outFile = File(destDir, header.fileName.replace("/", "_"))
                zipFile.extractFile(header, destDir.absolutePath, outFile.name)
                if (outFile.exists()) outFile else null
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            tempZip.delete()
        }
    }
}
