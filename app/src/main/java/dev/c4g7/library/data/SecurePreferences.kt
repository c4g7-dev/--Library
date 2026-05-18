package dev.c4g7.library.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferences(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "library_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var zipPassword: String
        get() = prefs.getString(KEY_ZIP_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ZIP_PASSWORD, value).apply()

    var zipUriString: String
        get() = prefs.getString(KEY_ZIP_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ZIP_URI, value).apply()

    companion object {
        private const val KEY_ZIP_PASSWORD = "zip_password"
        private const val KEY_ZIP_URI = "zip_uri"
    }
}
