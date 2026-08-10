package com.example.vultrmanager

import android.app.Application
import android.content.Context
import com.example.vultrmanager.data.VultrRepository
import com.example.vultrmanager.data.local.ApiKeyStore
import com.example.vultrmanager.data.local.ThemeStore
import com.example.vultrmanager.data.remote.AuthInterceptor
import com.example.vultrmanager.data.remote.VultrApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Application entry point. Builds the networking + storage stack once and exposes it
 * via [appContainer] so screens can obtain the repository without a DI framework.
 */
class VultrManagerApp : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}

/**
 * Manual dependency container: OkHttp (+ auth interceptor) -> Retrofit -> VultrApi,
 * plus the encrypted [ApiKeyStore] and the [VultrRepository].
 */
class AppContainer(context: Context) {

    private val gson: Gson = GsonBuilder().create()

    private val apiKeyStore: ApiKeyStore = ApiKeyStore(context.applicationContext)
    private val themeStore: ThemeStore = ThemeStore(context.applicationContext)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(apiKeyStore))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(VultrApi.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val vultrApi: VultrApi = retrofit.create(VultrApi::class.java)

    val repository: VultrRepository = VultrRepository(vultrApi, apiKeyStore, gson)

    /** Exposed so the UI can check/route on first launch without going through the repo. */
    val apiKeyStoreRef: ApiKeyStore = apiKeyStore

    /** Exposed so the UI can read/observe the dark-mode preference. */
    val themeStoreRef: ThemeStore = themeStore
}
