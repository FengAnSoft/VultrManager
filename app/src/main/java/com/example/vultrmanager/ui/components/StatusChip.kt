package com.example.vultrmanager.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Maps a raw Vultr `status` string to a human-readable Chinese label and a color,
 * then renders it as a small chip.
 */
@Composable
fun StatusChip(status: String?) {
    val (label, color) = statusInfo(status)
    AssistChip(
        onClick = { },
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color,
            leadingIconContentColor = color
        )
    )
}

fun statusInfo(status: String?): Pair<String, Color> = when (status?.lowercase()) {
    "active" -> "运行中" to Color(0xFF2E7D32)
    "off", "temp_stopped" -> "已关机" to Color(0xFFC62828)
    "pending", "installing_boot", "isolate" -> "部署中" to Color(0xFFEF6C00)
    else -> (status ?: "未知") to Color(0xFF616161)
}

/**
 * Coarse category used by the list's status filter. One of:
 * `running`, `stopped`, `pending`, `other`.
 */
fun statusCategory(status: String?): String = when (status?.lowercase()) {
    "active" -> "running"
    "off", "temp_stopped" -> "stopped"
    "pending", "installing_boot", "isolate" -> "pending"
    else -> "other"
}
