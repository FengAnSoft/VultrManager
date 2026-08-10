package com.example.vultrmanager.ui.instances

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vultrmanager.data.VultrRepository
import com.example.vultrmanager.data.remote.model.Instance
import com.example.vultrmanager.ui.components.StatusChip
import com.example.vultrmanager.ui.components.statusCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceListScreen(
    repository: VultrRepository,
    onOpenSettings: () -> Unit,
    onOpenAccount: () -> Unit,
    onInstanceClick: (String) -> Unit,
    onCreateInstance: () -> Unit,
    viewModel: InstanceListViewModel = viewModel(factory = InstanceListViewModelFactory(repository))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadInstances() }

    LaunchedEffect(uiState.error, uiState.needsApiKey) {
        if (uiState.error != null && !uiState.needsApiKey) {
            snackbarHostState.showSnackbar(uiState.error!!)
        }
    }

    val visibleInstances = remember(
        uiState.instances, uiState.searchQuery, uiState.statusFilter, uiState.sortOrder
    ) {
        applyFilters(
            instances = uiState.instances,
            query = uiState.searchQuery,
            statusFilter = uiState.statusFilter,
            sortOrder = uiState.sortOrder
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vultr 实例") },
                actions = {
                    IconButton(onClick = { viewModel.loadInstances() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = onOpenAccount) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "账户与账单")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置 API Key")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateInstance) {
                Icon(Icons.Default.Add, contentDescription = "创建实例")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.instances.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.needsApiKey -> {
                    ApiKeyPrompt(onOpenSettings = onOpenSettings)
                }
                uiState.instances.isEmpty() -> {
                    EmptyState(onRetry = { viewModel.loadInstances() })
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search box
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("搜索名称 / IP / 区域") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // Status filter chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = listOf(
                                null to "全部",
                                "running" to "运行中",
                                "stopped" to "已关机",
                                "pending" to "部署中"
                            )
                            filters.forEach { (category, label) ->
                                FilterChip(
                                    selected = uiState.statusFilter == category,
                                    onClick = { viewModel.onStatusFilterChange(category) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        // Sort row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(Icons.Default.Sort, contentDescription = "排序")
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("名称 (A→Z)") },
                                    onClick = {
                                        viewModel.onSortOrderChange(InstanceSortOrder.NAME_ASC)
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("名称 (Z→A)") },
                                    onClick = {
                                        viewModel.onSortOrderChange(InstanceSortOrder.NAME_DESC)
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("状态") },
                                    onClick = {
                                        viewModel.onSortOrderChange(InstanceSortOrder.STATUS)
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("创建时间 (新→旧)") },
                                    onClick = {
                                        viewModel.onSortOrderChange(InstanceSortOrder.CREATED)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }

                        if (visibleInstances.isEmpty()) {
                            NoMatchState(onClear = {
                                viewModel.onSearchQueryChange("")
                                viewModel.onStatusFilterChange(null)
                            })
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(visibleInstances, key = { it.id }) { instance ->
                                    InstanceCard(
                                        instance = instance,
                                        onClick = { onInstanceClick(instance.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Applies the active search query, status filter and sort order to the full list.
 * Pure function so the ViewModel stays the single source of truth for raw data.
 */
private fun applyFilters(
    instances: List<Instance>,
    query: String,
    statusFilter: String?,
    sortOrder: InstanceSortOrder
): List<Instance> {
    val q = query.trim().lowercase()
    val filtered = instances.filter { inst ->
        val matchesQuery = q.isEmpty() ||
            inst.displayName.lowercase().contains(q) ||
            (inst.mainIp ?: "").contains(q) ||
            (inst.region ?: "").lowercase().contains(q)
        val matchesStatus = statusFilter == null || statusCategory(inst.status) == statusFilter
        matchesQuery && matchesStatus
    }
    return when (sortOrder) {
        InstanceSortOrder.NAME_ASC -> filtered.sortedBy { it.displayName.lowercase() }
        InstanceSortOrder.NAME_DESC -> filtered.sortedByDescending { it.displayName.lowercase() }
        InstanceSortOrder.STATUS -> filtered.sortedBy { statusRank(it.status) }
        InstanceSortOrder.CREATED -> filtered.sortedByDescending { it.createdAt ?: "" }
    }
}

/** Numeric rank for status ordering: running first, then stopped, pending, other. */
private fun statusRank(status: String?): Int = when (statusCategory(status)) {
    "running" -> 0
    "stopped" -> 1
    "pending" -> 2
    else -> 3
}

@Composable
private fun InstanceCard(instance: Instance, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = instance.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(status = instance.status)
            }
            Text(
                text = "IP：${instance.mainIp ?: "—"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            if (!instance.region.isNullOrBlank()) {
                Text(
                    text = "区域：${instance.region}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ApiKeyPrompt(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text("尚未配置 API Key", style = MaterialTheme.typography.titleMedium)
        Text(
            "请在设置中填写你的 Vultr API Key 以加载云服务器列表。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onOpenSettings) { Text("前往设置") }
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Cloud,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text("没有可用的实例", style = MaterialTheme.typography.titleMedium)
        Text(
            "当前账户下没有任何云服务器。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onRetry) { Text("刷新") }
    }
}

@Composable
private fun NoMatchState(onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("没有匹配的实例", style = MaterialTheme.typography.titleMedium)
        Text(
            "尝试调整搜索关键词或状态筛选。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onClear) { Text("清除筛选") }
    }
}

private class InstanceListViewModelFactory(
    private val repository: VultrRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InstanceListViewModel(repository) as T
    }
}
