package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.AppDatabase
import com.example.data.GameEntity
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class GameListWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val appColorPref = getSharedPreferences("app_settings", Context.MODE_PRIVATE).getString("app_color", "dynamic") ?: "dynamic"

        setContent {
            MyApplicationTheme(appColor = appColorPref) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Tùy Chỉnh Widget Danh sách", style = MaterialTheme.typography.titleLarge) }
                        )
                    }
                ) { innerPadding ->
                    GameListWidgetConfigScreen(
                        modifier = Modifier.padding(innerPadding),
                        appWidgetId = appWidgetId,
                        onConfigure = {
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(RESULT_OK, resultValue)
                            
                            val appWidgetManager = AppWidgetManager.getInstance(this)
                            updateGameListWidget(this, appWidgetManager, appWidgetId)
                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, com.example.R.id.widget_grid_view)
                            
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListWidgetConfigScreen(modifier: Modifier = Modifier, appWidgetId: Int, onConfigure: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("game_list_widget_prefs", Context.MODE_PRIVATE)

    var bgType by remember { mutableStateOf(prefs.getString("widget_${appWidgetId}_bg", "material_you") ?: "material_you") }
    var textColor by remember { mutableStateOf(prefs.getString("widget_${appWidgetId}_text_color", "white") ?: "white") }
    var blurredGameId by remember { mutableStateOf(-1) }
    
    var games by remember { mutableStateOf<List<GameEntity>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            games = db.gameDao().getAllGamesSync()
        }
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        
        Text("Nền Widget", style = MaterialTheme.typography.titleMedium)
        
        var showBgDropdown by remember { mutableStateOf(false) }
        val bgLabels = mapOf(
            "transparent" to "Trong suốt",
            "gamecard" to "Màu giống Gamecard (Dark mode)",
            "blurred_cover" to "Ảnh bìa mờ ảo (như gradient)",
            "material_you" to "Màu Material You (Theo hệ thống)",
            "asian_dragon" to "Hoa văn rồng châu Á",
            "banh_chung" to "Hoa văn lá bánh chưng",
            "dong_leaf" to "Hoa văn lá dong",
            "mooncake" to "Hoa văn hộp bánh trung thu"
        )
        
        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedButton(
                onClick = { showBgDropdown = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(bgLabels[bgType] ?: "Chọn nền")
            }
            DropdownMenu(
                expanded = showBgDropdown,
                onDismissRequest = { showBgDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                bgLabels.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            bgType = key
                            showBgDropdown = false
                        }
                    )
                }
            }
        }

        if (bgType == "transparent") {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Màu chữ", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { textColor = "white" }) {
                RadioButton(selected = textColor == "white", onClick = { textColor = "white" })
                Text("Trắng (White)")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { textColor = "black" }) {
                RadioButton(selected = textColor == "black", onClick = { textColor = "black" })
                Text("Đen (Black)")
            }
        }

        if (bgType == "blurred_cover") {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chọn game làm ảnh bìa nền", style = MaterialTheme.typography.titleMedium)
            
            var showGameDropdown by remember { mutableStateOf(false) }
            val selectedGame = games.find { it.id == blurredGameId }
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(
                    onClick = { showGameDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedGame?.title ?: "Chọn Game")
                }
                DropdownMenu(
                    expanded = showGameDropdown,
                    onDismissRequest = { showGameDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    games.forEach { game ->
                        DropdownMenuItem(
                            text = { Text(game.title) },
                            onClick = {
                                blurredGameId = game.id
                                showGameDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (isProcessing) return@Button
                isProcessing = true
                coroutineScope.launch(Dispatchers.Default) {
                    try {
                        if (bgType == "blurred_cover" && blurredGameId != -1) {
                            val game = games.find { it.id == blurredGameId }
                            if (game != null && game.boxartUrl?.startsWith("/") == true) {
                                val originalBmp = BitmapFactory.decodeFile(game.boxartUrl)
                                if (originalBmp != null) {
                                    // Blur and darken
                                    val rs = RenderScript.create(context)
                                    // scale down for heavy blur
                                    var blurred = Bitmap.createScaledBitmap(originalBmp, originalBmp.width / 4, originalBmp.height / 4, true)
                                    val input = Allocation.createFromBitmap(rs, blurred)
                                    val output = Allocation.createTyped(rs, input.type)
                                    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
                                    script.setRadius(25f) // Max radius
                                    script.setInput(input)
                                    script.forEach(output)
                                    output.copyTo(blurred)
                                    
                                    // draw black overlay
                                    val finalBmp = Bitmap.createBitmap(blurred.width, blurred.height, Bitmap.Config.ARGB_8888)
                                    val canvas = android.graphics.Canvas(finalBmp)
                                    canvas.drawBitmap(blurred, 0f, 0f, null)
                                    val paint = android.graphics.Paint()
                                    paint.color = android.graphics.Color.argb(150, 0, 0, 0) // Dark overlay
                                    canvas.drawRect(0f, 0f, blurred.width.toFloat(), blurred.height.toFloat(), paint)
                                    
                                    val file = File(context.filesDir, "widget_${appWidgetId}_screenshot.png")
                                    FileOutputStream(file).use { out ->
                                        finalBmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    }
                                }
                            }
                        }
                        
                        withContext(Dispatchers.Main) {
                            prefs.edit()
                                .putString("widget_${appWidgetId}_bg", bgType)
                                .putString("widget_${appWidgetId}_text_color", textColor)
                                .apply()
                            onConfigure()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isProcessing = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Hoàn tất")
            }
        }
    }
}
