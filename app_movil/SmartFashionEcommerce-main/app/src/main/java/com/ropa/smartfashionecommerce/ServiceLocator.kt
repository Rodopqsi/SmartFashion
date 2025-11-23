package com.ropa.smartfashionecommerce

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {
    fun okHttp(tokenStore: TokenStore, baseUrl: String): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .authenticator(TokenAuthenticator(tokenStore, baseUrl))
            .addInterceptor(logging)
            .build()
    }

    fun retrofit(client: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    fun authApi(retrofit: Retrofit) = retrofit.create(AuthApi::class.java)
    fun shopApi(retrofit: Retrofit) = retrofit.create(ShopApi::class.java)
}