package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.example.data.AppDatabase
import com.example.data.GameEntity
import com.example.ui.theme.MyApplicationTheme
import com.example.widget.TrampolineActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun getBlurredWallpaper(context: Context, blurLevel: Float, forceExtract: Boolean = false): Bitmap? {
    try {
        var bitmap: Bitmap? = null
        val file = java.io.File(context.filesDir, "folder_bg.png")
        if (!forceExtract && file.exists()) {
            bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        }
        
        if (bitmap == null || forceExtract) {
            val wallpaperManager = android.app.WallpaperManager.getInstance(context)
            val drawable = wallpaperManager.drawable ?: return null
            bitmap = drawable.toBitmap()
            // Save it for next time
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
        }
        
        // Scale down more for higher blur levels to improve performance and blur effect
        val scale = if (blurLevel > 50f) 0.1f else 0.2f
        val width = maxOf(1, (bitmap.width * scale).toInt())
        val height = maxOf(1, (bitmap.height * scale).toInt())
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        
        // Radius max is 25f for RenderScript
        val radius = maxOf(1f, minOf(25f, blurLevel / 2f))
        
        val rs = android.renderscript.RenderScript.create(context)
        val input = android.renderscript.Allocation.createFromBitmap(rs, scaledBitmap)
        val output = android.renderscript.Allocation.createTyped(rs, input.type)
        val script = android.renderscript.ScriptIntrinsicBlur.create(rs, android.renderscript.Element.U8_4(rs))
        script.setRadius(radius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(scaledBitmap)
        rs.destroy()
        
        return scaledBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

class FolderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        
        setContent {
            MyApplicationTheme {
                FolderScreen(
                    onClose = { finish() },
                    onGameClick = { game -> 
                        val intent = Intent(this, TrampolineActivity::class.java).apply {
                            putExtra("EXTRA_GAME_ID", game.id)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FolderScreen(onClose: () -> Unit, onGameClick: (GameEntity) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val games by db.gameDao().getAllGames().collectAsState(initial = emptyList())
    val platforms = remember(games) { games.map { it.platform }.distinct().sorted() }
    
    val pagerState = rememberPagerState(pageCount = { platforms.size + 1 })
    
    val prefs = context.getSharedPreferences("folder_settings", Context.MODE_PRIVATE)
    val blurLevel = prefs.getFloat("blur_level", 50f)
    
    var blurredWallpaper by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(blurLevel) {
        withContext(Dispatchers.IO) {
            val bitmap = getBlurredWallpaper(context, blurLevel)
            withContext(Dispatchers.Main) {
                blurredWallpaper = bitmap
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        androidx.activity.compose.BackHandler {
            onClose()
        }
        
        if (blurredWallpaper != null) {
            androidx.compose.foundation.Image(
                bitmap = blurredWallpaper!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Consume click
                )
        ) {
            // Title
            Text(
                text = "Game Launcher Fold",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Normal),
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 32.dp)
            )
            
            // Icons row (Add, Color)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp).padding(4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier.size(24.dp).background(Color(0xFFA5D6A7), CircleShape)
                )
            }
            
            // Folder Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val pageGames = if (page == 0) games else {
                        val platform = platforms[page - 1]
                        games.filter { it.platform == platform }
                    }
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = pageGames, key = { it.id }) { game ->
                            FolderGameItem(modifier = Modifier.animateItem(), game = game, onClick = { onGameClick(game) })
                        }
                    }
                }
            }
            
            // Pager Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.4f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FolderGameItem(modifier: Modifier = Modifier, game: GameEntity, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
        ) {
            AsyncImage(
                model = game.boxartUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = game.title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
