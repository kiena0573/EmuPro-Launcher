package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.AppDatabase
import com.example.data.GameEntity
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
class WidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        // Find the widget id from the intent.
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val appColorPref = getSharedPreferences("app_settings", Context.MODE_PRIVATE).getString("app_color", "dynamic") ?: "dynamic"

        setContent {
            MyApplicationTheme(appColor = appColorPref) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Thêm Game Mới Ra Màn Hình", style = MaterialTheme.typography.titleLarge) }
                        )
                    }
                ) { innerPadding ->
                    WidgetConfigScreen(
                        modifier = Modifier.padding(innerPadding),
                        onConfigure = { gameId, scaleMode, padding, showBg ->
                            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putInt("widget_${appWidgetId}_game_id", gameId)
                                    .putString("widget_${appWidgetId}_scale", scaleMode)
                                    .putInt("widget_${appWidgetId}_padding", padding)
                                    .putBoolean("widget_${appWidgetId}_show_background", showBg)
                                    .commit()
                                
                                try {
                                    updateAppWidget(this@WidgetConfigureActivity, AppWidgetManager.getInstance(this@WidgetConfigureActivity), appWidgetId)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                
                                val updateIntent = Intent(this@WidgetConfigureActivity, GameWidgetReceiver::class.java).apply {
                                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                                }
                                sendBroadcast(updateIntent)
                                
                                launch(kotlinx.coroutines.Dispatchers.Main) {
                                    val resultValue = Intent().apply {
                                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                    }
                                    setResult(RESULT_OK, resultValue)
                                    finish()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(modifier: Modifier = Modifier, onConfigure: (Int, String, Int, Boolean) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val games by db.gameDao().getAllGames().collectAsState(initial = emptyList())
    
    val prefs = remember { context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE) }
    val activity = context as? android.app.Activity
    val appWidgetId = activity?.intent?.extras?.getInt(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
    
    var selectedGameId by remember { mutableIntStateOf(prefs.getInt("widget_${appWidgetId}_game_id", -1)) }
    var selectedScaleMode by remember { mutableStateOf(prefs.getString("widget_${appWidgetId}_scale", "crop") ?: "crop") } // "crop" or "fit"
    var imagePadding by remember { mutableFloatStateOf(prefs.getInt("widget_${appWidgetId}_padding", 0).toFloat()) }
    var showBackground by remember { mutableStateOf(prefs.getBoolean("widget_${appWidgetId}_show_background", false)) }

    var showGameSelection by remember { mutableStateOf(false) }

    val selectedGame = games.find { it.id == selectedGameId }

    Column(modifier = modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (showBackground) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (showBackground) 4.dp else 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (selectedGame?.boxartUrl != null) {
                        val f = selectedGame.boxartUrl
                        val data = if (f!!.startsWith("/")) java.io.File(f) else f
                        coil.compose.AsyncImage(
                            model = data,
                            contentDescription = null,
                            contentScale = if (selectedScaleMode == "fit") androidx.compose.ui.layout.ContentScale.Fit else androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().padding(imagePadding.dp)
                        )
                    } else {
                        Icon(Icons.Default.Gamepad, contentDescription = null, modifier = Modifier.size(64.dp).align(Alignment.Center))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedCard(onClick = { showGameSelection = true }, modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(selectedGame?.title ?: "Chưa chọn Game") },
                supportingContent = { Text("Nhấn để chọn game") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Cài đặt hiển thị Boxart", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedScaleMode = "crop" }) {
            RadioButton(selected = selectedScaleMode == "crop", onClick = { selectedScaleMode = "crop" })
            Text("Maximized Fitting (Phủ kín)")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedScaleMode = "fit" }) {
            RadioButton(selected = selectedScaleMode == "fit", onClick = { selectedScaleMode = "fit" })
            Text("Original (Giữ nguyên tỉ lệ)")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Thu nhỏ ảnh (Padding): ${imagePadding.toInt()} dp", style = MaterialTheme.typography.titleMedium)
        androidx.compose.material3.Slider(
            value = imagePadding,
            onValueChange = { imagePadding = it },
            valueRange = 0f..32f,
            steps = 31
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBackground = !showBackground }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hiển thị viền nền", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = showBackground,
                onCheckedChange = { showBackground = it }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (selectedGameId != -1) onConfigure(selectedGameId, selectedScaleMode, imagePadding.toInt(), showBackground) },
            enabled = selectedGameId != -1,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hoàn tất")
        }
    }

    if (showGameSelection) {
        ModalBottomSheet(
            onDismissRequest = { showGameSelection = false }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Text(
                    "Chọn Game", 
                    style = MaterialTheme.typography.titleLarge, 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(games) { game ->
                        ListItem(
                            headlineContent = { Text(game.title) },
                            supportingContent = { Text(game.platform) },
                            modifier = Modifier.clickable {
                                selectedGameId = game.id
                                showGameSelection = false
                            }
                        )
                    }
                }
            }
        }
    }
}
