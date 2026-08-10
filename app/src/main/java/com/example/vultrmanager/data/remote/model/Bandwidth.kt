package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Bandwidth usage for an instance, from GET /instances/{id}/bandwidth.
 * See: https://www.vultr.com/api/#operation/get-instance-bandwidth
 *
 * The Vultr API returns, for `incoming` and `outgoing`, a list of [unixSeconds, bytes]
 * pairs describing cumulative traffic at each sampled timestamp. If the response uses a
 * different shape, both lists stay null and the UI falls back to "no data".
 */
data class BandwidthResponse(
    @SerializedName("bandwidth") val bandwidth: BandwidthData
)

data class BandwidthData(
    @SerializedName("incoming") val incoming: List<List<Double>>? = null,
    @SerializedName("outgoing") val outgoing: List<List<Double>>? = null
)
