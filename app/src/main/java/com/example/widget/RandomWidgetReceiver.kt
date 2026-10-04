package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.net.Uri
import com.example.R
import com.example.data.AppDatabase
import com.example.data.GameEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RandomWidgetReceiver : AppWidgetProvider() {
    
    companion object {
        const val ACTION_REFRESH_RANDOM = "com.example.ACTION_REFRESH_RANDOM"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateRandomAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_RANDOM) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                updateRandomAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }
}

internal fun updateRandomAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.widget_random)

    val bgColor = com.example.ui.theme.getWidgetBackgroundColor(context)
    val fgColor = com.example.ui.theme.getWidgetForegroundColor(context)

    views.setInt(R.id.widget_random_bg, "setColorFilter", bgColor)
    views.setInt(R.id.widget_random_icon, "setColorFilter", fgColor)

    // Launch random game intent via TrampolineActivity
    val launchIntent = Intent(context, TrampolineActivity::class.java).apply {
        putExtra("EXTRA_RANDOM_GAME", true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_random_root, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
