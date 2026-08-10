package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/** Response wrapper for `GET /instances/{id}`. */
data class InstanceResponse(
    @SerializedName("instance") val instance: Instance? = null
)
