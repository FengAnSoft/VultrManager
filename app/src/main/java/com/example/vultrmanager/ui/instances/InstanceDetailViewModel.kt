package com.example.vultrmanager.ui.instances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vultrmanager.data.VultrApiException
import com.example.vultrmanager.data.VultrRepository
import com.example.vultrmanager.data.remote.model.BandwidthData
import com.example.vultrmanager.data.remote.model.Instance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InstanceDetailUiState(
    val isLoading: Boolean = false,
    val instance: Instance? = null,
    /** A power action (start/halt/reboot) is currently in flight. */
    val actionInProgress: Boolean = false,
    val actionError: String? = null,
    /** Transient success message (e.g. "开机指令已发送"). */
    val message: String? = null,
    val notFound: Boolean = false,
    /** Bandwidth samples for the current month. */
    val bandwidth: BandwidthData? = null,
    val bandwidthLoading: Boolean = false,
    val bandwidthError: String? = null,
    /** True while a destroy (DELETE) is in flight. */
    val isDeleting: Boolean = false,
    /** True once the instance has been destroyed successfully. */
    val deleted: Boolean = false
)

class InstanceDetailViewModel(private val repository: VultrRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InstanceDetailUiState())
    val uiState: StateFlow<InstanceDetailUiState> = _uiState.asStateFlow()

    fun loadInstance(id: String) {
        _uiState.update { it.copy(isLoading = true, actionError = null, deleted = false) }
        viewModelScope.launch {
            repository.getInstance(id)
                .onSuccess { instance ->
                    _uiState.update { it.copy(isLoading = false, instance = instance, notFound = false) }
                    loadBandwidth(instance.id)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notFound = (e is VultrApiException && e.code == 404),
                            actionError = e.message ?: "加载失败"
                        )
                    }
                }
        }
    }

    fun loadBandwidth(id: String) {
        _uiState.update { it.copy(bandwidthLoading = true, bandwidthError = null) }
        viewModelScope.launch {
            repository.getInstanceBandwidth(id)
                .onSuccess { bw -> _uiState.update { it.copy(bandwidthLoading = false, bandwidth = bw) } }
                .onFailure { e -> _uiState.update { it.copy(bandwidthLoading = false, bandwidthError = e.message) } }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null, actionError = null) }

    private fun performAction(
        id: String,
        action: suspend (String) -> Result<Unit>,
        successMessage: String,
        errorPrefix: String
    ) {
        _uiState.update { it.copy(actionInProgress = true, actionError = null, message = null) }
        viewModelScope.launch {
            action(id)
                .onSuccess {
                    _uiState.update { it.copy(actionInProgress = false, message = successMessage) }
                    loadInstance(id)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(actionInProgress = false, actionError = "$errorPrefix：${e.message}")
                    }
                }
        }
    }

    fun start(id: String) = performAction(id, repository::startInstance, "开机指令已发送", "开机失败")
    fun halt(id: String) = performAction(id, repository::haltInstance, "关机指令已发送", "关机失败")
    fun reboot(id: String) = performAction(id, repository::rebootInstance, "重启指令已发送", "重启失败")

    fun delete(id: String) {
        _uiState.update { it.copy(isDeleting = true, actionError = null) }
        viewModelScope.launch {
            repository.deleteInstance(id)
                .onSuccess { _uiState.update { it.copy(isDeleting = false, deleted = true) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isDeleting = false, actionError = "删除失败：${e.message}") }
                }
        }
    }
}
