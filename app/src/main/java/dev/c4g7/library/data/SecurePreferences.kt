package dev.c4g7.library.data

import android.content.Context

class SecurePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE)

    var zipUriString: String
        get() = prefs.getString(KEY_ZIP_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ZIP_URI, value).apply()

    companion object {
        private const val KEY_ZIP_URI = "zip_uri"
    }
}
