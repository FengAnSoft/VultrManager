package com.example.vultrmanager.data.remote

import com.example.vultrmanager.data.remote.model.AccountResponse
import com.example.vultrmanager.data.remote.model.BandwidthResponse
import com.example.vultrmanager.data.remote.model.CreateInstanceRequest
import com.example.vultrmanager.data.remote.model.InstanceResponse
import com.example.vultrmanager.data.remote.model.InstancesResponse
import com.example.vultrmanager.data.remote.model.OsResponse
import com.example.vultrmanager.data.remote.model.PlansResponse
import com.example.vultrmanager.data.remote.model.RegionsResponse
import com.example.vultrmanager.data.remote.model.SshKeysResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Vultr API v2 — Instances endpoints.
 * Reference: https://www.vultr.com/api/  (tag: instances)
 *
 * Base URL is defined in [BASE_URL]. All requests require the
 * `Authorization: Bearer <API_KEY>` header (see [AuthInterceptor]).
 */
interface VultrApi {

    companion object {
        /** Vultr API v2 base URL (must end with a trailing slash for Retrofit). */
        const val BASE_URL = "https://api.vultr.com/v2/"
    }

    /** GET /instances — List all instances in the account. */
    @GET("instances")
    suspend fun listInstances(): Response<InstancesResponse>

    /** GET /instances/{id} — Get a single instance. */
    @GET("instances/{id}")
    suspend fun getInstance(@Path("id") id: String): Response<InstanceResponse>

    /** POST /instances/{id}/start — Power on an instance. */
    @POST("instances/{id}/start")
    suspend fun startInstance(@Path("id") id: String): Response<Unit>

    /** POST /instances/{id}/halt — Power off (halt) an instance. */
    @POST("instances/{id}/halt")
    suspend fun haltInstance(@Path("id") id: String): Response<Unit>

    /** POST /instances/{id}/reboot — Reboot an instance. */
    @POST("instances/{id}/reboot")
    suspend fun rebootInstance(@Path("id") id: String): Response<Unit>

    /** GET /account — Account + billing info. */
    @GET("account")
    suspend fun getAccount(): Response<AccountResponse>

    /** GET /instances/{id}/bandwidth — Monthly bandwidth (incoming/outgoing sample pairs). */
    @GET("instances/{id}/bandwidth")
    suspend fun getInstanceBandwidth(@Path("id") id: String): Response<BandwidthResponse>

    /** GET /regions — Available deploy regions. */
    @GET("regions")
    suspend fun listRegions(): Response<RegionsResponse>

    /** GET /plans — Available instance plans. */
    @GET("plans")
    suspend fun listPlans(): Response<PlansResponse>

    /** GET /os — Available operating systems. */
    @GET("os")
    suspend fun listOs(): Response<OsResponse>

    /** GET /ssh-keys — Registered SSH keys. */
    @GET("ssh-keys")
    suspend fun listSshKeys(): Response<SshKeysResponse>

    /** POST /instances — Create a new instance. */
    @POST("instances")
    suspend fun createInstance(@Body request: CreateInstanceRequest): Response<InstanceResponse>

    /** DELETE /instances/{id} — Destroy an instance. */
    @DELETE("instances/{id}")
    suspend fun deleteInstance(@Path("id") id: String): Response<Unit>
}
