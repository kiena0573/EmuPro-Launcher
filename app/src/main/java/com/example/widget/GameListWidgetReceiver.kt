package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.R
import com.example.utils.EmulatorIntentFactory
import java.io.File
import android.util.Log

class GameListWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateGameListWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

fun updateGameListWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val prefs = context.getSharedPreferences("game_list_widget_prefs", Context.MODE_PRIVATE)
    val bgType = prefs.getString("widget_${appWidgetId}_bg", "material_you") ?: "material_you"
    val textColor = prefs.getString("widget_${appWidgetId}_text_color", "white") ?: "white"
    
    val views = RemoteViews(context.packageName, R.layout.game_list_widget)

    // Set background
    views.setViewVisibility(R.id.widget_background_image, android.view.View.GONE)
    views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)

    when (bgType) {
        "transparent" -> {
            views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
        }
        "material_you" -> {
            // Using standard color
            val color = ContextCompat.getColor(context, android.R.color.system_accent1_500)
            views.setInt(R.id.widget_root, "setBackgroundColor", color)
        }
        "gamecard" -> {
            val isDarkMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            if (isDarkMode) {
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.parseColor("#1E1E1E")) // Dark surface variant
            } else {
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.parseColor("#E8DEF8")) // Light surface variant or primary container
            }
        }
        "blurred_cover" -> {
            val file = File(context.filesDir, "widget_${appWidgetId}_screenshot.png")
            if (file.exists()) {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                if (bmp != null) {
                    views.setImageViewBitmap(R.id.widget_background_image, bmp)
                    views.setViewVisibility(R.id.widget_background_image, android.view.View.VISIBLE)
                }
            }
        }
        "screenshot" -> {
            val file = File(context.filesDir, "widget_${appWidgetId}_screenshot.png")
            if (file.exists()) {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                if (bmp != null) {
                    views.setImageViewBitmap(R.id.widget_background_image, bmp)
                    views.setViewVisibility(R.id.widget_background_image, android.view.View.VISIBLE)
                }
            }
        }
        else -> {
            // Check for patterns
            var drawableId = 0
            if (bgType == "asian_dragon") drawableId = R.drawable.bg_asian_dragon
            if (bgType == "banh_chung") drawableId = R.drawable.bg_banh_chung
            if (bgType == "dong_leaf") drawableId = R.drawable.bg_dong_leaf
            if (bgType == "mooncake") drawableId = R.drawable.bg_mooncake

            if (drawableId != 0) {
                val drawable = ContextCompat.getDrawable(context, drawableId)
                if (drawable is BitmapDrawable) {
                    views.setImageViewBitmap(R.id.widget_background_image, drawable.bitmap)
                    views.setViewVisibility(R.id.widget_background_image, android.view.View.VISIBLE)
                }
            } else {
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.parseColor("#E8DEF8"))
            }
        }
    }

    // Set up the intent that starts the RemoteViewsService
    val intent = Intent(context, GameListWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }
    
    // Pass the text color to the factory via intent or we can let factory read prefs
    views.setRemoteAdapter(R.id.widget_grid_view, intent)
    views.setEmptyView(R.id.widget_grid_view, R.id.widget_empty_view)
    
    // Set text color for empty view based on preference
    val emptyTextColor = if (textColor == "black") Color.BLACK else Color.WHITE
    views.setTextColor(R.id.widget_empty_view, emptyTextColor)

    // Set up the PendingIntent template for clicks on grid items
    val clickIntent = Intent(context, TrampolineActivity::class.java)
    val clickPendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        clickIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    views.setPendingIntentTemplate(R.id.widget_grid_view, clickPendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
