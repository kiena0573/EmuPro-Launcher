package com.example.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameEntity
import com.example.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class MainViewModel(private val repository: GameRepository) : ViewModel() {
    val games: StateFlow<List<GameEntity>?> = repository.allGames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _appColor = MutableStateFlow("dynamic")
    val appColor: StateFlow<String> = _appColor.asStateFlow()

    private val _mainScreenStyle = MutableStateFlow("style1")
    val mainScreenStyle: StateFlow<String> = _mainScreenStyle.asStateFlow()

    private val _coloredTopBar = MutableStateFlow(false)
    val coloredTopBar: StateFlow<Boolean> = _coloredTopBar.asStateFlow()

    private val _rawgApiKey = MutableStateFlow("")
    val rawgApiKey: StateFlow<String> = _rawgApiKey.asStateFlow()

    private val _scraperSteamEnabled = MutableStateFlow(true)
    val scraperSteamEnabled: StateFlow<Boolean> = _scraperSteamEnabled.asStateFlow()

    private val _scraperRawgEnabled = MutableStateFlow(true)
    val scraperRawgEnabled: StateFlow<Boolean> = _scraperRawgEnabled.asStateFlow()

    private val _scraperWikipediaEnabled = MutableStateFlow(true)
    val scraperWikipediaEnabled: StateFlow<Boolean> = _scraperWikipediaEnabled.asStateFlow()

    private val _scraperGoogleEnabled = MutableStateFlow(true)
    val scraperGoogleEnabled: StateFlow<Boolean> = _scraperGoogleEnabled.asStateFlow()

    private val _scraperTheGamesDbEnabled = MutableStateFlow(true)
    val scraperTheGamesDbEnabled: StateFlow<Boolean> = _scraperTheGamesDbEnabled.asStateFlow()

    private val _defaultCardStyle = MutableStateFlow("default")
    val defaultCardStyle: StateFlow<String> = _defaultCardStyle.asStateFlow()

    private val _addRomEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val addRomEvent = _addRomEvent.asSharedFlow()

    fun triggerAddRom() {
        viewModelScope.launch { _addRomEvent.emit(Unit) }
    }

    private val _defaultImageScale = MutableStateFlow("fit")
    val defaultImageScale: StateFlow<String> = _defaultImageScale.asStateFlow()

    private val _defaultShowLabel = MutableStateFlow(true)
    val defaultShowLabel: StateFlow<Boolean> = _defaultShowLabel.asStateFlow()

    private val _defaultLabelInside = MutableStateFlow(false)
    val defaultLabelInside: StateFlow<Boolean> = _defaultLabelInside.asStateFlow()

    private val _gridColumns = MutableStateFlow(2)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _globalGameCardScale = MutableStateFlow(1.0f)
    val globalGameCardScale: StateFlow<Float> = _globalGameCardScale.asStateFlow()

    private val _multiLineLabel = MutableStateFlow(false)
    val multiLineLabel: StateFlow<Boolean> = _multiLineLabel.asStateFlow()

    private val _boldGameTitle = MutableStateFlow(true)
    val boldGameTitle: StateFlow<Boolean> = _boldGameTitle.asStateFlow()

    private val _gameTitleFontWeight = MutableStateFlow("normal")
    val gameTitleFontWeight: StateFlow<String> = _gameTitleFontWeight.asStateFlow()

    private val _gameTitleFontSize = MutableStateFlow(14f)
    val gameTitleFontSize: StateFlow<Float> = _gameTitleFontSize.asStateFlow()

    private val _enableGlassEffectDefault = MutableStateFlow(true)
    val enableGlassEffectDefault: StateFlow<Boolean> = _enableGlassEffectDefault.asStateFlow()

    private val _enableFadeoutDefault = MutableStateFlow(true)
    val enableFadeoutDefault: StateFlow<Boolean> = _enableFadeoutDefault.asStateFlow()

    private val _fadeoutIntensityDefault = MutableStateFlow(0.7f)
    val fadeoutIntensityDefault: StateFlow<Float> = _fadeoutIntensityDefault.asStateFlow()

    private val _enableFadeoutDelta = MutableStateFlow(true)
    val enableFadeoutDelta: StateFlow<Boolean> = _enableFadeoutDelta.asStateFlow()

    private val _fadeoutIntensityDelta = MutableStateFlow(0.7f)
    val fadeoutIntensityDelta: StateFlow<Float> = _fadeoutIntensityDelta.asStateFlow()

    private val _enablePatternBackground = MutableStateFlow(false)
    val enablePatternBackground: StateFlow<Boolean> = _enablePatternBackground.asStateFlow()

    private val _patternPreset = MutableStateFlow("Hoa")
    val patternPreset: StateFlow<String> = _patternPreset.asStateFlow()
    
    private val _patternBackgroundStyle = MutableStateFlow("Nhạt")
    val patternBackgroundStyle: StateFlow<String> = _patternBackgroundStyle.asStateFlow()

    private val _patternDensity = MutableStateFlow(2)
    val patternDensity: StateFlow<Int> = _patternDensity.asStateFlow()

    private val _patternAnimated = MutableStateFlow(true)
    val patternAnimated: StateFlow<Boolean> = _patternAnimated.asStateFlow()

    private val _customWallpaperUri = MutableStateFlow<String?>(null)
    private val _allGamesWallpaperUri = MutableStateFlow<String?>(null)
    val allGamesWallpaperUri: StateFlow<String?> = _allGamesWallpaperUri.asStateFlow()
    val customWallpaperUri: StateFlow<String?> = _customWallpaperUri.asStateFlow()

    private val _enableWallpaperBlur = MutableStateFlow(false)
    val enableWallpaperBlur: StateFlow<Boolean> = _enableWallpaperBlur.asStateFlow()

    private val _wallpaperBlurRadius = MutableStateFlow(16f)
    val wallpaperBlurRadius: StateFlow<Float> = _wallpaperBlurRadius.asStateFlow()

    private val _hazePreset = MutableStateFlow("Ultra Thin")
    val hazePreset: StateFlow<String> = _hazePreset.asStateFlow()

    private val _popupHazePreset = MutableStateFlow("Regular")
    val popupHazePreset: StateFlow<String> = _popupHazePreset.asStateFlow()

    private val _popupHazeBrightness = MutableStateFlow(0f)
    val popupHazeBrightness: StateFlow<Float> = _popupHazeBrightness.asStateFlow()

    private val _chipHazePreset = MutableStateFlow("Ultra Thin")
    val chipHazePreset: StateFlow<String> = _chipHazePreset.asStateFlow()

    private val _hazeBlurRadius = MutableStateFlow(32f)
    val hazeBlurRadius: StateFlow<Float> = _hazeBlurRadius.asStateFlow()

    private val _hazeNoiseFactor = MutableStateFlow(0f)
    val hazeNoiseFactor: StateFlow<Float> = _hazeNoiseFactor.asStateFlow()

    private val _hazeTintAlpha = MutableStateFlow(0.55f)
    val hazeTintAlpha: StateFlow<Float> = _hazeTintAlpha.asStateFlow()

    private val _hazeBrightness = MutableStateFlow(0f)
    val hazeBrightness: StateFlow<Float> = _hazeBrightness.asStateFlow()

    private val _customPlatforms = MutableStateFlow<List<com.example.data.PlatformConfig>>(emptyList())
    val customPlatforms: StateFlow<List<com.example.data.PlatformConfig>> = _customPlatforms.asStateFlow()

    private val _lastSelectedPlatform = MutableStateFlow<String?>("Tất cả")
    val lastSelectedPlatform: StateFlow<String?> = _lastSelectedPlatform.asStateFlow()

    fun loadSettings(context: Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _mainScreenStyle.value = prefs.getString("main_screen_style", "style1") ?: "style1"
        _coloredTopBar.value = prefs.getBoolean("colored_top_bar", false)
        _appColor.value = prefs.getString("app_color", "dynamic") ?: "dynamic"
        _rawgApiKey.value = prefs.getString("rawg_api_key", "") ?: ""
        
        _scraperSteamEnabled.value = prefs.getBoolean("scraper_steam_enabled", true)
        _scraperRawgEnabled.value = prefs.getBoolean("scraper_rawg_enabled", true)
        _scraperWikipediaEnabled.value = prefs.getBoolean("scraper_wikipedia_enabled", true)
        _scraperGoogleEnabled.value = prefs.getBoolean("scraper_google_enabled", true)
        _scraperTheGamesDbEnabled.value = prefs.getBoolean("scraper_thegamesdb_enabled", true)
        
        _defaultCardStyle.value = prefs.getString("default_card_style", "default") ?: "default"
        _defaultImageScale.value = prefs.getString("default_image_scale", "fit") ?: "fit"
        _defaultShowLabel.value = try { prefs.getBoolean("default_show_label", true) } catch(e: Exception) { true }
        _defaultLabelInside.value = try { prefs.getBoolean("default_label_inside", false) } catch(e: Exception) { false }
        _gridColumns.value = try { prefs.getInt("grid_columns", 2) } catch(e: Exception) { 2 }
        _globalGameCardScale.value = try { prefs.getFloat("global_game_card_scale", 1.0f) } catch(e: Exception) { try { prefs.getInt("global_game_card_scale", 1).toFloat() } catch(e2: Exception) { 1.0f } }
        _multiLineLabel.value = try { prefs.getBoolean("multi_line_label", false) } catch(e: Exception) { false }
        _boldGameTitle.value = try { prefs.getBoolean("bold_game_title", true) } catch(e: Exception) { true }
        _gameTitleFontWeight.value = prefs.getString("game_title_font_weight", "normal") ?: "normal"
        _gameTitleFontSize.value = try { prefs.getFloat("game_title_font_size", 12f) } catch(e: Exception) { try { prefs.getInt("game_title_font_size", 12).toFloat() } catch(e2: Exception) { 12f } }
        _enableGlassEffectDefault.value = try { prefs.getBoolean("enable_glass_effect_default", true) } catch(e: Exception) { true }
        _enableFadeoutDefault.value = try { prefs.getBoolean("enable_fadeout_default", true) } catch(e: Exception) { true }
        _fadeoutIntensityDefault.value = try { prefs.getFloat("fadeout_intensity_default", 0.7f) } catch(e: Exception) { 0.7f }
        _enableFadeoutDelta.value = try { prefs.getBoolean("enable_fadeout_delta", true) } catch(e: Exception) { true }
        _fadeoutIntensityDelta.value = try { prefs.getFloat("fadeout_intensity_delta", 0.7f) } catch(e: Exception) { 0.7f }
        _enablePatternBackground.value = try { prefs.getBoolean("enable_pattern_background", false) } catch(e: Exception) { false }
        _patternPreset.value = prefs.getString("pattern_preset", "Hoa") ?: "Hoa"
        _patternBackgroundStyle.value = prefs.getString("pattern_background_style", "Nhạt") ?: "Nhạt"
        _patternDensity.value = try { prefs.getInt("pattern_density", 2) } catch(e: Exception) { 2 }
        _patternAnimated.value = try { prefs.getBoolean("pattern_animated", true) } catch(e: Exception) { true }
        _customWallpaperUri.value = prefs.getString("custom_wallpaper_uri", null)
        _allGamesWallpaperUri.value = prefs.getString("all_games_wallpaper_uri", null)
        _enableWallpaperBlur.value = try { prefs.getBoolean("enable_wallpaper_blur", false) } catch(e: Exception) { false }
        _wallpaperBlurRadius.value = try { prefs.getFloat("wallpaper_blur_radius", 16f) } catch(e: Exception) { 16f }
        _hazePreset.value = prefs.getString("haze_preset", "Ultra Thin") ?: "Ultra Thin"
        _popupHazePreset.value = prefs.getString("popup_haze_preset", "Regular") ?: "Regular"
        _popupHazeBrightness.value = prefs.getFloat("popup_haze_brightness", 0f)
        _chipHazePreset.value = prefs.getString("chip_haze_preset", "Ultra Thin") ?: "Ultra Thin"
        _hazeBlurRadius.value = prefs.getFloat("haze_blur_radius", 32f)
        _hazeNoiseFactor.value = prefs.getFloat("haze_noise_factor", 0f)
        _hazeTintAlpha.value = prefs.getFloat("haze_tint_alpha", 0.55f)
        _hazeBrightness.value = prefs.getFloat("haze_brightness", 0f)
        _lastSelectedPlatform.value = prefs.getString("last_selected_platform", "Tất cả")

        com.example.utils.EmulatorIntentFactory.loadCustomPlatforms(context)
        loadCustomPlatformsFlow(context)
        }
    }

    private fun loadCustomPlatformsFlow(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("custom_platforms", "[]")
        val list = mutableListOf<com.example.data.PlatformConfig>()
        try {
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(com.example.data.PlatformConfig(
                    name = obj.getString("name"),
                    packageName = obj.optString("package", ""),
                    playInApp = obj.optBoolean("playInApp", false),
                    isPreset = obj.optBoolean("isPreset", false)
                ))
            }
        } catch (e: Exception) { e.printStackTrace() }
        if (list.isEmpty()) {
            list.add(com.example.data.PlatformConfig("PSP", "org.ppsspp.ppssppgold", false, true))
            list.add(com.example.data.PlatformConfig("PS2", "xyz.aethersx2.android", false, true))
            list.add(com.example.data.PlatformConfig("GBA", "com.explusalpha.GbaEmu", false, true))
            saveCustomPlatforms(context, list)
        } else {
            _customPlatforms.value = list
        }
    }

    fun addCustomPlatform(context: Context, config: com.example.data.PlatformConfig) {
        val current = _customPlatforms.value.toMutableList()
        current.removeAll { it.name == config.name }
        current.add(config)
        saveCustomPlatforms(context, current)
    }

    fun removeCustomPlatform(context: Context, name: String) {
        val current = _customPlatforms.value.toMutableList()
        current.removeAll { it.name == name }
        saveCustomPlatforms(context, current)
    }

    private fun saveCustomPlatforms(context: Context, list: List<com.example.data.PlatformConfig>) {
        try {
            val jsonArray = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject()
                obj.put("name", item.name)
                obj.put("package", item.packageName)
                obj.put("playInApp", item.playInApp)
                obj.put("isPreset", item.isPreset)
                jsonArray.put(obj)
            }
            context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .edit().putString("custom_platforms", jsonArray.toString()).apply()
            com.example.utils.EmulatorIntentFactory.loadCustomPlatforms(context)
            _customPlatforms.value = list
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun exportSettings(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val exportObj = org.json.JSONObject()
                
                val settingsObj = org.json.JSONObject()
                prefs.all.forEach { (key, value) ->
                    settingsObj.put(key, value)
                }
                exportObj.put("settings", settingsObj)

                val gamesArray = org.json.JSONArray()
                val currentGames = repository.allGames.first()
                currentGames.forEach { game ->
                    val gameObj = org.json.JSONObject()
                    gameObj.put("title", game.title)
                    gameObj.put("filePath", game.filePath)
                    gameObj.put("platform", game.platform)
                    
                    // Rewrite absolute paths to relative ones inside the ZIP
                    var finalBoxartUrl = game.boxartUrl ?: ""
                    if (finalBoxartUrl.startsWith(context.filesDir.absolutePath)) {
                        val file = java.io.File(finalBoxartUrl)
                        finalBoxartUrl = file.name
                    }
                    gameObj.put("boxartUrl", finalBoxartUrl)
                    gameObj.put("showLabel", game.showLabel)
                    gameObj.put("labelInside", game.labelInside)
                    gameObj.put("cardStyle", game.cardStyle)
                    gameObj.put("imageScale", game.imageScale)
                    gamesArray.put(gameObj)
                }
                exportObj.put("games", gamesArray)
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val zipOut = java.util.zip.ZipOutputStream(outputStream)
                    
                    // Add JSON
                    val jsonEntry = java.util.zip.ZipEntry("data.json")
                    zipOut.putNextEntry(jsonEntry)
                    zipOut.write(exportObj.toString().toByteArray())
                    zipOut.closeEntry()
                    
                    // Add Images
                    val boxartsDir = java.io.File(context.filesDir, "boxarts")
                    if (boxartsDir.exists() && boxartsDir.isDirectory) {
                        boxartsDir.listFiles()?.forEach { file ->
                            if (file.isFile) {
                                val entry = java.util.zip.ZipEntry("boxarts/${file.name}")
                                zipOut.putNextEntry(entry)
                                file.inputStream().use { fis -> fis.copyTo(zipOut) }
                                zipOut.closeEntry()
                            }
                        }
                    }
                    
                    zipOut.close()
                }
            } catch(e: Exception) { e.printStackTrace() }
        }
    }

    fun importSettings(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                var jsonString: String? = null
                val boxartsDir = java.io.File(context.filesDir, "boxarts")
                if (!boxartsDir.exists()) boxartsDir.mkdirs()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val zipIn = java.util.zip.ZipInputStream(inputStream)
                    var entry = zipIn.nextEntry
                    
                    while (entry != null) {
                        if (entry.name == "data.json") {
                            jsonString = String(zipIn.readBytes())
                        } else if (entry.name.startsWith("boxarts/") && !entry.isDirectory) {
                            val fileName = java.io.File(entry.name).name
                            val destFile = java.io.File(boxartsDir, fileName)
                            java.io.FileOutputStream(destFile).use { fos ->
                                zipIn.copyTo(fos)
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
                
                if (jsonString != null) {
                    val exportObj = org.json.JSONObject(jsonString)
                    
                    if (exportObj.has("settings")) {
                        val settingsObj = exportObj.getJSONObject("settings")
                        val editor = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit()
                        settingsObj.keys().forEach { key ->
                            val value = settingsObj.get(key)
                            when (value) {
                                is String -> editor.putString(key, value)
                                is Boolean -> editor.putBoolean(key, value)
                                is Int -> editor.putInt(key, value)
                                is Float -> editor.putFloat(key, value)
                                is Long -> editor.putLong(key, value)
                            }
                        }
                        editor.apply()
                    }

                    if (exportObj.has("games")) {
                        val gamesArray = exportObj.getJSONArray("games")
                        val currentGames = repository.allGames.first()
                        for (i in 0 until gamesArray.length()) {
                            val gameObj = gamesArray.getJSONObject(i)
                            val title = gameObj.getString("title")
                            val filePath = gameObj.getString("filePath")
                            val platform = gameObj.getString("platform")
                            val rawBoxartUrl = gameObj.optString("boxartUrl", "")
                            
                            var finalBoxartUrl: String? = null
                            if (rawBoxartUrl.isNotEmpty()) {
                                if (!rawBoxartUrl.startsWith("http")) { // local file restored from zip
                                    finalBoxartUrl = java.io.File(boxartsDir, rawBoxartUrl).absolutePath
                                } else {
                                    finalBoxartUrl = rawBoxartUrl
                                }
                            }
                            
                            val showLabel = gameObj.optBoolean("showLabel", true)
                            val labelInside = gameObj.optBoolean("labelInside", false)
                            val cardStyle = gameObj.optString("cardStyle", "default")
                            val imageScale = gameObj.optString("imageScale", "fit")
                            
                            val existingGame = currentGames.find { it.filePath == filePath }
                            if (existingGame != null) {
                                repository.updateGame(existingGame.copy(
                                    title = title,
                                    platform = platform,
                                    boxartUrl = finalBoxartUrl,
                                    showLabel = showLabel,
                                    labelInside = labelInside,
                                    cardStyle = cardStyle,
                                    imageScale = imageScale
                                ))
                            } else {
                                val newGame = GameEntity(
                                    title = title,
                                    filePath = filePath,
                                    platform = platform,
                                    boxartUrl = finalBoxartUrl,
                                    showLabel = showLabel,
                                    labelInside = labelInside,
                                    cardStyle = cardStyle,
                                    imageScale = imageScale
                                )
                                repository.updateGame(newGame)
                            }
                        }
                    }
                    
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        loadSettings(context)
                    }
                }
            } catch(e: Exception) { e.printStackTrace() }
        }
    }

    fun saveDefaultTemplate(context: Context, cardStyle: String, imageScale: String, showLabel: Boolean, labelInside: Boolean) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("default_card_style", cardStyle)
            .putString("default_image_scale", imageScale)
            .putBoolean("default_show_label", showLabel)
            .putBoolean("default_label_inside", labelInside)
            .apply()
        _defaultCardStyle.value = cardStyle
        _defaultImageScale.value = imageScale
        _defaultShowLabel.value = showLabel
        _defaultLabelInside.value = labelInside
    }

    fun addNativeApp(context: Context, appName: String, packageName: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val pm = context.packageManager
            val boxartUrl = try {
                val icon = pm.getApplicationIcon(packageName)
                val bitmap = (icon as? android.graphics.drawable.BitmapDrawable)?.bitmap ?: run {
                    val bmp = android.graphics.Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bmp)
                    icon.setBounds(0, 0, canvas.width, canvas.height)
                    icon.draw(canvas)
                    bmp
                }
                val file = java.io.File(context.filesDir, "native_${packageName}.png")
                file.outputStream().use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                }
                file.absolutePath
            } catch (e: Exception) {
                null
            }

            val game = com.example.data.GameEntity(
                title = appName,
                platform = "Android", // Hoặc App
                filePath = "package:$packageName",
                boxartUrl = boxartUrl
            )
            repository.insertGame(game)
        }
    }

    fun saveMainScreenStyle(context: Context, style: String) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("main_screen_style", style).apply()
        _mainScreenStyle.value = style
    }

    fun saveColoredTopBar(context: Context, colored: Boolean) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("colored_top_bar", colored).apply()
        _coloredTopBar.value = colored
    }

    fun saveAppColor(context: Context, color: String) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_color", color).apply()
        _appColor.value = color
    }

    fun saveRawgApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("rawg_api_key", key).apply()
        _rawgApiKey.value = key
    }

    fun saveScraperToggles(context: Context, steam: Boolean, rawg: Boolean, wikipedia: Boolean, google: Boolean, gamesDb: Boolean) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("scraper_steam_enabled", steam)
            .putBoolean("scraper_rawg_enabled", rawg)
            .putBoolean("scraper_wikipedia_enabled", wikipedia)
            .putBoolean("scraper_google_enabled", google)
            .putBoolean("scraper_thegamesdb_enabled", gamesDb)
            .apply()
        _scraperSteamEnabled.value = steam
        _scraperRawgEnabled.value = rawg
        _scraperWikipediaEnabled.value = wikipedia
        _scraperGoogleEnabled.value = google
        _scraperTheGamesDbEnabled.value = gamesDb
    }

    fun updateGridColumns(context: Context, columns: Int) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putInt("grid_columns", columns).apply()
        _gridColumns.value = columns
    }

    fun updateGlobalGameCardScale(context: Context, scale: Float) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putFloat("global_game_card_scale", scale).apply()
        _globalGameCardScale.value = scale
    }

    fun toggleMultiLineLabel(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val current = _multiLineLabel.value
        prefs.edit().putBoolean("multi_line_label", !current).apply()
        _multiLineLabel.value = !current
    }

    fun toggleBoldGameTitle(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val current = _boldGameTitle.value
        prefs.edit().putBoolean("bold_game_title", !current).apply()
        _boldGameTitle.value = !current
    }

    fun updateGameTitleFontWeight(context: Context, weight: String) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("game_title_font_weight", weight).apply()
        _gameTitleFontWeight.value = weight
    }

    fun updateGameTitleFontSize(context: Context, size: Float) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putFloat("game_title_font_size", size).apply()
        _gameTitleFontSize.value = size
    }

    fun toggleGlassEffectDefault(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val current = _enableGlassEffectDefault.value
        prefs.edit().putBoolean("enable_glass_effect_default", !current).apply()
        _enableGlassEffectDefault.value = !current
    }

    fun toggleFadeoutDefault(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val current = _enableFadeoutDefault.value
        prefs.edit().putBoolean("enable_fadeout_default", !current).apply()
        _enableFadeoutDefault.value = !current
    }

    fun updateFadeoutIntensityDefault(context: Context, intensity: Float) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putFloat("fadeout_intensity_default", intensity).apply()
        _fadeoutIntensityDefault.value = intensity
    }

    fun toggleFadeoutDelta(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val current = _enableFadeoutDelta.value
        prefs.edit().putBoolean("enable_fadeout_delta", !current).apply()
        _enableFadeoutDelta.value = !current
    }

    fun updateFadeoutIntensityDelta(context: Context, intensity: Float) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putFloat("fadeout_intensity_delta", intensity).apply()
        _fadeoutIntensityDelta.value = intensity
    }

    fun togglePatternBackground(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val current = _enablePatternBackground.value
        prefs.edit().putBoolean("enable_pattern_background", !current).apply()
        _enablePatternBackground.value = !current
    }

    fun updatePatternPreset(context: Context, value: String) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putString("pattern_preset", value).apply()
        _patternPreset.value = value
    }

    fun updatePatternBackgroundStyle(context: Context, value: String) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putString("pattern_background_style", value).apply()
        _patternBackgroundStyle.value = value
    }

    fun updatePatternDensity(context: Context, value: Int) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putInt("pattern_density", value).apply()
        _patternDensity.value = value
    }

    fun togglePatternAnimated(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val current = _patternAnimated.value
        prefs.edit().putBoolean("pattern_animated", !current).apply()
        _patternAnimated.value = !current
    }

    fun updateAllGamesWallpaperUri(context: Context, value: String?) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putString("all_games_wallpaper_uri", value).apply()
        _allGamesWallpaperUri.value = value
    }

    fun updateCustomWallpaperUri(context: Context, value: String?) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putString("custom_wallpaper_uri", value).apply()
        _customWallpaperUri.value = value
    }

    fun toggleWallpaperBlur(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val current = _enableWallpaperBlur.value
        prefs.edit().putBoolean("enable_wallpaper_blur", !current).apply()
        _enableWallpaperBlur.value = !current
    }

    fun updateWallpaperBlurRadius(context: Context, value: Float) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putFloat("wallpaper_blur_radius", value).apply()
        _wallpaperBlurRadius.value = value
    }

    fun updateHazePreset(context: Context, value: String) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putString("haze_preset", value).apply()
        _hazePreset.value = value
    }

    fun updatePopupHazePreset(context: Context, value: String) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putString("popup_haze_preset", value).apply()
        _popupHazePreset.value = value
    }

    fun updatePopupHazeBrightness(context: Context, value: Float) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putFloat("popup_haze_brightness", value).apply()
        _popupHazeBrightness.value = value
    }

    fun updateChipHazePreset(context: Context, value: String) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putString("chip_haze_preset", value).apply()
        _chipHazePreset.value = value
    }

    fun updateHazeBlurRadius(context: Context, value: Float) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putFloat("haze_blur_radius", value).apply()
        _hazeBlurRadius.value = value
    }

    fun updateHazeNoiseFactor(context: Context, value: Float) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putFloat("haze_noise_factor", value).apply()
        _hazeNoiseFactor.value = value
    }

    fun updateHazeTintAlpha(context: Context, value: Float) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putFloat("haze_tint_alpha", value).apply()
        _hazeTintAlpha.value = value
    }

    fun updateHazeBrightness(context: Context, value: Float) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putFloat("haze_brightness", value).apply()
        _hazeBrightness.value = value
    }

    fun saveLastSelectedPlatform(context: Context, platform: String) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("last_selected_platform", platform).apply()
        _lastSelectedPlatform.value = platform
    }

    fun addROMs(context: Context, uris: List<Uri>, platform: String) {
        viewModelScope.launch {
            val allGames = repository.allGames.first()

            for (uri in uris) {
                var title = "Unknown Game"
                var forcedBoxartUrl: String? = null
                var originalFileName = ""
                
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            val name = cursor.getString(index)
                            originalFileName = name
                            title = name.substringBeforeLast(".")
                            
                            if (platform == "PS2") {
                                val regex = Regex("([A-Z]{4})[-_]?([0-9]{3})\\.([0-9]{2})", RegexOption.IGNORE_CASE)
                                val match = regex.find(title)
                                if (match != null) {
                                    val groups = match.groupValues
                                    val serial = "${groups[1]}-${groups[2]}${groups[3]}".uppercase()
                                    forcedBoxartUrl = "https://raw.githubusercontent.com/xlenore/ps2-covers/main/covers/default/${serial}.jpg"
                                }
                            }
                        }
                    }
                }

                // Check if an existing game matches the exact title or original filename
                val existingGame = allGames.find { 
                    it.title.equals(title, ignoreCase = true) || 
                    android.net.Uri.decode(it.filePath).endsWith(originalFileName, ignoreCase = true)
                }

                if (existingGame != null) {
                    repository.updateGame(existingGame.copy(filePath = uri.toString()))
                } else {
                    val enabledSources = mutableListOf<String>()
                    if (_scraperSteamEnabled.value) enabledSources.add("steam")
                    if (_scraperRawgEnabled.value) enabledSources.add("rawg")
                    if (_scraperWikipediaEnabled.value) enabledSources.add("wikipedia")
                    if (_scraperGoogleEnabled.value) enabledSources.add("google")
                    if (_scraperTheGamesDbEnabled.value) enabledSources.add("thegamesdb")

                    repository.scanAndAddGame(
                        context, title, uri.toString(), platform, _rawgApiKey.value, forcedBoxartUrl,
                        cardStyle = _defaultCardStyle.value,
                        imageScale = _defaultImageScale.value,
                        showLabel = _defaultShowLabel.value,
                        labelInside = _defaultLabelInside.value,
                        enabledSources = enabledSources
                    )
                }
            }
        }
    }

    fun clearAllBoxarts() {
        viewModelScope.launch {
            repository.clearAllBoxarts()
        }
    }

    fun batchUpdateGameSettings(platform: String?, cardStyle: String, imageScale: String, showLabel: Boolean, labelInside: Boolean) {
        viewModelScope.launch {
            val gamesToUpdate = repository.allGames.first().filter { platform == null || it.platform == platform }
            gamesToUpdate.forEach { game ->
                repository.updateGame(game.copy(
                    cardStyle = cardStyle,
                    imageScale = imageScale,
                    showLabel = showLabel,
                    labelInside = labelInside
                ))
            }
        }
    }

    fun updateGame(game: GameEntity) {
        viewModelScope.launch {
            repository.updateGame(game)
        }
    }

    fun updateGameWithImage(context: Context, game: GameEntity) {
        viewModelScope.launch {
            var finalGame = game
            if (game.boxartUrl != null && game.boxartUrl.startsWith("http")) {
                val localPath = repository.downloadImageToLocal(context, game.boxartUrl, game.platform, game.title)
                if (localPath != null) {
                    finalGame = game.copy(boxartUrl = localPath)
                }
            }
            repository.updateGame(finalGame)
        }
    }

    fun deleteGame(id: Int) {
        viewModelScope.launch {
            repository.deleteGame(id)
        }
    }

    fun batchDownloadBoxarts(context: Context, platformText: String = "{platform}", keyword: String = "") {
        viewModelScope.launch {
            val enabledSources = mutableListOf<String>()
            if (_scraperSteamEnabled.value) enabledSources.add("steam")
            if (_scraperRawgEnabled.value) enabledSources.add("rawg")
            if (_scraperWikipediaEnabled.value) enabledSources.add("wikipedia")
            if (_scraperGoogleEnabled.value) enabledSources.add("google")
            if (_scraperTheGamesDbEnabled.value) enabledSources.add("thegamesdb")

            repository.batchDownloadBoxarts(context, _rawgApiKey.value, platformText, keyword, enabledSources)
        }
    }

    suspend fun searchBoxartsOnline(title: String, platform: String = "", keyword: String = ""): List<String> {
        val enabledSources = mutableListOf<String>()
        if (_scraperSteamEnabled.value) enabledSources.add("steam")
        if (_scraperRawgEnabled.value) enabledSources.add("rawg")
        if (_scraperWikipediaEnabled.value) enabledSources.add("wikipedia")
        if (_scraperGoogleEnabled.value) enabledSources.add("google")
        if (_scraperTheGamesDbEnabled.value) enabledSources.add("thegamesdb")

        return repository.searchBoxartsOnline(title, platform, keyword, _rawgApiKey.value, enabledSources)
    }
}
