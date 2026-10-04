package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.bounceClick
import com.example.ui.theme.pressScale
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIComponentsSettingsFullScreen(
    viewModel: MainViewModel,
    context: android.content.Context,
    isTablet: Boolean,
    onBack: () -> Unit
) {
    val mainScreenStyle by viewModel.mainScreenStyle.collectAsState()
    val enableGlassEffectDefault by viewModel.enableGlassEffectDefault.collectAsState()
    val enableFadeoutDefault by viewModel.enableFadeoutDefault.collectAsState()
    val fadeoutIntensityDefault by viewModel.fadeoutIntensityDefault.collectAsState()
    val enableFadeoutDelta by viewModel.enableFadeoutDelta.collectAsState()
    val fadeoutIntensityDelta by viewModel.fadeoutIntensityDelta.collectAsState()
    val enablePatternBackground by viewModel.enablePatternBackground.collectAsState()
    val patternPreset by viewModel.patternPreset.collectAsState()
    val patternBackgroundStyle by viewModel.patternBackgroundStyle.collectAsState()
    val patternDensity by viewModel.patternDensity.collectAsState()
    val patternAnimated by viewModel.patternAnimated.collectAsState()
    val hazePreset by viewModel.hazePreset.collectAsState()
    val popupHazePreset by viewModel.popupHazePreset.collectAsState()
    val chipHazePreset by viewModel.chipHazePreset.collectAsState()
    val hazeBlurRadius by viewModel.hazeBlurRadius.collectAsState()
    val hazeNoiseFactor by viewModel.hazeNoiseFactor.collectAsState()
    val hazeTintAlpha by viewModel.hazeTintAlpha.collectAsState()
    val hazeBrightness by viewModel.hazeBrightness.collectAsState()
    val appColor by viewModel.appColor.collectAsState()
    val allGamesWallpaperUri by viewModel.allGamesWallpaperUri.collectAsState()
    val allGamesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.updateAllGamesWallpaperUri(context, uri.toString())
        }
    }
    var showColorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thành phần giao diện") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            item {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    headlineContent = { Text("Hình nền Tất cả game") },
                    supportingContent = { Text(if (allGamesWallpaperUri != null) "Đã chọn" else "Chưa chọn") },
                    trailingContent = {
                        if (allGamesWallpaperUri != null) {
                            IconButton(onClick = { viewModel.updateAllGamesWallpaperUri(context, null) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa")
                            }
                        }
                    },
                    modifier = Modifier.clickable { allGamesLauncher.launch(arrayOf("image/*")) }
                )
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    headlineContent = { Text("Họa tiết nền", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                    supportingContent = { Text("Hiển thị họa tiết nghệ thuật chìm theo màu accent") },
                    trailingContent = { Switch(checked = enablePatternBackground, onCheckedChange = { viewModel.togglePatternBackground(context) }) },
                    modifier = Modifier.clickable { viewModel.togglePatternBackground(context) }
                )

                if (enablePatternBackground) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "XEM TRƯỚC HỌA TIẾT",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            ) {
                                com.example.ui.navigation.EmojiPatternBackground(
                                    color = MaterialTheme.colorScheme.primary,
                                    preset = patternPreset,
                                    style = patternBackgroundStyle,
                                    density = patternDensity,
                                    animated = patternAnimated
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(12.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "$patternPreset ($patternBackgroundStyle)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        headlineContent = { Text("Chuyển động họa tiết") },
                        supportingContent = { Text(if (patternAnimated) "Họa tiết trôi chuyển động nhẹ nhàng" else "Họa tiết đứng im cố định") },
                        trailingContent = {
                            Switch(
                                checked = patternAnimated,
                                onCheckedChange = { viewModel.togglePatternAnimated(context) }
                            )
                        },
                        modifier = Modifier.clickable { viewModel.togglePatternAnimated(context) }.padding(start = 16.dp)
                    )
                    var showPatternPresetDialog by remember { mutableStateOf(false) }
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        headlineContent = { Text("Loại họa tiết") },
                        supportingContent = { Text(patternPreset) },
                        modifier = Modifier.bounceClick { showPatternPresetDialog = true }.padding(start = 16.dp)
                    )
                    if (showPatternPresetDialog) {
                        AlertDialog(
                            onDismissRequest = { showPatternPresetDialog = false },
                            title = { Text("Chọn loại họa tiết") },
                            text = {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    val presets = listOf(
                                        "Gamer Retro",
                                        "Vũ trụ Stars",
                                        "Anime Pop",
                                        "Hình học Grid",
                                        "Hoa",
                                        "Mèo",
                                        "Cún",
                                        "Hoa quả",
                                        "Hỗn hợp"
                                    )
                                    presets.forEach { preset ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().bounceClick {
                                                viewModel.updatePatternPreset(context, preset)
                                                showPatternPresetDialog = false
                                            }.padding(vertical = 12.dp)
                                        ) {
                                            RadioButton(selected = patternPreset == preset, onClick = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text(preset)
                                        }
                                    }
                                }
                            },
                            confirmButton = { TextButton(onClick = { showPatternPresetDialog = false }) { Text("Đóng") } }
                        )
                    }

                    var showPatternStyleDialog by remember { mutableStateOf(false) }
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        headlineContent = { Text("Độ đậm họa tiết") },
                        supportingContent = { Text(patternBackgroundStyle) },
                        modifier = Modifier.clickable { showPatternStyleDialog = true }.padding(start = 16.dp)
                    )
                    if (showPatternStyleDialog) {
                        AlertDialog(
                            onDismissRequest = { showPatternStyleDialog = false },
                            title = { Text("Chọn độ đậm") },
                            text = {
                                Column {
                                    val styles = listOf("Rất nhạt", "Nhạt", "Vừa", "Đậm")
                                    styles.forEach { style ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                viewModel.updatePatternBackgroundStyle(context, style)
                                                showPatternStyleDialog = false
                                            }.padding(vertical = 12.dp)
                                        ) {
                                            RadioButton(selected = patternBackgroundStyle == style, onClick = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text(style)
                                        }
                                    }
                                }
                            },
                            confirmButton = { TextButton(onClick = { showPatternStyleDialog = false }) { Text("Đóng") } }
                        )
                    }

                    Column(modifier = Modifier.padding(start = 32.dp, end = 24.dp, top = 4.dp, bottom = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mật độ họa tiết", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                when (patternDensity) {
                                    1 -> "Thưa"
                                    2 -> "Vừa"
                                    3 -> "Dày"
                                    else -> "Rất dày"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                        Slider(
                            value = patternDensity.toFloat(),
                            onValueChange = { viewModel.updatePatternDensity(context, it.toInt()) },
                            valueRange = 1f..4f,
                            steps = 2
                        )
                    }
                }

                if (mainScreenStyle == "style1") {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        headlineContent = { Text("Hiệu ứng kính (Mặc định)") },
                        supportingContent = { Text("Làm mờ ảnh nền phía sau nội dung") },
                        trailingContent = { Switch(checked = enableGlassEffectDefault, onCheckedChange = { viewModel.toggleGlassEffectDefault(context) }) },
                        modifier = Modifier.clickable { viewModel.toggleGlassEffectDefault(context) }
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        headlineContent = { Text("Làm mờ phần đầu trang (Mặc định)") },
                        supportingContent = { Text("Fadeout hiệu ứng kéo lên mượt mà") },
                        trailingContent = { Switch(checked = enableFadeoutDefault, onCheckedChange = { viewModel.toggleFadeoutDefault(context) }) },
                        modifier = Modifier.clickable { viewModel.toggleFadeoutDefault(context) }
                    )

                    if (enableFadeoutDefault) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Độ mờ đầu trang", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(fadeoutIntensityDefault * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                            Slider(
                                value = fadeoutIntensityDefault,
                                onValueChange = { viewModel.updateFadeoutIntensityDefault(context, it) },
                                valueRange = 0.1f..1.0f,
                                steps = 9
                            )
                        }
                    }

                    if (enableGlassEffectDefault) {
                        var expandGlassSettings by remember { mutableStateOf(false) }
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                            headlineContent = { Text("Chỉnh kính") },
                            supportingContent = { Text("Tùy chỉnh độ mờ, độ nhiễu, độ sáng...") },
                            trailingContent = {
                                Icon(
                                    imageVector = if (expandGlassSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable { expandGlassSettings = !expandGlassSettings }
                        )
                        androidx.compose.animation.AnimatedVisibility(visible = expandGlassSettings) {
                            Column(modifier = Modifier.padding(start = 32.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)) {
                                var showHazePresetDialog by remember { mutableStateOf(false) }
                                val hazePresets = listOf("Ultra Thin", "Thin", "Regular", "Thick", "Ultra Thick")

                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                    headlineContent = { Text("Preset Kính NavBar") },
                                    supportingContent = { Text(hazePreset) },
                                    modifier = Modifier.bounceClick { showHazePresetDialog = true }.padding(start = 0.dp)
                                )
                                if (showHazePresetDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showHazePresetDialog = false },
                                        title = { Text("Chọn Preset") },
                                        text = {
                                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                                hazePresets.forEach { preset ->
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { 
                                                        viewModel.updateHazePreset(context, preset)
                                                        showHazePresetDialog = false
                                                    }) {
                                                        RadioButton(selected = hazePreset == preset, onClick = { 
                                                            viewModel.updateHazePreset(context, preset)
                                                            showHazePresetDialog = false
                                                         })
                                                        Text(preset)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = { TextButton(onClick = { showHazePresetDialog = false }) { Text("Đóng") } }
                                    )
                                }

                                var showPopupHazePresetDialog by remember { mutableStateOf(false) }
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                    headlineContent = { Text("Preset Kính Popup") },
                                    supportingContent = { Text(popupHazePreset) },
                                    modifier = Modifier.bounceClick { showPopupHazePresetDialog = true }.padding(start = 0.dp)
                                )
                                if (showPopupHazePresetDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showPopupHazePresetDialog = false },
                                        title = { Text("Chọn Preset Popup") },
                                        text = {
                                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                                val popupPresets = listOf("Ultra Thin", "Thin", "Regular", "Thick", "Ultra Thick")
                                                popupPresets.forEach { preset ->
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { 
                                                        viewModel.updatePopupHazePreset(context, preset)
                                                        showPopupHazePresetDialog = false
                                                    }) {
                                                        RadioButton(selected = popupHazePreset == preset, onClick = { 
                                                            viewModel.updatePopupHazePreset(context, preset)
                                                            showPopupHazePresetDialog = false
                                                         })
                                                        Text(preset)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = { TextButton(onClick = { showPopupHazePresetDialog = false }) { Text("Đóng") } }
                                    )
                                }

                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    val popupHazeBrightness by viewModel.popupHazeBrightness.collectAsState()
                                    Text("Độ sáng Popup: ${(popupHazeBrightness * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                                    Slider(
                                        value = popupHazeBrightness,
                                        onValueChange = { viewModel.updatePopupHazeBrightness(context, it) },
                                        valueRange = -1f..1f
                                    )
                                }

                                var showChipHazePresetDialog by remember { mutableStateOf(false) }
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                    headlineContent = { Text("Preset Kính Chips") },
                                    supportingContent = { Text(chipHazePreset) },
                                    modifier = Modifier.bounceClick { showChipHazePresetDialog = true }.padding(start = 0.dp)
                                )
                                if (showChipHazePresetDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showChipHazePresetDialog = false },
                                        title = { Text("Chọn Preset Chips") },
                                        text = {
                                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                                val chipPresets = listOf("Ultra Thin", "Thin", "Regular", "Thick", "Ultra Thick")
                                                chipPresets.forEach { preset ->
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { 
                                                        viewModel.updateChipHazePreset(context, preset)
                                                        showChipHazePresetDialog = false
                                                    }) {
                                                        RadioButton(selected = chipHazePreset == preset, onClick = { 
                                                            viewModel.updateChipHazePreset(context, preset)
                                                            showChipHazePresetDialog = false
                                                         })
                                                        Text(preset)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = { TextButton(onClick = { showChipHazePresetDialog = false }) { Text("Đóng") } }
                                    )
                                }
                            }
                        }
                    }
                }

                if (mainScreenStyle == "style2") {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        headlineContent = { Text("Phủ mờ lên ảnh nền (Delta)") },
                        supportingContent = { Text("Làm mờ nửa trên background") },
                        trailingContent = { Switch(checked = enableFadeoutDelta, onCheckedChange = { viewModel.toggleFadeoutDelta(context) }) },
                        modifier = Modifier.clickable { viewModel.toggleFadeoutDelta(context) }
                    )

                    if (enableFadeoutDelta) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Độ mờ Delta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(fadeoutIntensityDelta * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                            Slider(
                                value = fadeoutIntensityDelta,
                                onValueChange = { viewModel.updateFadeoutIntensityDelta(context, it) },
                                valueRange = 0.1f..1.0f,
                                steps = 9
                            )
                        }
                    }
                }

                Box {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        headlineContent = { Text("Màu chủ đạo") },
                        supportingContent = { Text(
                            when(appColor) {
                                "dynamic" -> "Dynamic (Material You)"
                                "blue" -> "Xanh dương"
                                "green" -> "Xanh lá"
                                "red" -> "Đỏ"
                                "yellow" -> "Vàng"
                                "purple" -> "Tím"
                                else -> "Mặc định (Tím nhạt)"
                            }
                        ) },
                        modifier = Modifier.clickable { showColorDialog = true }
                    )
                    if (isTablet) {
                        DropdownMenu(
                            expanded = showColorDialog,
                            onDismissRequest = { showColorDialog = false }
                        ) {
                            val colors = listOf(
                                "dynamic" to "Dynamic (Theo hình nền Material You)",
                                "default" to "Mặc định (Tím nhạt)",
                                "blue" to "Xanh dương",
                                "green" to "Xanh lá",
                                "red" to "Đỏ",
                                "yellow" to "Vàng",
                                "purple" to "Tím"
                            )
                            colors.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.saveAppColor(context, key)
                                        showColorDialog = false
                                    }
                                )
                            }
                        }
                    } else if (showColorDialog) {
                        val colors = listOf(
                            "dynamic" to "Dynamic (Theo hình nền Material You)",
                            "default" to "Mặc định (Tím nhạt)",
                            "blue" to "Xanh dương",
                            "green" to "Xanh lá",
                            "red" to "Đỏ",
                            "yellow" to "Vàng",
                            "purple" to "Tím"
                        )
                        AlertDialog(
                            onDismissRequest = { showColorDialog = false },
                            title = { Text("Màu chủ đạo") },
                            text = {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    colors.forEach { (key, label) ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { 
                                            viewModel.saveAppColor(context, key)
                                            showColorDialog = false
                                        }) {
                                            RadioButton(selected = appColor == key, onClick = { 
                                                viewModel.saveAppColor(context, key)
                                                showColorDialog = false
                                             })
                                            Text(label)
                                        }
                                    }
                                }
                            },
                            confirmButton = { TextButton(onClick = { showColorDialog = false }) { Text("Đóng") } }
                        )
                    }
                }
            }
        }
    }
}
