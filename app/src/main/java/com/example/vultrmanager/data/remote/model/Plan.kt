package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * A Vultr plan (instance type), from GET /plans -> { plans: [...] }.
 * See: https://www.vultr.com/api/#operation/get-plans
 */
data class Plan(
    @SerializedName("id") val id: String,
    @SerializedName("vcpu_count") val vcpuCount: Int? = null,
    @SerializedName("ram") val ram: Int? = null,
    @SerializedName("disk") val disk: Int? = null,
    @SerializedName("bandwidth") val bandwidth: Int? = null,
    @SerializedName("price_per_month") val pricePerMonth: Double? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("locations") val locations: List<String>? = null
)

data class PlansResponse(
    @SerializedName("plans") val plans: List<Plan>
)
