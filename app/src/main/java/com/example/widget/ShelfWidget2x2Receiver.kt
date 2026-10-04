package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.widget.RemoteViews
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.RoundedCornersTransformation
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShelfWidget2x2Receiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateShelf2x2Widget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateShelf2x2Widget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val prefs = context.getSharedPreferences("shelf_widget_prefs", Context.MODE_PRIVATE)
    val game1Id = prefs.getInt("widget_${appWidgetId}_game_1", -1)
    val game2Id = prefs.getInt("widget_${appWidgetId}_game_2", -1)
    val game3Id = prefs.getInt("widget_${appWidgetId}_game_3", -1)
    val game4Id = prefs.getInt("widget_${appWidgetId}_game_4", -1)
    val gameIds = listOf(game1Id, game2Id, game3Id, game4Id)
    val viewIds = listOf(R.id.shelf_game_1, R.id.shelf_game_2, R.id.shelf_game_3, R.id.shelf_game_4)
    val mode = prefs.getString("widget_${appWidgetId}_mode", "original") ?: "original"
    val showShelf = prefs.getBoolean("widget_${appWidgetId}_show_shelf", true)

    val views = RemoteViews(context.packageName, R.layout.widget_shelf_2x2)

    if (showShelf) {
        val bgType = prefs.getString("widget_${appWidgetId}_bg", "material_you")
        if (bgType == "screenshot" || bgType == "blurred_cover") {
            val file = java.io.File(context.filesDir, "widget_${appWidgetId}_screenshot.png")
            if (file.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.shelf_bg_image, bitmap)
                    views.setInt(R.id.shelf_bg_image, "setVisibility", android.view.View.VISIBLE)
                    views.setInt(R.id.shelf_container, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
                }
            } else {
                views.setInt(R.id.shelf_bg_image, "setVisibility", android.view.View.GONE)
                views.setInt(R.id.shelf_container, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
            }
        } else if (bgType == "transparent") {
            views.setInt(R.id.shelf_bg_image, "setVisibility", android.view.View.GONE)
            views.setInt(R.id.shelf_container, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
        } else if (bgType == "gamecard") {
            views.setInt(R.id.shelf_bg_image, "setVisibility", android.view.View.GONE)
            val isDarkMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            if (isDarkMode) {
                views.setInt(R.id.shelf_container, "setBackgroundColor", android.graphics.Color.parseColor("#1E1E1E"))
            } else {
                views.setInt(R.id.shelf_container, "setBackgroundColor", android.graphics.Color.parseColor("#E8DEF8"))
            }
        } else {
            if (bgType == "material_you") {
                val bgColor = com.example.ui.theme.getWidgetBackgroundColor(context)
                views.setInt(R.id.shelf_bg_image, "setVisibility", android.view.View.VISIBLE)
                views.setInt(R.id.shelf_bg_image, "setImageResource", R.drawable.bg_material_you)
                views.setInt(R.id.shelf_bg_image, "setColorFilter", bgColor)
                views.setInt(R.id.shelf_container, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
            } else {
                views.setInt(R.id.shelf_bg_image, "setVisibility", android.view.View.GONE)
                val bgRes = when (bgType) {
                    "asian_dragon" -> R.drawable.bg_asian_dragon
                    "banh_chung" -> R.drawable.bg_banh_chung
                    "dong_leaf" -> R.drawable.bg_dong_leaf
                    "mooncake" -> R.drawable.bg_mooncake
                    else -> R.drawable.shelf_background
                }
                views.setInt(R.id.shelf_container, "setBackgroundResource", bgRes)
            }
        }
    } else {
        views.setInt(R.id.shelf_bg_image, "setVisibility", android.view.View.GONE)
        views.setInt(R.id.shelf_container, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
    }

    val configIntent = Intent(context, ShelfWidgetConfigureActivity::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val configPendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        configIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.shelf_root, configPendingIntent)

    GlobalScope.launch(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        for (i in 0..3) {
            val gameId = gameIds[i]
            val viewId = viewIds[i]
            if (gameId != -1) {
                val launchIntent = Intent(context, TrampolineActivity::class.java).apply {
                    putExtra("EXTRA_GAME_ID", gameId)
                    action = "com.example.LAUNCH_GAME_${appWidgetId}_$i"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 10 + i,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(viewId, pendingIntent)

                var imageSet = false
                val game = db.gameDao().getGameById(gameId)
                if (game?.boxartUrl != null) {
                    try {
                        val filePath = game.boxartUrl
                        val data = if (filePath?.startsWith("/") == true) java.io.File(filePath) else filePath
                        val requestBuilder = ImageRequest.Builder(context.applicationContext)
                            .data(data)
                            .size(512, 512)
                            .allowHardware(false)
                        
                        if (mode == "rounded") {
                            requestBuilder.transformations(RoundedCornersTransformation(64f))
                        }
                        
                        val request = requestBuilder.build()
                        val result = context.applicationContext.imageLoader.execute(request)
                        if (result is SuccessResult) {
                            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                            if (bitmap != null) {
                                views.setImageViewBitmap(viewId, bitmap)
                                imageSet = true
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (!imageSet) {
                    views.setImageViewResource(viewId, R.mipmap.ic_launcher)
                }
            } else {
                val launchConfigIntent = Intent(context, ShelfWidgetConfigureActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val configPendingIntent2 = PendingIntent.getActivity(
                    context,
                    appWidgetId * 10 + i,
                    launchConfigIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(viewId, configPendingIntent2)
                views.setImageViewResource(viewId, R.drawable.ic_widget_add)
            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
