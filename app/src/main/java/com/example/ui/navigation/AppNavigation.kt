package com.example.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.bounceClick
import com.example.ui.viewmodels.MainViewModel

import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Menu
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import androidx.compose.animation.togetherWith

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.translate

import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.FilterVintage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun AppNavigation(mainViewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isRailExpanded by remember { mutableStateOf(false) }
    val mainScreenStyle by mainViewModel.mainScreenStyle.collectAsState()
    val enableGlassEffectDefault by mainViewModel.enableGlassEffectDefault.collectAsState()
    val appColor by mainViewModel.appColor.collectAsState()
    val enablePatternBackground by mainViewModel.enablePatternBackground.collectAsState()
    val patternPreset by mainViewModel.patternPreset.collectAsState()
    val patternBackgroundStyle by mainViewModel.patternBackgroundStyle.collectAsState()
    val patternDensity by mainViewModel.patternDensity.collectAsState()
    val patternAnimated by mainViewModel.patternAnimated.collectAsState()
    
    val hazePreset by mainViewModel.hazePreset.collectAsState()
    val hazeBlurRadius by mainViewModel.hazeBlurRadius.collectAsState()
    val hazeNoiseFactor by mainViewModel.hazeNoiseFactor.collectAsState()
    val hazeTintAlpha by mainViewModel.hazeTintAlpha.collectAsState()
    val hazeBrightness by mainViewModel.hazeBrightness.collectAsState()
    val chipHazePreset by mainViewModel.chipHazePreset.collectAsState()

    val showDock = mainScreenStyle == "style1"
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600 && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val useGlass = showDock && enableGlassEffectDefault

    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
    val hazeState = remember { HazeState() }

    androidx.activity.compose.BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        if (enablePatternBackground) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isTablet) 0.dp else if (showDock) 64.dp else 0.dp)
            ) {
                EmojiPatternBackground(
                    color = MaterialTheme.colorScheme.primary,
                    preset = patternPreset,
                    style = patternBackgroundStyle,
                    density = patternDensity,
                    animated = patternAnimated
                )
            }
        }
        
        if (isTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showDock,
                    enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it }) + androidx.compose.animation.fadeOut()
                ) {
                Surface(
                    modifier = Modifier.fillMaxHeight().width(if (isRailExpanded) 160.dp else 80.dp).animateContentSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = if (isRailExpanded) Alignment.Start else Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .height(64.dp)
                                .fillMaxWidth()
                                .padding(start = if (isRailExpanded) 16.dp else 0.dp),
                            contentAlignment = if (isRailExpanded) Alignment.CenterStart else Alignment.Center
                        ) {
                            IconButton(onClick = { isRailExpanded = !isRailExpanded }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (mainScreenStyle != "style2") {
                            FloatingActionButton(
                                onClick = { mainViewModel.triggerAddRom() },
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                                modifier = Modifier.padding(horizontal = if (isRailExpanded) 16.dp else 0.dp),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = if (isRailExpanded) 16.dp else 0.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Thêm")
                                    if (isRailExpanded) {
                                        Spacer(Modifier.width(8.dp))
                                        Text("Mới", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val tabs = listOf(
                            Triple(0, "Tất cả", Icons.Default.Gamepad),
                            Triple(1, "Tìm kiếm", Icons.Default.Search),
                            Triple(2, "Cài đặt", Icons.Default.Settings)
                        )

                        tabs.forEach { (index, title, icon) ->
                            val selected = selectedTab == index
                            Surface(
                                selected = selected,
                                onClick = { selectedTab = index },
                                shape = CircleShape,
                                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .padding(horizontal = if (isRailExpanded) 12.dp else 12.dp, vertical = 4.dp)
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = if (isRailExpanded) Arrangement.Start else Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = if (isRailExpanded) 16.dp else 0.dp)
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = title,
                                        tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isRailExpanded) {
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            title,
                                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                androidx.compose.animation.AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200)).togetherWith(
                            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                        )
                    },
                    label = "tablet_tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> MainScreen(viewModel = mainViewModel, hazeState = null, onNavigate = { selectedTab = it })
                        1 -> SearchScreen(viewModel = mainViewModel, onBack = { selectedTab = 0 })
                        2 -> SettingsScreen(viewModel = mainViewModel, onBack = { selectedTab = 0 })
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val duration = 280
                        if (targetState > initialState) {
                            (androidx.compose.animation.slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(duration), initialOffsetX = { (it * 0.25f).toInt() }) +
                                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(duration)) +
                                    androidx.compose.animation.scaleIn(animationSpec = androidx.compose.animation.core.tween(duration), initialScale = 0.96f)) togetherWith
                                    (androidx.compose.animation.slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(duration), targetOffsetX = { (-it * 0.25f).toInt() }) +
                                            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(duration * 3 / 4)))
                        } else {
                            (androidx.compose.animation.slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(duration), initialOffsetX = { (-it * 0.25f).toInt() }) +
                                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(duration)) +
                                    androidx.compose.animation.scaleIn(animationSpec = androidx.compose.animation.core.tween(duration), initialScale = 0.96f)) togetherWith
                                    (androidx.compose.animation.slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(duration), targetOffsetX = { (it * 0.25f).toInt() }) +
                                            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(duration * 3 / 4)))
                        }
                    },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> MainScreen(viewModel = mainViewModel, hazeState = hazeState, onNavigate = { selectedTab = it })
                        1 -> Box(modifier = Modifier.fillMaxSize().then(if (useGlass) Modifier.haze(hazeState) else Modifier)) { SearchScreen(viewModel = mainViewModel, onBack = { selectedTab = 0 }) }
                        2 -> Box(modifier = Modifier.fillMaxSize().then(if (useGlass) Modifier.haze(hazeState) else Modifier)) { SettingsScreen(viewModel = mainViewModel, onBack = { selectedTab = 0 }) }
                    }
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = showDock,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut()
            ) {
                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                val navBarHeight = 56.dp
                val iosNavbarHazeStyle = dev.chrisbanes.haze.materials.HazeMaterials.regular()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (useGlass && hazeState != null) {
                                Modifier.hazeChild(state = hazeState, style = iosNavbarHazeStyle)
                            } else Modifier
                        )
                        .background(
                            if (useGlass && hazeState != null) Color.Transparent else MaterialTheme.colorScheme.surface
                        )
                ) {
                    if (useGlass && hazeState != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f))
                                .align(Alignment.TopCenter)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                .align(Alignment.TopCenter)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(navBarHeight)
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            Triple(0, "Tất cả", Icons.Default.Gamepad),
                            Triple(1, "Tìm kiếm", Icons.Default.Search),
                            Triple(2, "Cài đặt", Icons.Default.Settings)
                        )
                        
                        tabs.forEach { (index, title, icon) ->
                            val selected = selectedTab == index
                            val activeColor = MaterialTheme.colorScheme.primary
                            val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
                            val pillBg = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .bounceClick { selectedTab = index },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(pillBg)
                                        .padding(horizontal = 18.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = title,
                                        tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else inactiveColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    title, 
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal), 
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else inactiveColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}


