package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/** Response wrapper for `GET /instances`. */
data class InstancesResponse(
    @SerializedName("instances") val instances: List<Instance> = emptyList(),
    @SerializedName("meta") val meta: Meta? = null
) {
    data class Meta(@SerializedName("total") val total: Int? = null)
}
