package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/** Vultr error body shape: `{ "error": "message" }`. */
data class VultrError(
    @SerializedName("error") val error: String? = null
)
