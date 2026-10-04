package com.example.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApi {
    @GET("api/storesearch/")
    suspend fun searchGame(
        @Query("term") query: String,
        @Query("l") lang: String = "english",
        @Query("cc") cc: String = "US"
    ): SteamSearchResponse

    companion object {
        fun create(): SteamApi {
            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .build())
                }.build()
            return Retrofit.Builder()
                .baseUrl("https://store.steampowered.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SteamApi::class.java)
        }
    }
}

data class SteamSearchResponse(
    val total: Int,
    val items: List<SteamGameResult>
)

data class SteamGameResult(
    val id: Int,
    val name: String,
    val tiny_image: String?
)
