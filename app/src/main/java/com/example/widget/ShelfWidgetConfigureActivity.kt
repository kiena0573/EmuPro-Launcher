package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppDatabase
import com.example.data.GameEntity
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
class ShelfWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

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
                            title = { Text("Tùy Chỉnh Giá Đỡ", style = MaterialTheme.typography.titleLarge) }
                        )
                    }
                ) { innerPadding ->
                    ShelfWidgetConfigScreen(
                        modifier = Modifier.padding(innerPadding),
                        appWidgetId = appWidgetId,
                        onConfigure = {
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(RESULT_OK, resultValue)
                            
                            // Trigger immediate update
                            val appWidgetManager = AppWidgetManager.getInstance(this)
                            val provider = appWidgetManager.getAppWidgetInfo(appWidgetId)?.provider?.className
                            if (provider == ShelfWidget2x2Receiver::class.java.name) {
                                updateShelf2x2Widget(this, appWidgetManager, appWidgetId)
                            } else {
                                updateShelfWidget(this, appWidgetManager, appWidgetId)
                            }
                            
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetCropDialog(
    uri: Uri,
    is2x2: Boolean,
    onConfirm: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
    onChangeImage: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    var mode by remember { mutableStateOf("CROP") }
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // The Crop Box
    var boxWidthDp by remember { mutableStateOf(if (is2x2) 160.dp else 320.dp) }
    var boxHeightDp by remember { mutableStateOf(if (is2x2) 160.dp else 120.dp) }

    // Lift offset state out of the mode blocks so they persist
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isInitialized by remember { mutableStateOf(false) }

    // Edit settings
    var blurLevel by remember { mutableStateOf(0f) }
    var grainLevel by remember { mutableStateOf(0f) }
    var brightness by remember { mutableStateOf(0f) } // -1 to 1
    var tintColor by remember { mutableStateOf(android.graphics.Color.TRANSPARENT) }
    var tintAlpha by remember { mutableStateOf(0f) } // 0 to 1

    // Load bitmap
    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    withContext(Dispatchers.Main) { 
                        originalBitmap = bmp 
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            if (originalBitmap == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (mode == "CROP") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { containerSize = it }
                ) {
                    Image(
                        bitmap = originalBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    val boxWidthPx = with(density) { boxWidthDp.toPx() }
                    val boxHeightPx = with(density) { boxHeightDp.toPx() }

                    // Center initially ONLY ONCE
                    LaunchedEffect(containerSize) {
                        if (!isInitialized && containerSize.width > 0 && containerSize.height > 0) {
                            offsetX = (containerSize.width - boxWidthPx) / 2f
                            offsetY = (containerSize.height - boxHeightPx) / 2f
                            isInitialized = true
                        }
                    }

                    // Keep crop box in bounds when resizing
                    LaunchedEffect(boxWidthPx, boxHeightPx, containerSize) {
                        if (isInitialized && containerSize.width > 0 && containerSize.height > 0) {
                            offsetX = offsetX.coerceIn(0f, maxOf(0f, containerSize.width - boxWidthPx))
                            offsetY = offsetY.coerceIn(0f, maxOf(0f, containerSize.height - boxHeightPx))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(density) { offsetX.toDp() },
                                y = with(density) { offsetY.toDp() }
                            )
                            .size(boxWidthDp, boxHeightDp)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(2.dp, Color.White)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxOf(0f, containerSize.width - boxWidthPx))
                                    offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxOf(0f, containerSize.height - boxHeightPx))
                                }
                            }
                    ) {
                        Text(
                            "Kéo vị trí",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    // Top Bar for Actions
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("Hủy", color = Color.White) }
                        TextButton(onClick = onChangeImage) { Text("Đổi Ảnh", color = Color.White) }
                        Button(
                            onClick = {
                                if (isProcessing) return@Button
                                isProcessing = true
                                coroutineScope.launch(Dispatchers.Default) {
                                    try {
                                        val scale = maxOf(
                                            containerSize.width.toFloat() / originalBitmap!!.width,
                                            containerSize.height.toFloat() / originalBitmap!!.height
                                        )
                                        val scaledWidth = originalBitmap!!.width * scale
                                        val scaledHeight = originalBitmap!!.height * scale
                                        val leftPadding = (scaledWidth - containerSize.width) / 2f
                                        val topPadding = (scaledHeight - containerSize.height) / 2f

                                        val cropX = ((offsetX + leftPadding) / scale).toInt().coerceIn(0, originalBitmap!!.width - 1)
                                        val cropY = ((offsetY + topPadding) / scale).toInt().coerceIn(0, originalBitmap!!.height - 1)
                                        val cropW = (boxWidthPx / scale).toInt().coerceAtMost(originalBitmap!!.width - cropX)
                                        val cropH = (boxHeightPx / scale).toInt().coerceAtMost(originalBitmap!!.height - cropY)

                                        val cropped = Bitmap.createBitmap(originalBitmap!!, cropX, cropY, cropW, cropH)
                                        
                                        withContext(Dispatchers.Main) {
                                            croppedBitmap = cropped
                                            mode = "EDIT"
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isProcessing = false
                                    }
                                }
                            },
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Tiếp tục")
                            }
                        }
                    }

                    // Bottom Control Panel
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rộng:", color = Color.White, modifier = Modifier.width(50.dp))
                            Slider(
                                value = boxWidthDp.value,
                                onValueChange = { boxWidthDp = it.dp },
                                valueRange = 50f..400f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Cao:", color = Color.White, modifier = Modifier.width(50.dp))
                            Slider(
                                value = boxHeightDp.value,
                                onValueChange = { boxHeightDp = it.dp },
                                valueRange = 50f..400f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else if (mode == "EDIT" && croppedBitmap != null) {
                // EDIT MODE
                
                // Real-time preview of the effect
                var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
                
                LaunchedEffect(blurLevel, grainLevel, brightness, tintColor, tintAlpha) {
                    isProcessing = true
                    withContext(Dispatchers.Default) {
                        try {
                            val tempBmp = croppedBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
                            
                            // Blur
                            if (blurLevel > 0f) {
                                val effectiveBlur = blurLevel
                                val rs = RenderScript.create(context)
                                
                                var currentBmp = tempBmp
                                if (effectiveBlur > 25f) {
                                    val scale = 25f / effectiveBlur
                                    val w = maxOf(1, (currentBmp.width * scale).toInt())
                                    val h = maxOf(1, (currentBmp.height * scale).toInt())
                                    currentBmp = Bitmap.createScaledBitmap(currentBmp, w, h, true)
                                }
                                
                                val radius = effectiveBlur.coerceAtMost(25f).coerceAtLeast(1f)
                                
                                val input = Allocation.createFromBitmap(rs, currentBmp)
                                val output = Allocation.createTyped(rs, input.type)
                                val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
                                script.setRadius(radius)
                                script.setInput(input)
                                script.forEach(output)
                                output.copyTo(currentBmp)
                                
                                if (currentBmp != tempBmp) {
                                    val scaledBack = Bitmap.createScaledBitmap(currentBmp, tempBmp.width, tempBmp.height, true)
                                    val canvas = android.graphics.Canvas(tempBmp)
                                    val paint = android.graphics.Paint().apply {
                                        isFilterBitmap = true
                                        isAntiAlias = true
                                    }
                                    canvas.drawBitmap(scaledBack, 0f, 0f, paint)
                                    currentBmp.recycle()
                                    scaledBack.recycle()
                                }
                            }
                            
                            val canvas = android.graphics.Canvas(tempBmp)
                            
                            // Brightness
                            if (brightness != 0f) {
                                val paint = android.graphics.Paint()
                                if (brightness < 0f) {
                                    paint.color = android.graphics.Color.argb((-brightness * 255).toInt(), 0, 0, 0)
                                } else {
                                    paint.color = android.graphics.Color.argb((brightness * 255).toInt(), 255, 255, 255)
                                }
                                canvas.drawRect(0f, 0f, tempBmp.width.toFloat(), tempBmp.height.toFloat(), paint)
                            }
                            
                            // Tint
                            if (tintAlpha > 0f && tintColor != android.graphics.Color.TRANSPARENT) {
                                val paint = android.graphics.Paint()
                                val r = android.graphics.Color.red(tintColor)
                                val g = android.graphics.Color.green(tintColor)
                                val b = android.graphics.Color.blue(tintColor)
                                paint.color = android.graphics.Color.argb((tintAlpha * 255).toInt(), r, g, b)
                                canvas.drawRect(0f, 0f, tempBmp.width.toFloat(), tempBmp.height.toFloat(), paint)
                            }
                            
                            // Grain
                            if (grainLevel > 0f) {
                                val pixels = IntArray(tempBmp.width * tempBmp.height)
                                tempBmp.getPixels(pixels, 0, tempBmp.width, 0, 0, tempBmp.width, tempBmp.height)
                                val random = java.util.Random()
                                val grainAlpha = (grainLevel * 2.55f).toInt() // max 255 for 100 level
                                for (i in pixels.indices) {
                                    val color = pixels[i]
                                    val a = android.graphics.Color.alpha(color)
                                    var r = android.graphics.Color.red(color)
                                    var g = android.graphics.Color.green(color)
                                    var b = android.graphics.Color.blue(color)
                                    
                                    val noise = random.nextInt(maxOf(1, grainAlpha * 2)) - grainAlpha
                                    r = (r + noise).coerceIn(0, 255)
                                    g = (g + noise).coerceIn(0, 255)
                                    b = (b + noise).coerceIn(0, 255)
                                    
                                    pixels[i] = android.graphics.Color.argb(a, r, g, b)
                                }
                                tempBmp.setPixels(pixels, 0, tempBmp.width, 0, 0, tempBmp.width, tempBmp.height)
                            }

                            withContext(Dispatchers.Main) {
                                previewBitmap = tempBmp
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isProcessing = false
                        }
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // Preview Image
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Preview",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(boxWidthDp, boxHeightDp)
                                .border(1.dp, Color.White),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { mode = "CROP" }) { Text("Quay lại", color = Color.White) }
                        Button(
                            onClick = {
                                if (previewBitmap != null) {
                                    onConfirm(previewBitmap!!)
                                }
                            },
                            enabled = previewBitmap != null && !isProcessing
                        ) {
                            Text("Hoàn tất")
                        }
                    }
                    
                    // Bottom Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Mờ:", color = Color.White, modifier = Modifier.width(60.dp))
                            Slider(
                                value = blurLevel,
                                onValueChange = { blurLevel = it },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Nhiễu:", color = Color.White, modifier = Modifier.width(60.dp))
                            Slider(
                                value = grainLevel,
                                onValueChange = { grainLevel = it },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sáng:", color = Color.White, modifier = Modifier.width(60.dp))
                            Slider(
                                value = brightness,
                                onValueChange = { brightness = it },
                                valueRange = -1f..1f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Màu phủ:", color = Color.White, modifier = Modifier.width(60.dp))
                            Slider(
                                value = tintAlpha,
                                onValueChange = { tintAlpha = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        val tintColors = listOf(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.BLACK,
                            android.graphics.Color.WHITE,
                            android.graphics.Color.parseColor("#FF5252"), // Red
                            android.graphics.Color.parseColor("#448AFF"), // Blue
                            android.graphics.Color.parseColor("#4CAF50"), // Green
                            android.graphics.Color.parseColor("#FFEB3B"), // Yellow
                            android.graphics.Color.parseColor("#9C27B0")  // Purple
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                        ) {
                            items(tintColors) { colorInt ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(colorInt).copy(alpha = if (colorInt == android.graphics.Color.TRANSPARENT) 0.1f else 1f))
                                        .border(
                                            width = if (tintColor == colorInt) 2.dp else 1.dp,
                                            color = if (tintColor == colorInt) Color.White else Color.Gray,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                        .clickable { tintColor = colorInt },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (colorInt == android.graphics.Color.TRANSPARENT) {
                                        Text("✖", color = Color.Gray, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfWidgetConfigScreen(modifier: Modifier = Modifier, appWidgetId: Int, onConfigure: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("shelf_widget_prefs", Context.MODE_PRIVATE)

    var gameIds by remember { mutableStateOf(listOf(-1, -1, -1, -1)) }
    var selectedMode by remember { mutableStateOf(prefs.getString("widget_${appWidgetId}_mode", "original") ?: "original") }
    var showShelf by remember { mutableStateOf(prefs.getBoolean("widget_${appWidgetId}_show_shelf", true)) }
    val provider = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)?.provider?.className
    val is2x2 = provider == ShelfWidget2x2Receiver::class.java.name
    var bgType by remember { mutableStateOf(prefs.getString("widget_${appWidgetId}_bg", if (is2x2) "material_you" else "default") ?: (if (is2x2) "material_you" else "default")) }

    var games by remember { mutableStateOf<List<GameEntity>>(emptyList()) }
    var targetSlot by remember { mutableStateOf(-1) }
    var showGameSelection by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val initialIds = listOf(
            prefs.getInt("widget_${appWidgetId}_game_1", -1),
            prefs.getInt("widget_${appWidgetId}_game_2", -1),
            prefs.getInt("widget_${appWidgetId}_game_3", -1),
            prefs.getInt("widget_${appWidgetId}_game_4", -1)
        )
        gameIds = initialIds

        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            games = db.gameDao().getAllGamesSync()
        }
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(16.dp)) {
        Text("Cài đặt Giá đỡ Game", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .background(
                    if (showShelf) {
                        if (bgType == "material_you") MaterialTheme.colorScheme.surfaceVariant
                        else if (bgType == "default" || bgType == "screenshot") Color(0xFFE8DEF8)
                        else MaterialTheme.colorScheme.surfaceVariant
                    } else Color.Transparent
                )
                .border(
                    width = if (showShelf) 0.dp else 1.dp,
                    color = if (showShelf) Color.Transparent else Color.LightGray,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(120.dp).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (i in 0..3) {
                    val gId = gameIds[i]
                    val game = games.find { it.id == gId }
                    val contentScale = if (selectedMode == "original") androidx.compose.ui.layout.ContentScale.Fit else androidx.compose.ui.layout.ContentScale.Crop
                    val shape = if (selectedMode == "original") androidx.compose.ui.graphics.RectangleShape else androidx.compose.foundation.shape.RoundedCornerShape(8.dp)

                    Card(
                        modifier = Modifier.weight(1f).aspectRatio(0.75f).padding(4.dp),
                        onClick = { targetSlot = i; showGameSelection = true },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        if (game != null) {
                            coil.compose.AsyncImage(
                                model = if (game.boxartUrl?.startsWith("/") == true) java.io.File(game.boxartUrl) else game.boxartUrl,
                                contentDescription = null,
                                contentScale = contentScale,
                                modifier = Modifier.fillMaxSize().clip(shape)
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha=0.3f), shape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Thêm")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Hình dáng hiển thị", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedMode = "original" }) {
            RadioButton(selected = selectedMode == "original", onClick = { selectedMode = "original" })
            Text("Không giới hạn (Original - Cho các game tỷ lệ tự do)")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedMode = "rounded" }) {
            RadioButton(selected = selectedMode == "rounded", onClick = { selectedMode = "rounded" })
            Text("Bo tròn góc (Rounded)")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showShelf = !showShelf }) {
            Text("Hiển thị Giá đỡ (Shelf UI)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = showShelf, onCheckedChange = { showShelf = it })
        }

        if (showShelf) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nền Giá đỡ", style = MaterialTheme.typography.titleMedium)
            
            var showBgDropdown by remember { mutableStateOf(false) }
            val bgLabels = mapOf(
                "default" to "Mặc định (Trong suốt/Đen)",
                "transparent" to "Trong suốt",
                "gamecard" to "Màu giống Gamecard (Dark mode)",
                "blurred_cover" to "Ảnh bìa mờ ảo (như gradient)",
                "screenshot" to "Ảnh chụp màn hình (Cắt theo vị trí)",
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
                    Text(bgLabels[bgType] ?: "Mặc định")
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

            var pickedUri by remember { mutableStateOf<Uri?>(null) }
            val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) pickedUri = uri
            }

            if (bgType == "screenshot") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chọn Ảnh và Cắt (Blur)")
                }
            }

            if (bgType == "blurred_cover") {
                Spacer(modifier = Modifier.height(8.dp))
                var showGameDropdown by remember { mutableStateOf(false) }
                var blurredGameId by remember { mutableStateOf(prefs.getInt("widget_${appWidgetId}_blurred_game", -1)) }
                val selectedGame = games.find { it.id == blurredGameId }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showGameDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedGame?.title ?: "Chọn Game làm ảnh bìa nền")
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
                                    prefs.edit().putInt("widget_${appWidgetId}_blurred_game", game.id).apply()
                                    showGameDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            if (pickedUri != null) {
                WidgetCropDialog(
                    uri = pickedUri!!,
                    is2x2 = is2x2,
                    onConfirm = { bmp ->
                        // Save the bitmap
                        val file = File(context.filesDir, "widget_${appWidgetId}_screenshot.png")
                        FileOutputStream(file).use { out ->
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        pickedUri = null
                    },
                    onDismiss = { pickedUri = null },
                    onChangeImage = {
                        pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
        }

        var isProcessing by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (isProcessing) return@Button
                isProcessing = true
                coroutineScope.launch(Dispatchers.Default) {
                    try {
                        if (bgType == "blurred_cover") {
                            val blurredGameId = prefs.getInt("widget_${appWidgetId}_blurred_game", -1)
                            val game = games.find { it.id == blurredGameId }
                            if (game != null && game.boxartUrl?.startsWith("/") == true) {
                                val originalBmp = BitmapFactory.decodeFile(game.boxartUrl)
                                if (originalBmp != null) {
                                    val rs = RenderScript.create(context)
                                    val blurred = Bitmap.createScaledBitmap(originalBmp, originalBmp.width / 4, originalBmp.height / 4, true)
                                    val input = Allocation.createFromBitmap(rs, blurred)
                                    val output = Allocation.createTyped(rs, input.type)
                                    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
                                    script.setRadius(25f)
                                    script.setInput(input)
                                    script.forEach(output)
                                    output.copyTo(blurred)
                                    
                                    val finalBmp = Bitmap.createBitmap(blurred.width, blurred.height, Bitmap.Config.ARGB_8888)
                                    val canvas = android.graphics.Canvas(finalBmp)
                                    canvas.drawBitmap(blurred, 0f, 0f, null)
                                    val paint = android.graphics.Paint()
                                    paint.color = android.graphics.Color.argb(150, 0, 0, 0)
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
                                .putInt("widget_${appWidgetId}_game_1", gameIds[0])
                                .putInt("widget_${appWidgetId}_game_2", gameIds[1])
                                .putInt("widget_${appWidgetId}_game_3", gameIds[2])
                                .putInt("widget_${appWidgetId}_game_4", gameIds[3])
                                .putString("widget_${appWidgetId}_mode", selectedMode)
                                .putBoolean("widget_${appWidgetId}_show_shelf", showShelf)
                                .putString("widget_${appWidgetId}_bg", bgType)
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
                    item {
                        ListItem(
                            headlineContent = { Text("Xóa ô này", color = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.clickable {
                                val newIds = gameIds.toMutableList()
                                newIds[targetSlot] = -1
                                gameIds = newIds
                                showGameSelection = false
                            }
                        )
                    }
                    items(games) { game ->
                        ListItem(
                            headlineContent = { Text(game.title) },
                            supportingContent = { Text(game.platform) },
                            modifier = Modifier.clickable {
                                val newIds = gameIds.toMutableList()
                                newIds[targetSlot] = game.id
                                gameIds = newIds
                                showGameSelection = false
                            }
                        )
                    }
                }
            }
        }
    }
}
