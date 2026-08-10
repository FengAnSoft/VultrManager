package com.example.vultrmanager.ui.instances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vultrmanager.data.VultrAuthException
import com.example.vultrmanager.data.VultrRepository
import com.example.vultrmanager.data.remote.model.Os
import com.example.vultrmanager.data.remote.model.Plan
import com.example.vultrmanager.data.remote.model.Region
import com.example.vultrmanager.data.remote.model.SshKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the create-instance form: loads the deployable regions / plans / OS / SSH keys
 * and submits [CreateInstanceRequest] once the required fields are chosen.
 */
data class CreateInstanceUiState(
    val isLoadingOptions: Boolean = false,
    val regions: List<Region> = emptyList(),
    val plans: List<Plan> = emptyList(),
    val oses: List<Os> = emptyList(),
    val sshKeys: List<SshKey> = emptyList(),
    val optionsError: String? = null,

    // Form fields
    val regionId: String? = null,
    val planId: String? = null,
    val osId: Int? = null,
    val label: String = "",
    val hostname: String = "",
    val sshKeyId: String? = null,
    val enableIpv6: Boolean = false,
    val tag: String = "",
    val backups: String = "disabled", // "enabled" | "disabled"

    val isCreating: Boolean = false,
    val created: Boolean = false,
    val submitError: String? = null
)

class CreateInstanceViewModel(private val repository: VultrRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateInstanceUiState())
    val uiState: StateFlow<CreateInstanceUiState> = _uiState.asStateFlow()

    init {
        loadOptions()
    }

    fun loadOptions() {
        _uiState.update { it.copy(isLoadingOptions = true, optionsError = null) }
        viewModelScope.launch {
            val regions = repository.listRegions()
            val plans = repository.listPlans()
            val oses = repository.listOs()
            val sshKeys = repository.listSshKeys()

            val error = listOf(regions, plans, oses, sshKeys)
                .mapNotNull { it.exceptionOrNull() }
                .firstOrNull()
                ?.let { e ->
                    if (e is VultrAuthException) "API Key 无效或缺失，请在设置中检查。"
                    else e.message ?: "加载可选配置失败"
                }

            _uiState.update {
                it.copy(
                    isLoadingOptions = false,
                    regions = regions.getOrDefault(emptyList()),
                    plans = plans.getOrDefault(emptyList()),
                    oses = oses.getOrDefault(emptyList()),
                    sshKeys = sshKeys.getOrDefault(emptyList()),
                    optionsError = error
                )
            }
        }
    }

    // region form mutators
    fun onRegionChange(id: String) =
        _uiState.update { it.copy(regionId = id, planId = null, osId = null) }

    fun onPlanChange(id: String) = _uiState.update { it.copy(planId = id) }
    fun onOsChange(id: Int) = _uiState.update { it.copy(osId = id) }
    fun onLabelChange(v: String) = _uiState.update { it.copy(label = v) }
    fun onHostnameChange(v: String) = _uiState.update { it.copy(hostname = v) }
    fun onSshKeyChange(id: String?) = _uiState.update { it.copy(sshKeyId = id) }
    fun onEnableIpv6Change(v: Boolean) = _uiState.update { it.copy(enableIpv6 = v) }
    fun onTagChange(v: String) = _uiState.update { it.copy(tag = v) }
    fun onBackupsChange(v: String) = _uiState.update { it.copy(backups = v) }
    // endregion

    fun submit() {
        val s = _uiState.value
        val region = s.regionId
        val plan = s.planId
        val os = s.osId
        if (region == null || plan == null || os == null) {
            _uiState.update { it.copy(submitError = "请选择区域、套餐和操作系统。") }
            return
        }
        _uiState.update { it.copy(isCreating = true, submitError = null) }
        viewModelScope.launch {
            val request = com.example.vultrmanager.data.remote.model.CreateInstanceRequest(
                region = region,
                plan = plan,
                osId = os,
                label = s.label.ifBlank { null },
                hostname = s.hostname.ifBlank { null },
                sshkeyId = s.sshKeyId,
                enableIpv6 = s.enableIpv6,
                tag = s.tag.ifBlank { null },
                backups = s.backups
            )
            repository.createInstance(request)
                .onSuccess {
                    _uiState.update { it.copy(isCreating = false, created = true) }
                }
                .onFailure { e ->
                    val msg = if (e is VultrAuthException) {
                        "API Key 无效或缺失，请在设置中检查。"
                    } else e.message ?: "创建失败"
                    _uiState.update { it.copy(isCreating = false, submitError = msg) }
                }
        }
    }
}
