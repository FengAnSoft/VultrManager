package com.example.vultrmanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vultrmanager.data.local.ApiKeyStore
import com.example.vultrmanager.data.VultrRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKey: String = "",
    val hasExistingKey: Boolean = false,
    val isSaving: Boolean = false,
    val status: SettingsStatus = SettingsStatus.Idle,
    val message: String? = null
)

sealed interface SettingsStatus {
    data object Idle : SettingsStatus
    data object Success : SettingsStatus
    data object Error : SettingsStatus
}

class SettingsViewModel(
    private val apiKeyStore: ApiKeyStore,
    private val repository: VultrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(hasExistingKey = apiKeyStore.hasApiKey()))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onApiKeyChange(value: String) = _uiState.update { it.copy(apiKey = value) }

    /** Persists the key and, if a connection can be verified, invokes [onVerified]. */
    fun saveAndVerify(onVerified: () -> Unit) {
        val key = _uiState.value.apiKey.trim()
        if (key.isBlank()) {
            _uiState.update { it.copy(status = SettingsStatus.Error, message = "API Key 不能为空。") }
            return
        }

        try {
            apiKeyStore.saveApiKey(key)
        } catch (e: SecurityException) {
            _uiState.update { it.copy(status = SettingsStatus.Error, message = e.message) }
            return
        }

        _uiState.update { it.copy(hasExistingKey = true, isSaving = true, status = SettingsStatus.Idle, message = null) }
        viewModelScope.launch {
            repository.listInstances()
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, status = SettingsStatus.Success, message = "API Key 有效，连接成功。") }
                    onVerified()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            status = SettingsStatus.Error,
                            message = "已保存，但连接验证失败：${e.message}"
                        )
                    }
                }
        }
    }
}
