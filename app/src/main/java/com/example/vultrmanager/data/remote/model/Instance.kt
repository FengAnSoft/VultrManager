package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * A Vultr cloud compute instance, as returned by the Vultr API v2.
 * Field names map the JSON snake_case keys from the API to Kotlin properties.
 * See: https://www.vultr.com/api/#operation/get-instances
 */
data class Instance(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("main_ip") val mainIp: String? = null,
    @SerializedName("internal_ip") val internalIp: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("os") val os: String? = null,
    @SerializedName("os_id") val osId: Int? = null,
    @SerializedName("vcpu_count") val vcpuCount: Int? = null,
    @SerializedName("ram") val ram: Int? = null,
    @SerializedName("disk") val disk: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("power_status") val powerStatus: String? = null,
    @SerializedName("enable_ipv6") val enableIpv6: Boolean? = null,
    @SerializedName("v6_main_ip") val v6MainIp: String? = null,
    @SerializedName("firewall_group_id") val firewallGroupId: String? = null,
    @SerializedName("auto_backups") val autoBackups: String? = null,
    @SerializedName("cost") val cost: Double? = null,
    @SerializedName("monthly_cost") val monthlyCost: Double? = null,
    @SerializedName("allowed_bandwidth") val allowedBandwidth: Double? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("hostname") val hostname: String? = null,
    @SerializedName("tag") val tag: String? = null,
    @SerializedName("allowed") val allowed: List<String>? = null
) {
    /** Display name: label when present, otherwise the instance id. */
    val displayName: String get() = if (label.isNullOrBlank()) id else label
}
