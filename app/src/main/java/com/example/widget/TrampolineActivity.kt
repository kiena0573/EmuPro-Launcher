package com.example.widget

import android.app.Activity
import android.os.Bundle
import com.example.data.AppDatabase
import com.example.utils.EmulatorIntentFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrampolineActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameId = intent.getIntExtra("EXTRA_GAME_ID", -1)
        val isRandom = intent.getBooleanExtra("EXTRA_RANDOM_GAME", false)
        if (gameId != -1 || isRandom) {
            EmulatorIntentFactory.loadCustomPlatforms(this)
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(this@TrampolineActivity)
                val game = if (isRandom) {
                    val allGames = db.gameDao().getAllGamesSync()
                    if (allGames.isNotEmpty()) allGames.random() else null
                } else {
                    db.gameDao().getGameById(gameId)
                }
                
                if (game != null) {
                    launch(Dispatchers.Main) {
                        EmulatorIntentFactory.launchGame(this@TrampolineActivity, game.platform, game.filePath)
                        finish()
                    }
                } else {
                    launch(Dispatchers.Main) { finish() }
                }
            }
        } else {
            finish()
        }
    }
}
