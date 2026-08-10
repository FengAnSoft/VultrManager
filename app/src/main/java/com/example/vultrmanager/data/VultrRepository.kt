package com.example.vultrmanager.data

import com.example.vultrmanager.data.local.ApiKeyStore
import com.example.vultrmanager.data.remote.VultrApi
import com.example.vultrmanager.data.remote.model.Account
import com.example.vultrmanager.data.remote.model.BandwidthData
import com.example.vultrmanager.data.remote.model.CreateInstanceRequest
import com.example.vultrmanager.data.remote.model.Instance
import com.example.vultrmanager.data.remote.model.Os
import com.example.vultrmanager.data.remote.model.Plan
import com.example.vultrmanager.data.remote.model.Region
import com.example.vultrmanager.data.remote.model.SshKey
import com.example.vultrmanager.data.remote.model.VultrError
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Single source of truth for Vultr data. Wraps [VultrApi] and converts results into
 * [kotlin.Result], translating HTTP failures into domain-specific exceptions:
 *  - 401               -> [VultrAuthException] (key missing / revoked)
 *  - other non-2xx     -> [VultrApiException]
 *  - connectivity issue -> [VultrNetworkException]
 */
class VultrRepository(
    private val api: VultrApi,
    private val apiKeyStore: ApiKeyStore,
    private val gson: Gson
) {
    fun hasApiKey(): Boolean = apiKeyStore.hasApiKey()

    suspend fun listInstances(): Result<List<Instance>> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.listInstances() }.map { it.instances }
    }

    suspend fun getInstance(id: String): Result<Instance> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.getInstance(id) }.mapCatching { resp ->
            resp.instance ?: throw VultrApiException("未找到该实例的详情。", 404)
        }
    }

    suspend fun startInstance(id: String): Result<Unit> = powerAction { api.startInstance(id) }
    suspend fun haltInstance(id: String): Result<Unit> = powerAction { api.haltInstance(id) }
    suspend fun rebootInstance(id: String): Result<Unit> = powerAction { api.rebootInstance(id) }

    suspend fun getAccount(): Result<Account> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.getAccount() }.map { it.account }
    }

    suspend fun getInstanceBandwidth(id: String): Result<BandwidthData> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.getInstanceBandwidth(id) }.map { it.bandwidth }
    }

    suspend fun listRegions(): Result<List<Region>> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.listRegions() }.map { it.regions }
    }

    suspend fun listPlans(): Result<List<Plan>> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.listPlans() }.map { it.plans }
    }

    suspend fun listOs(): Result<List<Os>> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.listOs() }.map { it.os }
    }

    suspend fun listSshKeys(): Result<List<SshKey>> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.listSshKeys() }.map { it.sshKeys }
    }

    suspend fun createInstance(request: CreateInstanceRequest): Result<Instance> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.createInstance(request) }
            .mapCatching { resp -> resp.instance ?: throw VultrApiException("创建实例失败：响应未包含实例数据。", 0) }
    }

    suspend fun deleteInstance(id: String): Result<Unit> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall { api.deleteInstance(id) }
    }

    private suspend fun powerAction(call: suspend () -> Response<Unit>): Result<Unit> {
        if (!apiKeyStore.hasApiKey()) {
            return Result.failure(VultrAuthException("尚未配置 API Key，请先在设置中填写。"))
        }
        return safeCall(call)
    }

    /**
     * Executes a Retrofit suspend call returning [Response] and normalizes the outcome.
     * For [Unit]-typed responses a null body is treated as success.
     */
    private suspend inline fun <reified T> safeCall(
        apiCall: suspend () -> Response<T>
    ): Result<T> = try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null || T::class == Unit::class) {
                @Suppress("UNCHECKED_CAST")
                Result.success(body ?: Unit as T)
            } else {
                Result.failure(VultrApiException("响应为空 (HTTP ${response.code()})", response.code()))
            }
        } else {
            val code = response.code()
            if (code == 401) {
                Result.failure(VultrAuthException("API Key 无效或缺失，请在设置中检查。"))
            } else {
                Result.failure(VultrApiException(parseErrorMessage(response), code))
            }
        }
    } catch (e: IOException) {
        Result.failure(VultrNetworkException("网络连接失败，请检查网络后重试。"))
    } catch (e: HttpException) {
        Result.failure(VultrApiException("HTTP ${e.code()}：${e.message()}", e.code()))
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun parseErrorMessage(response: Response<*>): String {
        return runCatching {
            val raw = response.errorBody()?.string()
            if (!raw.isNullOrBlank()) {
                gson.fromJson(raw, VultrError::class.java)?.error
                    ?.takeIf { it.isNotBlank() }
                    ?: "请求失败 (HTTP ${response.code()})"
            } else {
                "请求失败 (HTTP ${response.code()})"
            }
        }.getOrDefault("请求失败 (HTTP ${response.code()})")
    }
}
