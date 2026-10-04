package com.example.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaApi {
    @GET("w/api.php")
    suspend fun searchCover(
        @Query("gsrsearch") query: String,
        @Query("action") action: String = "query",
        @Query("generator") generator: String = "search",
        @Query("prop") prop: String = "pageimages",
        @Query("format") format: String = "json",
        @Query("pithumbsize") pithumbsize: Int = 1000
    ): WikipediaResponse

    companion object {
        fun create(): WikipediaApi {
            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder()
                        .header("User-Agent", "AppletAgent/1.0 (Google AI Studio)")
                        .build())
                }.build()
            return Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(WikipediaApi::class.java)
        }
    }
}

data class WikipediaResponse(
    val query: WikiQuery?
)

data class WikiQuery(
    val pages: Map<String, WikiPage>?
)

data class WikiPage(
    val pageid: Int,
    val title: String,
    val thumbnail: WikiThumbnail?
)

data class WikiThumbnail(
    val source: String,
    val width: Int,
    val height: Int
)
