package com.example.ui.screens

import android.content.Context
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.unit.sp
import com.example.utils.simpleVerticalScrollbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.GameEntity
import com.example.ui.theme.bounceClick
import com.example.ui.theme.pressScale
import com.example.ui.viewmodels.MainViewModel
import com.example.utils.EmulatorIntentFactory

import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, hazeState: dev.chrisbanes.haze.HazeState? = null, onNavigate: ((Int) -> Unit)? = null) {
    val gamesState by viewModel.games.collectAsState()
    val games = gamesState ?: emptyList()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val globalGameCardScale by viewModel.globalGameCardScale.collectAsState()
    val multiLineLabel by viewModel.multiLineLabel.collectAsState()
    val appColor by viewModel.appColor.collectAsState()
    val gameTitleFontWeight by viewModel.gameTitleFontWeight.collectAsState()
    val gameTitleFontSize by viewModel.gameTitleFontSize.collectAsState()
    val mainScreenStyle by viewModel.mainScreenStyle.collectAsState()
    val chipHazePreset by viewModel.chipHazePreset.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600 && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Edit Game Dialog State
    var gameToEdit by remember { mutableStateOf<GameEntity?>(null) }
    var gameToDelete by remember { mutableStateOf<GameEntity?>(null) }
    var optionsDialogGame by remember { mutableStateOf<GameEntity?>(null) }
    
    val lastSelectedPlatform by viewModel.lastSelectedPlatform.collectAsState()
    val platforms = listOf("Tất cả") + games.map { it.platform }.distinct().sorted()
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { platforms.size })
    val coroutineScope = rememberCoroutineScope()
    
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(gamesState) {
        if (!isLoaded && gamesState != null) {
            val idx = platforms.indexOf(lastSelectedPlatform)
            if (idx >= 0) {
                pagerState.scrollToPage(idx)
            }
            isLoaded = true
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (isLoaded && gamesState != null && platforms.isNotEmpty() && pagerState.currentPage < platforms.size) {
            viewModel.saveLastSelectedPlatform(context, platforms[pagerState.currentPage])
        }
    }

    val currentPlatform = if (platforms.isNotEmpty() && pagerState.currentPage < platforms.size) platforms[pagerState.currentPage] else "Tất cả"

    val safeTabIndex = pagerState.currentPage.coerceIn(0, (platforms.size - 1).coerceAtLeast(0))

    val finalChipHazeStyle = when (chipHazePreset) {
        "Ultra Thin" -> HazeMaterials.ultraThin()
        "Thin" -> HazeMaterials.thin()
        "Regular" -> HazeMaterials.regular()
        "Thick" -> HazeMaterials.thick()
        "Ultra Thick" -> HazeMaterials.ultraThick()
        else -> HazeMaterials.thin()
    }

    val customWallpaperUri by viewModel.customWallpaperUri.collectAsState()
    val allGamesWallpaperUri by viewModel.allGamesWallpaperUri.collectAsState()
    val currentWallpaperUri = if (currentPlatform == "Tất cả" && allGamesWallpaperUri != null) allGamesWallpaperUri else customWallpaperUri
    val enableWallpaperBlur by viewModel.enableWallpaperBlur.collectAsState()
    val wallpaperBlurRadius by viewModel.wallpaperBlurRadius.collectAsState()
    var isWallpaperDark by remember { mutableStateOf<Boolean?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentWallpaperUri != null) {
            val uri = android.net.Uri.parse(currentWallpaperUri)
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (enableWallpaperBlur) Modifier.blur(wallpaperBlurRadius.dp) else Modifier),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                onSuccess = { result ->
                    val drawable = result.result.drawable
                    if (drawable is android.graphics.drawable.BitmapDrawable) {
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                val bitmap = drawable.bitmap
                                val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 1, 1, false)
                                val color = scaled.getPixel(0, 0)
                                val r = android.graphics.Color.red(color)
                                val g = android.graphics.Color.green(color)
                                val b = android.graphics.Color.blue(color)
                                val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
                                val isDark = luminance < 128
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    isWallpaperDark = isDark
                                }
                            } catch (e: Exception) {
                                // Ignored
                            }
                        }
                    }
                }
            )
        } else {
            isWallpaperDark = null
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp), // We will handle insets ourselves
        topBar = {
            if (mainScreenStyle == "style2") {
                val containerColor = Color.Transparent
                val titleColor = MaterialTheme.colorScheme.secondary
                val navIconColor = MaterialTheme.colorScheme.secondary
                val actionIconColor = MaterialTheme.colorScheme.secondary
                
                CenterAlignedTopAppBar(
                    title = { if (currentPlatform != "Tất cả") Text(currentPlatform, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate?.invoke(2) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigate?.invoke(1) }) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = containerColor,
                        navigationIconContentColor = navIconColor,
                        actionIconContentColor = actionIconColor,
                        titleContentColor = titleColor
                    )
                )
            } else {
                if (platforms.size > 1) {
                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                    val surfaceColor = MaterialTheme.colorScheme.background

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp,
                                    bottom = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val context = LocalContext.current
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 8.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .then(if (hazeState != null) Modifier.hazeChild(state = hazeState, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), style = finalChipHazeStyle) else Modifier)
                                    .background(if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.tertiaryContainer)
                                    .bounceClick {
                                        val randomGame = games.randomOrNull()
                                        if (randomGame != null) {
                                            com.example.utils.EmulatorIntentFactory.launchGame(context, randomGame.platform, randomGame.filePath)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(com.example.R.drawable.ic_casino),
                                    contentDescription = "Chơi ngẫu nhiên",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
                            LaunchedEffect(safeTabIndex) {
                                lazyListState.animateScrollToItem(safeTabIndex)
                            }

                            androidx.compose.foundation.lazy.LazyRow(
                                state = lazyListState,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(platforms.size, key = { platforms[it] }) { index ->
                                    val title = platforms[index]
                                    Box(
                                        modifier = Modifier
                                            .animateItem()
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                            .then(if (hazeState != null) Modifier.hazeChild(state = hazeState, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), style = finalChipHazeStyle) else Modifier)
                                            .background(if (safeTabIndex == index) (if (hazeState != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondaryContainer) else (if (hazeState != null) Color.Transparent else MaterialTheme.colorScheme.surface))
                                            .bounceClick {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(title, style = MaterialTheme.typography.labelLarge, color = if (safeTabIndex == index) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        },
        floatingActionButton = {}
    ) { padding ->
        val isAnyDialogOpen = gameToEdit != null || gameToDelete != null || isScanning
        val blurRadius = 0.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
                .then(if (hazeState != null) Modifier.haze(hazeState) else Modifier)
        ) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                    val platformGames = remember(games, searchQuery, page, platforms) {
                        games.filter {
                            val matchesSearch = searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.platform.contains(searchQuery, ignoreCase = true)
                            val matchesPlatform = page == 0 || (page < platforms.size && it.platform == platforms[page])
                            matchesSearch && matchesPlatform
                        }
                    }

                    if (platformGames.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (gamesState != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Gamepad, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "Chưa có game nào.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                androidx.compose.material3.CircularProgressIndicator()
                            }
                        }
                    } else {
                        val bottomPadding = if (mainScreenStyle == "style2") 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                        val actualColumns = if (isTablet) gridColumns * 2 else gridColumns
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(actualColumns),
                            contentPadding = PaddingValues(
                                start = 16.dp, 
                                top = padding.calculateTopPadding(), 
                                end = 16.dp, 
                                bottom = bottomPadding
                            ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize().simpleVerticalScrollbar(gridState)
                        ) {
                            items(platformGames, key = { it.id }) { game ->
                                GameCard(
                                    game = game, 
                                    globalScale = globalGameCardScale,
                                    mainScreenStyle = mainScreenStyle,
                                    multiLineLabel = multiLineLabel,
                                    gameTitleFontWeight = gameTitleFontWeight,
                                    gameTitleFontSize = gameTitleFontSize,
                                    isWallpaperDark = isWallpaperDark,
                                    onClick = { EmulatorIntentFactory.launchGame(context, game.platform, game.filePath) },
                                    onOptionsClick = { optionsDialogGame = game }
                                )
                            }
                        }
                    }
                }

            val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
            val isBgDark = isWallpaperDark ?: isDarkTheme
            val fadeBaseColor = if (isBgDark) Color.Black else MaterialTheme.colorScheme.surface
            val topAlphas = if (isBgDark) listOf(0.80f, 0.50f, 0.18f, 0f) else listOf(0.55f, 0.35f, 0.12f, 0f)
            val bottomAlphas = if (isBgDark) listOf(0f, 0.20f, 0.75f) else listOf(0f, 0.12f, 0.45f)

            val enableFadeoutDefault by viewModel.enableFadeoutDefault.collectAsState()
            val enableFadeoutDelta by viewModel.enableFadeoutDelta.collectAsState()
            val shouldShowTopFade = (mainScreenStyle == "style1" && enableFadeoutDefault) || (mainScreenStyle == "style2" && enableFadeoutDelta)
            val shouldShowBottomFade = mainScreenStyle == "style2" && enableFadeoutDelta

            // Top fade out overlay - soft and non-glaring in light mode
            if (shouldShowTopFade) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    fadeBaseColor.copy(alpha = topAlphas[0]),
                                    fadeBaseColor.copy(alpha = topAlphas[1]),
                                    fadeBaseColor.copy(alpha = topAlphas[2]),
                                    fadeBaseColor.copy(alpha = topAlphas[3])
                                )
                            )
                        )
                )
            }
            // Bottom fade out overlay
            if (shouldShowBottomFade) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    fadeBaseColor.copy(alpha = bottomAlphas[0]),
                                    fadeBaseColor.copy(alpha = bottomAlphas[1]),
                                    fadeBaseColor.copy(alpha = bottomAlphas[2])
                                )
                            )
                        )
                )
            }

            if (mainScreenStyle == "style2" && platforms.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(platforms.size) { index ->
                        val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        }
    }
    }

    if (optionsDialogGame != null) {
        val game = optionsDialogGame!!
        Dialog(
            onDismissRequest = { optionsDialogGame = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { optionsDialogGame = null },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* absorb taps on dialog */ },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = game.platform,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        TextButton(
                            onClick = {
                                val target = game
                                optionsDialogGame = null
                                gameToEdit = target
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Chỉnh sửa", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        TextButton(
                            onClick = {
                                val target = game
                                optionsDialogGame = null
                                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
                                    val myProvider = android.content.ComponentName(context, com.example.widget.GameWidgetReceiver::class.java)
                                    val successCallback = Intent(context, com.example.widget.GameWidgetReceiver::class.java).apply {
                                        action = "com.example.ACTION_APPWIDGET_PINNED"
                                        putExtra("EXTRA_GAME_ID", target.id)
                                    }
                                    val successPendingIntent = android.app.PendingIntent.getBroadcast(
                                        context, target.id, successCallback, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                    )
                                    val bundle = android.os.Bundle()
                                    try {
                                        val previewViews = android.widget.RemoteViews(context.packageName, com.example.R.layout.widget_game)
                                        if (target.boxartUrl?.startsWith("/") == true) {
                                            val bitmap = android.graphics.BitmapFactory.decodeFile(target.boxartUrl)
                                            val isCrop = target.imageScale == "fit"
                                            if (bitmap != null) {
                                                if (!isCrop) {
                                                    previewViews.setImageViewBitmap(com.example.R.id.widget_image_fit, bitmap)
                                                    previewViews.setViewVisibility(com.example.R.id.widget_image_fit, android.view.View.VISIBLE)
                                                    previewViews.setViewVisibility(com.example.R.id.widget_image_crop, android.view.View.GONE)
                                                } else {
                                                    previewViews.setImageViewBitmap(com.example.R.id.widget_image_crop, bitmap)
                                                    previewViews.setViewVisibility(com.example.R.id.widget_image_crop, android.view.View.VISIBLE)
                                                    previewViews.setViewVisibility(com.example.R.id.widget_image_fit, android.view.View.GONE)
                                                }
                                            }
                                        }
                                        bundle.putParcelable(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, previewViews)
                                    } catch (e: Exception) { e.printStackTrace() }
                                    appWidgetManager.requestPinAppWidget(myProvider, bundle, successPendingIntent)
                                    Toast.makeText(context, "Vui lòng xác nhận thêm widget!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Trình khởi chạy không hỗ trợ ghim Widget", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Crop, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Ghim Widget Màn Hình", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        TextButton(
                            onClick = {
                                val target = game
                                optionsDialogGame = null
                                gameToDelete = target
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Xóa Game", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { optionsDialogGame = null },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text("Đóng", color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (gameToDelete != null) {
        val game = gameToDelete!!
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa game '${game.title}' khỏi thư viện không?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.deleteGame(game.id)
                        gameToDelete = null 
                    }
                ) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) { Text("Hủy") }
            }
        )
    }

    if (gameToEdit != null) {
        EditGameDialog(
            game = gameToEdit!!,
            viewModel = viewModel,
            onDismiss = { gameToEdit = null },
            onSave = { updatedGame ->
                viewModel.updateGameWithImage(context, updatedGame)
                gameToEdit = null
            }
        )
    }

}

