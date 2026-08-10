package com.example.vultrmanager.ui.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.vultrmanager.ui.components.BandwidthChart
import com.example.vultrmanager.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceDetailScreen(
    repository: VultrRepository,
    instanceId: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: InstanceDetailViewModel = viewModel(factory = InstanceDetailViewModelFactory(repository))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(instanceId) { viewModel.loadInstance(instanceId) }

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onDeleted()
    }

    LaunchedEffect(uiState.message, uiState.actionError) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        uiState.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.instance?.displayName ?: "实例详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.instance == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.notFound -> {
                    EmptyDetail(message = "未找到该实例，可能已被删除。", onBack = onBack)
                }
                uiState.instance == null -> {
                    EmptyDetail(message = uiState.actionError ?: "加载失败。", onBack = onBack)
                }
                else -> {
                    val instance = uiState.instance!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PowerActionsCard(
                            instance = instance,
                            actionInProgress = uiState.actionInProgress,
                            onStart = { viewModel.start(instance.id) },
                            onHalt = { viewModel.halt(instance.id) },
                            onReboot = { viewModel.reboot(instance.id) }
                        )
                        BandwidthCard(
                            bandwidth = uiState.bandwidth,
                            isLoading = uiState.bandwidthLoading,
                            error = uiState.bandwidthError
                        )
                        InfoCard(title = "基本信息") {
                            DetailRow("名称", instance.displayName)
                            DetailRow("ID", instance.id)
                            DetailRow("状态", instance.status ?: "—")
                            DetailRow("电源状态", instance.powerStatus ?: "—")
                            DetailRow("标签", instance.tags?.joinToString(", ") ?: instance.tag ?: "—")
                            DetailRow("主机名", instance.hostname ?: "—")
                        }
                        InfoCard(title = "网络") {
                            DetailRow("公网 IP", instance.mainIp ?: "—")
                            DetailRow("内网 IP", instance.internalIp ?: "—")
                            DetailRow("IPv6 已启用", if (instance.enableIpv6 == true) "是" else "否")
                            if (instance.enableIpv6 == true) DetailRow("IPv6 地址", instance.v6MainIp ?: "—")
                        }
                        InfoCard(title = "配置") {
                            DetailRow("区域", instance.region ?: "—")
                            DetailRow("套餐", instance.plan ?: "—")
                            DetailRow("操作系统", instance.os ?: "—")
                            DetailRow("vCPU", instance.vcpuCount?.toString() ?: "—")
                            DetailRow("内存", formatRam(instance.ram))
                            DetailRow("磁盘", instance.disk?.let { "$it GB" } ?: "—")
                            DetailRow("月费用", (instance.monthlyCost ?: instance.cost)?.let { "$$it" } ?: "—")
                            DetailRow("月流量额度", instance.allowedBandwidth?.let { "${it.toLong()} GB" } ?: "—")
                        }
                        InfoCard(title = "其他") {
                            DetailRow("创建时间", instance.createdAt ?: "—")
                            DetailRow("防火墙组", instance.firewallGroupId ?: "—")
                            DetailRow("自动备份", instance.autoBackups ?: "—")
                            DetailRow("SSH 密钥数", instance.allowed?.size?.toString() ?: "0")
                        }
                        DangerCard(
                            instanceName = instance.displayName,
                            isDeleting = uiState.isDeleting,
                            onRequestDelete = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            instanceName = uiState.instance?.displayName ?: "",
            isDeleting = uiState.isDeleting,
            onDismiss = { if (!uiState.isDeleting) showDeleteDialog = false },
            onConfirm = {
                viewModel.delete(instanceId)
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun PowerActionsCard(
    instance: Instance,
    actionInProgress: Boolean,
    onStart: () -> Unit,
    onHalt: () -> Unit,
    onReboot: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("电源管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusChip(status = instance.status)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStart,
                    enabled = !actionInProgress && instance.status?.lowercase() != "active",
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("开机", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(
                    onClick = onHalt,
                    enabled = !actionInProgress && instance.status?.lowercase() != "off",
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                    Text("关机", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(
                    onClick = onReboot,
                    enabled = !actionInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Text("重启", modifier = Modifier.padding(start = 4.dp))
                }
            }
            if (actionInProgress) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                    Text("正在执行操作…", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun BandwidthCard(
    bandwidth: com.example.vultrmanager.data.remote.model.BandwidthData?,
    isLoading: Boolean,
    error: String?
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "流量监控（本月）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp)
                error != null -> Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                bandwidth != null -> {
                    val inTotal = bandwidth.incoming?.lastOrNull()?.getOrNull(1)
                    val outTotal = bandwidth.outgoing?.lastOrNull()?.getOrNull(1)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("入站", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatBytes(inTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("出站", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatBytes(outTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                    BandwidthChart(bandwidth)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendDot(MaterialTheme.colorScheme.primary, "入站")
                        LegendDot(MaterialTheme.colorScheme.tertiary, "出站")
                    }
                }
                else -> Text("暂无带宽数据", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun DangerCard(
    instanceName: String,
    isDeleting: Boolean,
    onRequestDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "危险操作",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                "销毁实例将永久删除该服务器及其数据，且不可恢复。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            Button(
                onClick = onRequestDelete,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                }
                Icon(Icons.Default.Delete, contentDescription = null)
                Text("销毁实例", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    instanceName: String,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting && confirmText == instanceName
            ) {
                Text("确认销毁")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) { Text("取消") }
        },
        title = { Text("确认销毁实例？") },
        text = {
            Column {
                Text("此操作不可恢复。请输入实例名称 \"$instanceName\" 以确认：")
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    )
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun ColumnScope.DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun EmptyDetail(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("返回列表") }
    }
}

private fun formatRam(mb: Int?): String = when {
    mb == null -> "—"
    mb >= 1024 -> "${mb / 1024} GB"
    else -> "$mb MB"
}

private fun formatBytes(bytes: Double?): String {
    if (bytes == null) return "—"
    val b = bytes.toLong()
    return when {
        b >= 1_000_000_000_000 -> "%.2f TB".format(b / 1_000_000_000_000.0)
        b >= 1_000_000_000 -> "%.2f GB".format(b / 1_000_000_000.0)
        b >= 1_000_000 -> "%.2f MB".format(b / 1_000_000.0)
        b >= 1_000 -> "%.2f KB".format(b / 1_000.0)
        else -> "$b B"
    }
}

private class InstanceDetailViewModelFactory(
    private val repository: VultrRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InstanceDetailViewModel(repository) as T
    }
}
