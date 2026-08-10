package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * A deployable Vultr region, from GET /regions -> { regions: [...] }.
 * See: https://www.vultr.com/api/#operation/get-regions
 */
data class Region(
    @SerializedName("id") val id: String,
    @SerializedName("city") val city: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("continent") val continent: String? = null,
    @SerializedName("options") val options: List<String>? = null
)

data class RegionsResponse(
    @SerializedName("regions") val regions: List<Region>
)
