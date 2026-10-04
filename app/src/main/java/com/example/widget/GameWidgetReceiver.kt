package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.ACTION_APPWIDGET_PINNED") {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            val gameId = intent.getIntExtra("EXTRA_GAME_ID", -1)
            if (appWidgetId != -1 && gameId != -1) {
                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putInt("widget_${appWidgetId}_game_id", gameId)
                    .putString("widget_${appWidgetId}_scale", "crop")
                    .putInt("widget_${appWidgetId}_padding", 8) // default slight padding
                    .apply()
                // Do the update
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    val gameId = prefs.getInt("widget_${appWidgetId}_game_id", -1)
    val scaleMode = prefs.getString("widget_${appWidgetId}_scale", "crop")
    val paddingDp = prefs.getInt("widget_${appWidgetId}_padding", 8).toFloat()
    val showBackground = prefs.getBoolean("widget_${appWidgetId}_show_background", false)

    val paddingPx = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP,
        paddingDp,
        context.resources.displayMetrics
    ).toInt()

    val views = RemoteViews(context.packageName, R.layout.widget_game)
    views.setViewPadding(R.id.widget_root, paddingPx, paddingPx, paddingPx, paddingPx)
    
    if (showBackground) {
        val bgColor = com.example.ui.theme.getWidgetBackgroundColor(context)
        views.setInt(R.id.widget_bg, "setVisibility", android.view.View.VISIBLE)
        views.setInt(R.id.widget_bg, "setColorFilter", bgColor)
        views.setInt(R.id.widget_root, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
    } else {
        views.setInt(R.id.widget_bg, "setVisibility", android.view.View.GONE)
        views.setInt(R.id.widget_root, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
    }

    if (gameId != -1) {
        val launchIntent = Intent(context, TrampolineActivity::class.java).apply {
            putExtra("EXTRA_GAME_ID", gameId)
            action = "com.example.LAUNCH_GAME_$appWidgetId"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val game = db.gameDao().getGameById(gameId)
            var imageSet = false
            if (game?.boxartUrl != null) {
                try {
                    val filePath = game.boxartUrl
                    val data = if (filePath?.startsWith("/") == true) java.io.File(filePath) else filePath
                    val request = ImageRequest.Builder(context.applicationContext)
                        .data(data)
                        .size(512, 512)
                        .allowHardware(false)
                        .build()
                    val result = context.applicationContext.imageLoader.execute(request)
                    if (result is SuccessResult) {
                        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                        if (bitmap != null) {
                            if (scaleMode == "fit") {
                                views.setImageViewBitmap(R.id.widget_image_fit, bitmap)
                                views.setViewVisibility(R.id.widget_image_fit, android.view.View.VISIBLE)
                                views.setViewVisibility(R.id.widget_image_crop, android.view.View.GONE)
                            } else {
                                views.setImageViewBitmap(R.id.widget_image_crop, bitmap)
                                views.setViewVisibility(R.id.widget_image_crop, android.view.View.VISIBLE)
                                views.setViewVisibility(R.id.widget_image_fit, android.view.View.GONE)
                            }
                            imageSet = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (!imageSet) {
                views.setImageViewResource(R.id.widget_image_crop, R.mipmap.ic_launcher)
                views.setViewVisibility(R.id.widget_image_crop, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.widget_image_fit, android.view.View.GONE)
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    } else {
        views.setImageViewResource(R.id.widget_image_crop, R.mipmap.ic_launcher)
        views.setViewVisibility(R.id.widget_image_crop, android.view.View.VISIBLE)
        views.setViewVisibility(R.id.widget_image_fit, android.view.View.GONE)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
