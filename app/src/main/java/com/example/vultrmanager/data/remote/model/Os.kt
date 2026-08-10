package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * A Vultr operating system image, from GET /os -> { os: [...] }.
 * See: https://www.vultr.com/api/#operation/get-oses
 */
data class Os(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String? = null,
    @SerializedName("arch") val arch: String? = null,
    @SerializedName("family") val family: String? = null
)

data class OsResponse(
    @SerializedName("os") val os: List<Os>
)
