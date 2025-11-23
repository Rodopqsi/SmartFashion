package com.ropa.smartfashionecommerce

import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("api/auth/token/")
    suspend fun login(@Body body: Map<String, String>): Response<TokenPair>

    @POST("api/auth/token/refresh/")
    fun refresh(@Body body: Map<String, String>): retrofit2.Call<AccessOnly>
}

interface ShopApi {
    // Ajustado a endpoints reales del backend
    @GET("api/home/")
    suspend fun getHome(
        @Query("category_id") categoryId: Int? = null,
        @Query("q") q: String? = null,
        @Query("size") size: Int? = null,
        @Query("color") color: Int? = null,
        @Query("limit") limit: Int? = 12
    ): HomeResponse

    @GET("api/sizes/")
    suspend fun getSizes(): BaseListResponse<SizeDto>

    @GET("api/colors/")
    suspend fun getColors(): BaseListResponse<ColorDto>
}