@Composable
fun EditGameDialog(game: GameEntity, viewModel: MainViewModel, onDismiss: () -> Unit, onSave: (GameEntity) -> Unit) {
    var editTitle by remember(game) { mutableStateOf(game.title) }
    var editBoxartUrl by remember(game) { mutableStateOf(game.boxartUrl ?: "") }
    var ps2Serial by remember { mutableStateOf("") }
    var editFilePath by remember(game) { mutableStateOf(game.filePath) }
    
    var showBoxartSearch by remember { mutableStateOf(false) }

    // New fields
    var showLabel by remember(game) { mutableStateOf(game.showLabel) }
    var labelInside by remember(game) { mutableStateOf(game.labelInside) }
    var cardStyle by remember(game) { mutableStateOf(game.cardStyle) }
    var imageScale by remember(game) { mutableStateOf(game.imageScale) }
    var sizeScale by remember(game) { mutableStateOf(game.sizeScale) }
    
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val boxartsDir = java.io.File(context.filesDir, "boxarts")
                if (!boxartsDir.exists()) {
                    boxartsDir.mkdirs()
                }
                
                // standard sanitized name
                val safeTitle = game.title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                val fileName = "${game.platform}_${safeTitle}_${System.currentTimeMillis()}.png"
                val file = java.io.File(boxartsDir, fileName)
                
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                editBoxartUrl = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                editBoxartUrl = it.toString()
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch(e:Exception){}
            editFilePath = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa Game") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = { Text("Tên Game") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editFilePath,
                    onValueChange = { editFilePath = it },
                    label = { Text("Đường dẫn File (ROM/ISO)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { filePicker.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Chọn lại File (Nếu mất quyền truy cập)")
                }
                
                OutlinedTextField(
                    value = editBoxartUrl,
                    onValueChange = { editBoxartUrl = it },
                    label = { Text("Link Ảnh / File URN") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Hoặc Chọn Ảnh Từ Máy")
                }
                androidx.compose.material3.OutlinedButton(onClick = { showBoxartSearch = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Tìm Bìa Trực Tuyến")
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                Text("Tùy chỉnh Game Card", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showLabel, onCheckedChange = { showLabel = it })
                    Text("Hiển thị tên game")
                }
                if (showLabel) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !labelInside, onClick = { labelInside = false })
                        Text("Bên ngoài")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = labelInside, onClick = { labelInside = true })
                        Text("Bên trong")
                    }
                }
                
                Text("Phong cách thẻ game", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { cardStyle = "default" }) {
                    RadioButton(selected = cardStyle == "default", onClick = { cardStyle = "default" })
                    Text("Khung mặc định (Material)")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { cardStyle = "switch" }) {
                    RadioButton(selected = cardStyle == "switch", onClick = { cardStyle = "switch" })
                    Text("Khung Switch")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { cardStyle = "ds" }) {
                    RadioButton(selected = cardStyle == "ds", onClick = { cardStyle = "ds" })
                    Text("Khung DS")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { cardStyle = "transparent" }) {
                    RadioButton(selected = cardStyle == "transparent", onClick = { cardStyle = "transparent" })
                    Text("Không khung")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { cardStyle = "clear" }) {
                    RadioButton(selected = cardStyle == "clear", onClick = { cardStyle = "clear" })
                    Text("Khung trong suốt")
                }

                Text("Kiểu hiển thị Boxart", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { imageScale = "original" }) {
                    RadioButton(selected = imageScale == "original", onClick = { imageScale = "original" })
                    Text("Original (Ảnh gốc, không bo, vừa khung)")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { imageScale = "fit" }) {
                    RadioButton(selected = imageScale == "fit", onClick = { imageScale = "fit" })
                    Text("Fit (Phóng to, bo tròn 4 góc)")
                }

                Text("Kích cỡ GameCard", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                Slider(
                    value = sizeScale,
                    onValueChange = { sizeScale = it },
                    valueRange = 0.5f..2.0f,
                    steps = 14
                )
                Text("Scale: ${String.format("%.1f", sizeScale)}x", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.End))
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                OutlinedTextField(
                    value = ps2Serial,
                    onValueChange = { ps2Serial = it },
                    label = { Text("Serial PS2 (VD: SLUS-21376)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { 
                    if (ps2Serial.isNotBlank()) {
                        editBoxartUrl = "https://raw.githubusercontent.com/xlenore/ps2-covers/main/covers/default/${ps2Serial}.jpg"
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Lấy bìa PS2 (Theo Github)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(game.copy(
                    title = editTitle,
                    filePath = editFilePath,
                    boxartUrl = editBoxartUrl.ifBlank { null },
                    showLabel = showLabel,
                    labelInside = labelInside,
                    cardStyle = cardStyle,
                    imageScale = imageScale,
                    sizeScale = sizeScale
                ))
            }) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )

    if (showBoxartSearch) {
        BoxartSearchDialog(
            gameTitle = editTitle,
            platform = game.platform,
            viewModel = viewModel,
            onDismiss = { showBoxartSearch = false },
            onSelect = { url -> 
                editBoxartUrl = url
                showBoxartSearch = false 
            }
        )
    }
}

