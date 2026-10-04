package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.GameEntity
import java.io.File

class GameListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return GameListRemoteViewsFactory(this.applicationContext, intent)
    }
}

class GameListRemoteViewsFactory(private val context: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    
    private val appWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    private var games: List<GameEntity> = emptyList()
    private var textColor: String = "white"
    
    override fun onCreate() {
        // In onCreate, you can initialize data, but querying should be done in onDataSetChanged
    }

    override fun onDataSetChanged() {
        // Read preferences
        val prefs = context.getSharedPreferences("game_list_widget_prefs", Context.MODE_PRIVATE)
        textColor = prefs.getString("widget_${appWidgetId}_text_color", "white") ?: "white"
        
        // Fetch all games
        val db = AppDatabase.getDatabase(context)
        games = kotlinx.coroutines.runBlocking { db.gameDao().getAllGamesSync() }
    }

    override fun onDestroy() {
        games = emptyList()
    }

    override fun getCount(): Int {
        return games.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val game = games[position]
        val rv = RemoteViews(context.packageName, R.layout.game_list_widget_item)
        
        // Set title
        rv.setTextViewText(R.id.item_game_title, game.title)
        val color = if (textColor == "black") Color.BLACK else Color.WHITE
        rv.setTextColor(R.id.item_game_title, color)
        
        // Set image
        if (game.boxartUrl?.startsWith("/") == true) {
            val file = File(game.boxartUrl)
            if (file.exists()) {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                if (bmp != null) {
                    rv.setImageViewBitmap(R.id.item_game_cover, bmp)
                }
            }
        }
        
        // Set fill in intent
        val fillInIntent = Intent().apply {
            putExtra("EXTRA_GAME_ID", game.id)
        }
        rv.setOnClickFillInIntent(R.id.item_game_cover, fillInIntent)
        
        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        return null // Use default loading view
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return games[position].id.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
