package com.example.vultrmanager.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure, at-rest storage for the user's Vultr API key.
 *
 * The key is encrypted with Android Jetpack Security's [EncryptedSharedPreferences],
 * which is backed by the Android Keystore (AES-256-GCM). The plaintext API key is
 * NEVER hardcoded and NEVER stored in plain SharedPreferences / strings / assets.
 *
 * If the device cannot provide a Keystore-backed key (e.g. no lock screen on older
 * Android versions) the operations fail with a [SecurityException] that the UI surfaces.
 */
class ApiKeyStore(context: Context) {

    private val appContext = context.applicationContext

    // Initialized lazily so constructing this class never crashes the app at startup.
    private val masterKey by lazy {
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Returns the stored API key, or null if none is set. */
    fun getApiKey(): String? =
        runCatching { prefs.getString(KEY_API_TOKEN, null) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }

    /** True when a non-blank API key is currently stored. */
    fun hasApiKey(): Boolean = getApiKey() != null

    /** Persists the API key (trimmed). Throws [SecurityException] if encryption is unavailable. */
    fun saveApiKey(apiKey: String) {
        try {
            prefs.edit().putString(KEY_API_TOKEN, apiKey.trim()).apply()
        } catch (e: Exception) {
            throw SecurityException("无法安全保存 API Key，请确认设备已设置锁屏（PIN/密码/图案）。", e)
        }
    }

    /** Removes the stored API key. */
    fun clearApiKey() {
        runCatching { prefs.edit().remove(KEY_API_TOKEN).apply() }
    }

    companion object {
        private const val PREFS_FILE = "vultr_secure_prefs"
        private const val KEY_API_TOKEN = "vultr_api_token"
    }
}
