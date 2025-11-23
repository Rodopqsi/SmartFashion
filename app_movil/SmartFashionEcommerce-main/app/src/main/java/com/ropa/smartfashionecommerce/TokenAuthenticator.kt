package com.ropa.smartfashionecommerce

import kotlinx.coroutines.runBlocking
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(
    private val tokenStore: TokenStore,
    baseUrl: String
): Authenticator {
    private val client = OkHttpClient.Builder().build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
    private val authApi = retrofit.create(AuthApi::class.java)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) return null
        val newAccess = runBlocking {
            val refresh = tokenStore.getRefresh() ?: return@runBlocking null
            val res = authApi.refresh(mapOf("refresh" to refresh)).execute()
            if (res.isSuccessful) {
                val a = res.body()?.access ?: return@runBlocking null
                tokenStore.saveTokens(a, refresh)
                a
            } else null
        } ?: return null

        return response.request.newBuilder().header("Authorization", "Bearer $newAccess").build()
    }
}