@Composable
fun BoxartSearchDialog(gameTitle: String, platform: String, viewModel: MainViewModel, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    var results by remember { mutableStateOf<List<String>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    var inputPlatform by remember { mutableStateOf(platform) }
    var inputKeyword by remember { mutableStateOf("boxart") }
    val keywordOptions = listOf("boxart", "Disc boxart", "gamecard", "cover", "trống (không dùng)")
    var isKeywordDropdownExpanded by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tải bìa trực tuyến") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Tùy chọn tìm kiếm:", style = MaterialTheme.typography.titleSmall)
                Text("- Tên game: $gameTitle", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp, top = 4.dp))
                
                OutlinedTextField(
                    value = inputPlatform,
                    onValueChange = { inputPlatform = it },
                    label = { Text("Hỗ trợ hệ máy") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = inputKeyword,
                        onValueChange = { inputKeyword = it },
                        label = { Text("Từ khóa (nhập hoặc chọn)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { isKeywordDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn từ khóa")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = isKeywordDropdownExpanded,
                        onDismissRequest = { isKeywordDropdownExpanded = false }
                    ) {
                        keywordOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = { 
                                    inputKeyword = if (opt == "trống (không dùng)") "" else opt
                                    isKeywordDropdownExpanded = false 
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            results = viewModel.searchBoxartsOnline(gameTitle, inputPlatform, inputKeyword)
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bắt đầu tìm")
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (results != null && results!!.isEmpty()) {
                    Text("Không tìm thấy ảnh. Thử đổi tên game hoặc thêm API key trong Cài đặt.", style = MaterialTheme.typography.bodySmall)
                } else if (results != null) {
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        items(results!!.size) { i ->
                            val url = results!![i]
                            val ctx = LocalContext.current
                            val sourceName = try {
                                val host = java.net.URL(url).host
                                when {
                                    host.contains("steampowered") || host.contains("steamstatic") -> "Steam"
                                    host.contains("thegamesdb") -> "TheGamesDB"
                                    host.contains("wikipedia") || host.contains("wikimedia") -> "Wikipedia"
                                    host.contains("rawg.io") -> "RAWG"
                                    host.contains("igdb") -> "IGDB"
                                    else -> host.removePrefix("www.")
                                }
                            } catch (e: Exception) { "Online" }
                            
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.66f)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .clickable { onSelect(url) }
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(ctx).data(url).crossfade(true).build(),
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(sourceName, color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCard(modifier: Modifier = Modifier, game: GameEntity, globalScale: Float = 1.0f, mainScreenStyle: String = "style1", multiLineLabel: Boolean = false, gameTitleFontWeight: String = "normal", gameTitleFontSize: Float = 12f, isWallpaperDark: Boolean? = null, onClick: () -> Unit, onOptionsClick: () -> Unit) {
    val context = LocalContext.current

    val isTransparent = game.cardStyle == "transparent" || game.cardStyle == "clear"
    val isCrop = game.imageScale == "fit" // user's "Fit": phóng to, bo tròn -> Crop

    val cardColor = if (isTransparent) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
    val cardElevation = if (isTransparent) 0.dp else 8.dp

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_press_scale"
    )

    val imageModel = remember(game.boxartUrl) {
        if (game.boxartUrl?.startsWith("/") == true) java.io.File(game.boxartUrl) else game.boxartUrl
    }

    val combinedScale = game.sizeScale * globalScale

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = combinedScale * pressScale
                scaleY = combinedScale * pressScale
            }
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
                onClick = onClick,
                onLongClick = onOptionsClick
            )
            .padding(4.dp)
    ) {

        val baseMod = Modifier.fillMaxWidth()
        val ratioMod = if (game.cardStyle == "transparent") baseMod.wrapContentHeight() else baseMod.aspectRatio(if (game.cardStyle == "switch") 0.8f else 1f)

        Box(modifier = ratioMod) {
            when (game.cardStyle) {
                "switch" -> {
                    // Switch Style Virtual Game Card
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(3.dp).background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    if (game.boxartUrl != null) {
                                        AsyncImage(
                                            model = imageModel,
                                            contentDescription = "Boxart for ${game.title}",
                                            contentScale = if (isCrop) ContentScale.Crop else ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize().padding(1.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(9.dp, 9.dp, 0.dp, 0.dp))
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(game.platform, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                        }
                                    }
                                    if (game.showLabel && game.labelInside) {
                                        Text(game.title, color = Color.White, fontWeight = if (gameTitleFontWeight == "bold") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal, fontSize = gameTitleFontSize.sp, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, maxLines = if (multiLineLabel) Int.MAX_VALUE else 1)
                                    }
                                }
                                // Bottom strip with little triangle
                                Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
                "ds" -> {
                    // DS Style
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            // Top banner
                            Box(modifier = Modifier.fillMaxWidth().height(20.dp).background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(4.dp)), contentAlignment = Alignment.TopStart) {
                                Text("NINTENDO DS", color = Color.Black, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp, top = 2.dp), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.LightGray)) {
                                if (game.boxartUrl != null) {
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = "Boxart for ${game.title}",
                                        contentScale = if (isCrop) ContentScale.Crop else ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(game.platform, style = MaterialTheme.typography.titleMedium, color = Color.DarkGray)
                                    }
                                }
                                if (game.showLabel && game.labelInside) {
                                    Text(game.title, color = Color.White, fontWeight = if (gameTitleFontWeight == "bold") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal, fontSize = gameTitleFontSize.sp, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, maxLines = if (multiLineLabel) Int.MAX_VALUE else 1)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                "transparent" -> {
                    // Transparent Style (No Frame, No rounded corners)
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (game.boxartUrl != null) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Boxart for ${game.title}",
                                contentScale = if (isCrop) ContentScale.Crop else ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth().wrapContentHeight().then(if (isCrop) Modifier.clip(MaterialTheme.shapes.medium).aspectRatio(1f) else Modifier)
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.Transparent), contentAlignment = Alignment.Center) {
                                Text(game.platform, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        if (game.showLabel && game.labelInside) {
                            Text(game.title, color = Color.White, fontWeight = if (gameTitleFontWeight == "bold") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal, fontSize = gameTitleFontSize.sp, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, maxLines = if (multiLineLabel) Int.MAX_VALUE else 1)
                        }
                    }
                }
                "clear" -> {
                    // Clear Style (Transparent background, no outline)
                    // If isCrop is false (Original), we use RectangleShape so the image isn't clipped by the invisible rounded corners.
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = if (isCrop) MaterialTheme.shapes.medium else androidx.compose.ui.graphics.RectangleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (game.boxartUrl != null) {
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = "Boxart for ${game.title}",
                                    contentScale = if (isCrop) ContentScale.Crop else ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(game.platform, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (game.showLabel && game.labelInside) {
                                Text(game.title, color = Color.White, fontWeight = if (gameTitleFontWeight == "bold") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal, fontSize = gameTitleFontSize.sp, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, maxLines = if (multiLineLabel) Int.MAX_VALUE else 1)
                            }
                        }
                    }
                }
                else -> {
                    // Default Style
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = MaterialTheme.shapes.medium, // Luôn bo tròn khung
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (game.boxartUrl != null) {
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = "Boxart for ${game.title}",
                                    contentScale = if (isCrop) ContentScale.Crop else ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                    Text(game.platform, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                            if (game.showLabel && game.labelInside) {
                                Text(game.title, color = Color.White, fontWeight = if (gameTitleFontWeight == "bold") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal, fontSize = gameTitleFontSize.sp, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, maxLines = if (multiLineLabel) Int.MAX_VALUE else 1)
                            }
                        }
                    }
                }
            }
        }
        
        if (game.showLabel && !game.labelInside) {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val textColor = if (isWallpaperDark != null) {
                if (isWallpaperDark) Color.White else Color.Black
            } else if (mainScreenStyle == "immersive" || isDark) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onBackground
            }
            Text(
                text = game.title,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (gameTitleFontWeight == "bold") androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                fontSize = gameTitleFontSize.sp,
                maxLines = if (multiLineLabel) Int.MAX_VALUE else 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
