package com.example.vultrmanager.ui.instances

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vultrmanager.data.VultrRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInstanceScreen(
    repository: VultrRepository,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateInstanceViewModel = viewModel(factory = CreateInstanceViewModelFactory(repository))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.created) {
        if (uiState.created) onCreated()
    }

    LaunchedEffect(uiState.submitError) {
        uiState.submitError?.let { snackbarHostState.showSnackbar(it) }
    }

    // Plans that can be deployed in the chosen region (falls back to all if unknown).
    val availablePlans = remember(uiState.plans, uiState.regionId) {
        if (uiState.regionId == null) {
            uiState.plans
        } else {
            uiState.plans.filter { it.locations.isNullOrEmpty() || it.locations.contains(uiState.regionId) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建实例") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoadingOptions) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                Text("正在加载可选项（区域 / 套餐 / 系统）…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (uiState.optionsError != null) {
                Text(
                    uiState.optionsError ?: "加载失败",
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = viewModel::loadOptions) { Text("重试") }
            } else {
                // Region (required)
                DropdownSelector(
                    label = "区域 *",
                    options = uiState.regions.map { it.id to regionLabel(it) },
                    selectedValue = uiState.regionId,
                    onSelected = viewModel::onRegionChange,
                    placeholder = "选择部署区域"
                )

                // Plan (required)
                DropdownSelector(
                    label = "套餐 *",
                    options = availablePlans.map { it.id to planLabel(it) },
                    selectedValue = uiState.planId,
                    onSelected = viewModel::onPlanChange,
                    enabled = uiState.regionId != null,
                    placeholder = if (uiState.regionId == null) "请先选择区域" else "选择套餐",
                    supportingText = selectedPlanSummary(uiState.plans, uiState.planId)
                )

                // OS (required)
                DropdownSelector(
                    label = "操作系统 *",
                    options = uiState.oses.map { it.id.toString() to (it.name ?: it.id.toString()) },
                    selectedValue = uiState.osId?.toString(),
                    onSelected = { viewModel.onOsChange(it.toInt()) },
                    placeholder = "选择操作系统"
                )

                // Label
                OutlinedTextField(
                    value = uiState.label,
                    onValueChange = viewModel::onLabelChange,
                    label = { Text("标签（可选）") },
                    placeholder = { Text("便于辨识的名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Hostname
                OutlinedTextField(
                    value = uiState.hostname,
                    onValueChange = viewModel::onHostnameChange,
                    label = { Text("主机名（可选）") },
                    placeholder = { Text("例如 server01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // SSH key (optional)
                DropdownSelector(
                    label = "SSH 密钥（可选）",
                    options = listOf("none" to "不使用") +
                        uiState.sshKeys.map { it.id to (it.name ?: it.id) },
                    selectedValue = uiState.sshKeyId ?: "none",
                    onSelected = { viewModel.onSshKeyChange(if (it == "none") null else it) }
                )

                // IPv6 toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("启用 IPv6", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = uiState.enableIpv6, onCheckedChange = viewModel::onEnableIpv6Change)
                }

                // Backups
                DropdownSelector(
                    label = "自动备份",
                    options = listOf("disabled" to "关闭", "enabled" to "开启（额外收费）"),
                    selectedValue = uiState.backups,
                    onSelected = viewModel::onBackupsChange
                )

                // Tag
                OutlinedTextField(
                    value = uiState.tag,
                    onValueChange = viewModel::onTagChange,
                    label = { Text("标签分组（可选）") },
                    placeholder = { Text("例如 production") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = viewModel::submit,
                    enabled = uiState.regionId != null && uiState.planId != null &&
                        uiState.osId != null && !uiState.isCreating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    }
                    Text("创建实例")
                }
            }
        }
    }
}

private fun regionLabel(region: com.example.vultrmanager.data.remote.model.Region): String {
    val city = region.city ?: region.id
    val country = region.country?.uppercase()?.takeIf { it.isNotBlank() }
    val extra = region.continent?.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()
    return if (country != null) "$city ($country)$extra" else "$city$extra"
}

private fun planLabel(plan: com.example.vultrmanager.data.remote.model.Plan): String {
    val vcpu = plan.vcpuCount?.let { "${it} vCPU" }.orEmpty()
    val ram = plan.ram?.let { "${it}MB" }.orEmpty()
    val disk = plan.disk?.let { "${it}GB" }.orEmpty()
    val spec = listOf(vcpu, ram, disk).filter { it.isNotBlank() }.joinToString(" · ")
    return if (spec.isNotBlank()) "${plan.id}  ($spec)" else plan.id
}

private fun selectedPlanSummary(
    plans: List<com.example.vultrmanager.data.remote.model.Plan>,
    planId: String?
): String? {
    if (planId == null) return null
    val plan = plans.firstOrNull { it.id == planId } ?: return null
    val price = plan.pricePerMonth?.let { "${'$'}$it/月" }.orEmpty()
    return price.takeIf { it.isNotBlank() }
}

/**
 * Read-only dropdown selector. Tapping the field opens a [DropdownMenu] listing the options.
 * `options` are (value, display) pairs; `selectedValue` is the currently chosen value (or null).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: List<Pair<String, String>>,
    selectedValue: String?,
    onSelected: (String) -> Unit,
    enabled: Boolean = true,
    placeholder: String? = null,
    supportingText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.firstOrNull { it.first == selectedValue }?.second ?: placeholder ?: "请选择"

    Box {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            supportingText = supportingText?.let { { Text(it) } }
        )
        // Transparent click surface covering the field, so taps always open the menu.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

private class CreateInstanceViewModelFactory(
    private val repository: VultrRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateInstanceViewModel(repository) as T
    }
}
