package com.example.vultrmanager.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the user's dark-mode preference in plain (non-encrypted) SharedPreferences.
 * The theme choice is not sensitive, so an EncryptedSharedPreferences is unnecessary.
 */
class ThemeStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("vultr_prefs", Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK, false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DARK, enabled) }
        _darkMode.value = enabled
    }

    companion object {
        private const val KEY_DARK = "dark_mode"
    }
}
