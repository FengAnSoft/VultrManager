package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /instances (create instance).
 * See: https://www.vultr.com/api/#operation/create-instance
 *
 * `region`, `plan` and `os_id` are required. All other fields are optional and, because
 * Gson skips null fields by default, only the ones you actually set are sent.
 */
data class CreateInstanceRequest(
    @SerializedName("region") val region: String,
    @SerializedName("plan") val plan: String,
    @SerializedName("os_id") val osId: Int,
    @SerializedName("label") val label: String? = null,
    @SerializedName("hostname") val hostname: String? = null,
    @SerializedName("sshkey_id") val sshkeyId: String? = null,
    @SerializedName("backups") val backups: String? = null,
    @SerializedName("enable_ipv6") val enableIpv6: Boolean? = null,
    @SerializedName("tag") val tag: String? = null,
    @SerializedName("ddos_protection") val ddosProtection: Boolean? = null,
    @SerializedName("activation_email") val activationEmail: Boolean? = null
)
