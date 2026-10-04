package com.example.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface BoxartApi {
    @GET("games")
    suspend fun searchGame(
        @Query("search") query: String,
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 5
    ): GameSearchResponse

    companion object {
        fun create(): BoxartApi {
            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .build())
                }.build()
            return Retrofit.Builder()
                .baseUrl("https://api.rawg.io/api/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(BoxartApi::class.java)
        }
    }
}

data class GameSearchResponse(
    val results: List<GameResult>
)

data class GameResult(
    val background_image: String?
)
