package com.example.data

import com.example.network.BoxartApi
import com.example.network.SteamApi
import com.example.network.WikipediaApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository(
    private val gameDao: GameDao, 
    private val boxartApi: BoxartApi = BoxartApi.create(),
    private val steamApi: SteamApi = SteamApi.create(),
    private val wikipediaApi: WikipediaApi = WikipediaApi.create()
) {
    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()

    suspend fun scanAndAddGame(
        context: android.content.Context, title: String, filePath: String, platform: String, apiKey: String, forcedBoxartUrl: String? = null,
        cardStyle: String = "default", imageScale: String = "fit", showLabel: Boolean = true, labelInside: Boolean = false,
        enabledSources: List<String> = listOf("steam", "rawg", "wikipedia", "google", "thegamesdb")
    ) {
        var finalBoxartUrl: String? = forcedBoxartUrl
        if (finalBoxartUrl == null) {
            try {
                val boxarts = searchBoxartsOnline(gameTitle = title, apiKey = apiKey, enabledSources = enabledSources)
                for (url in boxarts) {
                    val localPath = downloadImageToLocal(context, url, platform, title)
                    if (localPath != null) {
                        finalBoxartUrl = localPath
                        break
                    }
                }
                if (finalBoxartUrl == null && boxarts.isNotEmpty()) {
                    finalBoxartUrl = boxarts.first()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (finalBoxartUrl.startsWith("http")) {
            val localPath = downloadImageToLocal(context, finalBoxartUrl, platform, title)
            if (localPath != null) {
                finalBoxartUrl = localPath
            }
        }

        val game = GameEntity(
            title = title, filePath = filePath, platform = platform, boxartUrl = finalBoxartUrl,
            cardStyle = cardStyle, imageScale = imageScale, showLabel = showLabel, labelInside = labelInside
        )
        gameDao.insertGame(game)
    }

    suspend fun getGame(id: Int): GameEntity? {
        return gameDao.getGameById(id)
    }

    suspend fun insertGame(game: com.example.data.GameEntity) {
        gameDao.insertGame(game)
    }

    suspend fun updateGame(game: GameEntity) {
        gameDao.insertGame(game)
    }

    suspend fun deleteGame(id: Int) {
        gameDao.deleteGame(id)
    }

    suspend fun clearAllBoxarts() {
        gameDao.clearAllBoxarts()
    }

    suspend fun searchBoxartsOnline(gameTitle: String, platform: String = "", keyword: String = "", apiKey: String = "", enabledSources: List<String> = listOf("steam", "rawg", "wikipedia", "google", "thegamesdb")): List<String> = withContext(Dispatchers.IO) {
        val results = mutableListOf<String>()
        val exactTitleQuery = gameTitle.trim()
        val webQuery = "$gameTitle $platform $keyword".trim().replace(Regex("\\s+"), " ")

        // 1. Steam (High quality, free) expects game title
        if (enabledSources.contains("steam")) {
            try {
                val response = steamApi.searchGame(exactTitleQuery)
                response.items.forEach { item ->
                    results.add("https://steamcdn-a.akamaihd.net/steam/apps/${item.id}/library_600x900_2x.jpg")
                    results.add("https://cdn.akamai.steamstatic.com/steam/apps/${item.id}/header.jpg")
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // 2. RAWG expects game title
        if (enabledSources.contains("rawg") && apiKey.isNotBlank()) {
            try {
                val response = boxartApi.searchGame(exactTitleQuery, apiKey = apiKey)
                response.results.forEach { item ->
                    item.background_image?.let { results.add(it) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // 3. Wikipedia (full text search engine semantics)
        if (enabledSources.contains("wikipedia")) {
            try {
                val response = wikipediaApi.searchCover("$webQuery game cover boxart")
                response.query?.pages?.values?.forEach { page ->
                    page.thumbnail?.source?.let { results.add(it) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // 4. Web Image Search (Bing)
        if (enabledSources.contains("google")) {
            try {
                val encodedQuery = java.net.URLEncoder.encode("$webQuery game boxart", "UTF-8")
                val doc = org.jsoup.Jsoup.connect("https://www.bing.com/images/search?q=$encodedQuery")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get()
                val elements = doc.select("a.iusc")
                elements.forEach { el ->
                    val m = el.attr("m")
                    val murlMatch = Regex(""""murl":"([^"]+)"""").find(m)
                    if (murlMatch != null) {
                       results.add(murlMatch.groupValues[1])
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // 5. TheGamesDB (Jsoup HTML scrape)
        if (enabledSources.contains("thegamesdb")) {
            try {
                val encodedQuery = java.net.URLEncoder.encode(exactTitleQuery, "UTF-8")
                val doc = org.jsoup.Jsoup.connect("https://thegamesdb.net/search.php?name=$encodedQuery")
                    .userAgent("Mozilla/5.0")
                    .get()
                val elements = doc.select("img.card-img-top")
                elements.forEach { el ->
                    val src = el.attr("src")
                    if (src.startsWith("banners/")) {
                        val replacedSrc = src.replace("banners/", "")
                        results.add("https://cdn.thegamesdb.net/images/original/boxart/$replacedSrc")
                        results.add("https://cdn.thegamesdb.net/images/thumb/boxart/$replacedSrc")
                    } else if (src.contains("boxart")) {
                        results.add("https://cdn.thegamesdb.net/images/original/" + src.substringAfterLast("banners/"))
                        results.add("https://thegamesdb.net/$src")
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        return@withContext results.distinct()
    }

    suspend fun batchDownloadBoxarts(context: android.content.Context, apiKey: String = "", platformText: String = "{platform}", keyword: String = "", enabledSources: List<String> = listOf("steam", "rawg", "wikipedia", "google", "thegamesdb")) {
        val games = gameDao.getAllGamesSync()
        for (game in games) {
            if (game.boxartUrl == null || game.boxartUrl!!.isBlank()) {
                try {
                    val k = if (keyword == "trống (không dùng)") "" else keyword
                    val p = if (platformText == "{platform}") game.platform else platformText
                    val boxarts = searchBoxartsOnline(game.title, p, k, apiKey, enabledSources)
                    if (boxarts.isNotEmpty()) {
                        val targetUrl = boxarts.first()
                        val localPath = downloadImageToLocal(context, targetUrl, game.platform, game.title)
                        if (localPath != null) {
                            gameDao.insertGame(game.copy(boxartUrl = localPath))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun downloadImageToLocal(context: android.content.Context, url: String, platform: String, title: String): String? = withContext(Dispatchers.IO) {
        if (!url.startsWith("http")) return@withContext url // already local

        try {
            val boxartsDir = java.io.File(context.filesDir, "boxarts")
            if (!boxartsDir.exists()) boxartsDir.mkdirs()

            val safeTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            val fileName = "${platform}_${safeTitle}_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(boxartsDir, fileName)

            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doInput = true
            connection.connect()
            
            if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                return@withContext null
            }
            
            val input = connection.inputStream
            val output = java.io.FileOutputStream(file)
            input.copyTo(output)
            output.close()
            input.close()
            
            return@withContext file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
