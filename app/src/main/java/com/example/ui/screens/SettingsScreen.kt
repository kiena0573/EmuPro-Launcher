package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import com.example.ui.theme.bounceClick
import com.example.ui.theme.pressScale
import com.example.ui.viewmodels.MainViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.selection.selectableGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val rawgApiKey by viewModel.rawgApiKey.collectAsState()
    val gamesState by viewModel.games.collectAsState()
    val games = gamesState ?: emptyList()
    val customPlatforms by viewModel.customPlatforms.collectAsState()
    val platforms = games.map { it.platform }.distinct().sorted()
    
    // Add ROM Platform Selection Dialog states
    var pendingUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showPlatformDialog by remember { mutableStateOf(false) }
    
    var showAddTypeDialog by remember { mutableStateOf(false) }
    var showNativeAppDialog by remember { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf<List<android.content.pm.ResolveInfo>?>(null) }

    LaunchedEffect(showNativeAppDialog) {
        if (showNativeAppDialog && installedApps == null) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val apps = pm.queryIntentActivities(intent, 0)
            installedApps = apps.sortedBy { it.loadLabel(pm).toString() }
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch(e: Exception) {}
            }
            pendingUris = uris
            showPlatformDialog = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.addRomEvent.collect {
            showAddTypeDialog = true
        }
    }

    
    var cpToDelete by remember { mutableStateOf<String?>(null) }
    if (cpToDelete != null) {
        AlertDialog(
            onDismissRequest = { cpToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có muốn xóa hệ máy '$cpToDelete' không?") },
            confirmButton = { TextButton(onClick = { viewModel.removeCustomPlatform(context, cpToDelete!!); cpToDelete = null }) { Text("Xóa") } },
            dismissButton = { TextButton(onClick = { cpToDelete = null }) { Text("Hủy") } }
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let { viewModel.exportSettings(context, it) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importSettings(context, it) }
    }

    val defaultCardStyle by viewModel.defaultCardStyle.collectAsState()
    val defaultImageScale by viewModel.defaultImageScale.collectAsState()
    val defaultShowLabel by viewModel.defaultShowLabel.collectAsState()
    val defaultLabelInside by viewModel.defaultLabelInside.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val globalGameCardScale by viewModel.globalGameCardScale.collectAsState()
    val multiLineLabel by viewModel.multiLineLabel.collectAsState()
    val gameTitleFontWeight by viewModel.gameTitleFontWeight.collectAsState()
    val gameTitleFontSize by viewModel.gameTitleFontSize.collectAsState()
    
    val appColor by viewModel.appColor.collectAsState()

    var showApiDialog by remember { mutableStateOf(false) }
    var showEmulatorDialog by remember { mutableStateOf(false) }
    var showGameCardSettingsDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    var showDataSettingsDialog by remember { mutableStateOf(false) }
    var showFolderSettingsDialog by remember { mutableStateOf(false) }
    var showUIComponentsSettingsDialog by remember { mutableStateOf(false) }

    if (showUIComponentsSettingsDialog) {
        androidx.activity.compose.BackHandler { showUIComponentsSettingsDialog = false }
        UIComponentsSettingsFullScreen(
            viewModel = viewModel,
            context = context,
            isTablet = isTablet,
            onBack = { showUIComponentsSettingsDialog = false }
        )
        return
    }

    if (showFolderSettingsDialog) {
        androidx.activity.compose.BackHandler { showFolderSettingsDialog = false }
        FolderSettingsFullScreen(
            viewModel = viewModel,
            context = context,
            onBack = { showFolderSettingsDialog = false }
        )
        return
    }

    if (showApiDialog) {
        androidx.activity.compose.BackHandler { showApiDialog = false }
        ScraperSettingsFullScreen(
            viewModel = viewModel,
            context = context,
            rawgApiKey = rawgApiKey,
            onBack = { showApiDialog = false }
        )
        return
    }

    if (showEmulatorDialog) {
        androidx.activity.compose.BackHandler { showEmulatorDialog = false }
        EmulatorSettingsFullScreen(
            viewModel = viewModel,
            context = context,
            customPlatforms = customPlatforms,
            onBack = { showEmulatorDialog = false }
        )
        return
    }

    if (showGameCardSettingsDialog) {
        androidx.activity.compose.BackHandler { showGameCardSettingsDialog = false }
        GameCardSettingsFullScreen(
            viewModel = viewModel,
            context = context,
            defaultCardStyle = defaultCardStyle,
            defaultImageScale = defaultImageScale,
            defaultShowLabel = defaultShowLabel,
            defaultLabelInside = defaultLabelInside,
            platforms = platforms,
            gridColumns = gridColumns,
            globalGameCardScale = globalGameCardScale,
            multiLineLabel = multiLineLabel,
            gameTitleFontWeight = gameTitleFontWeight,
            gameTitleFontSize = gameTitleFontSize,
            onBack = { showGameCardSettingsDialog = false }
        )
        return
    }

    if (showDataSettingsDialog) {
        androidx.activity.compose.BackHandler { showDataSettingsDialog = false }
        DataSettingsFullScreen(
            onExportClick = { exportLauncher.launch("emulator_data.zip") },
            onImportClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
            onBack = { showDataSettingsDialog = false }
        )
        return
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        val bottomPadding = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = bottomPadding)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "HỆ THỐNG", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                leadingContent = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                headlineContent = { Text("Thêm ROM / Ứng dụng", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                                supportingContent = { Text("Chọn ROM hoặc Ứng dụng cần thêm") },
                                modifier = Modifier.bounceClick { viewModel.triggerAddRom() }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                                headlineContent = { Text("Cài đặt Thư mục (Fold)", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                                supportingContent = { Text("Bật/tắt thư mục giả lập Samsung và cấu hình icon") },
                                modifier = Modifier.bounceClick { showFolderSettingsDialog = true }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Box {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                    leadingContent = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                                    headlineContent = { Text("Cài đặt Scraper", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                                    supportingContent = { Text(if (rawgApiKey.isBlank()) "Chưa thiết lập API Key" else "Đã thiết lập API Key") },
                                    modifier = Modifier.bounceClick { showApiDialog = true }
                                )
                                if (isTablet) {
                                    DropdownMenu(
                                        expanded = showApiDialog,
                                        onDismissRequest = { showApiDialog = false },
                                        modifier = Modifier.width(320.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                var input by remember { mutableStateOf(rawgApiKey) }
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text("Cài đặt Scraper", style = MaterialTheme.typography.titleMedium)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        "Hỗ trợ tìm kiếm tự động thông qua IGDB, TheGamesDB, Steam, DuckDuckGo.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    OutlinedTextField(
                                                        value = input,
                                                        onValueChange = { input = it },
                                                        label = { Text("API Key của IGDB/RAWG (Tùy chọn)") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    
                                                    var inputPlatform by remember { mutableStateOf("{platform}") }
                                                    var keywordOpts = listOf("boxart", "Disc boxart", "gamecard", "cover", "trống (không dùng)")
                                                    var inputKeyword by remember { mutableStateOf("boxart") }
                                                    var expMenu by remember { mutableStateOf(false) }
                                                    
                                                    Text("Tùy chọn tải hàng loạt:", style = MaterialTheme.typography.labelLarge)
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
                                                                IconButton(onClick = { expMenu = true }) {
                                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn từ khóa")
                                                                }
                                                            }
                                                        )
                                                        DropdownMenu(expanded = expMenu, onDismissRequest = { expMenu = false }) {
                                                            keywordOpts.forEach { opt ->
                                                                DropdownMenuItem(text = { Text(opt) }, onClick = { inputKeyword = if(opt == "trống (không dùng)") "" else opt; expMenu = false })
                                                            }
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Button(
                                                        onClick = { viewModel.batchDownloadBoxarts(context, inputPlatform, inputKeyword) },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) { Text("Tải lại Boxart hàng loạt") }
                                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                                        TextButton(onClick = { showApiDialog = false }) { Text("Hủy") }
                                                        TextButton(onClick = { 
                                                            viewModel.saveRawgApiKey(context, input)
                                                            showApiDialog = false
                                                        }) { Text("Lưu") }
                                                    }
                                                }
                                            },
                                            onClick = {}
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                headlineContent = { Text("Tùy chỉnh hệ máy", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                                supportingContent = { Text("${customPlatforms.size} hệ máy đã kết nối") },
                                modifier = Modifier.bounceClick { showEmulatorDialog = true }
                            )
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "GIAO DIỆN", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    
                    val mainScreenStyle by viewModel.mainScreenStyle.collectAsState()
                    var showMainStyleDialog by remember { mutableStateOf(false) }

                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Box {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                    leadingContent = { Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    headlineContent = { Text("Bố cục giao diện", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                                    supportingContent = { Text(
                                        when(mainScreenStyle) {
                                            "style2" -> "Delta"
                                            else -> "Mặc định"
                                        }
                                    ) },
                                    modifier = Modifier.bounceClick { showMainStyleDialog = true }
                                )
                                
                                if (isTablet) {
                                    DropdownMenu(
                                        expanded = showMainStyleDialog,
                                        onDismissRequest = { showMainStyleDialog = false }
                                    ) {
                                        val styles = listOf("style1" to "Mặc định", "style2" to "Delta")
                                        styles.forEach { (key, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    viewModel.saveMainScreenStyle(context, key)
                                                    showMainStyleDialog = false
                                                }
                                            )
                                        }
                                    }
                                } else if (showMainStyleDialog) {
                                    val styles = listOf("style1" to "Mặc định", "style2" to "Delta")
                                    AlertDialog(
                                        onDismissRequest = { showMainStyleDialog = false },
                                        title = { Text("Bố cục giao diện") },
                                        text = {
                                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                                styles.forEach { (key, label) ->
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { 
                                                        viewModel.saveMainScreenStyle(context, key)
                                                        showMainStyleDialog = false
                                                    }.padding(vertical = 12.dp)) {
                                                        RadioButton(selected = mainScreenStyle == key, onClick = null)
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(label)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = { TextButton(onClick = { showMainStyleDialog = false }) { Text("Đóng") } }
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                leadingContent = { Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                                headlineContent = { Text("Thành phần giao diện", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                                supportingContent = { Text("Họa tiết nền, hiệu ứng kính, màu chủ đạo...") },
                                modifier = Modifier.bounceClick { showUIComponentsSettingsDialog = true }
                            )
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "GAMECARD", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                            leadingContent = { Icon(Icons.Default.Gamepad, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                            headlineContent = { Text("Định dạng thẻ Game", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                            supportingContent = { Text("Kích cỡ, font chữ, ẩn hiện nhãn...") },
                            modifier = Modifier.bounceClick { showGameCardSettingsDialog = true }
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "DỮ LIỆU & BỘ NHỚ", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            headlineContent = { Text("Quản lý Dữ liệu", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                            supportingContent = { Text("Nhập/Xuất cấu hình, ROMs và bìa game") },
                            modifier = Modifier.bounceClick { showDataSettingsDialog = true }
                        )
                    }
                }
            }
        }
    }






    
    if (showAddTypeDialog) {
        AlertDialog(
            onDismissRequest = { showAddTypeDialog = false },
            title = { Text("Thêm Game") },
            text = {
                Column {
                    TextButton(onClick = { 
                        showAddTypeDialog = false
                        filePicker.launch(arrayOf("application/octet-stream", "*/*"))
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("ROM", color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = { 
                        showAddTypeDialog = false
                        showNativeAppDialog = true
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Ứng dụng / Game Native", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddTypeDialog = false }) { Text("Đóng") } }
        )
    }

    if (showNativeAppDialog) {
        AlertDialog(
            onDismissRequest = { showNativeAppDialog = false },
            title = { Text("Chọn Ứng Dụng") },
            text = {
                if (installedApps == null) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn {
                        if (installedApps != null) {
                            items(installedApps!!.size) { index ->
                                val appInfo = installedApps!![index]
                                val pm = context.packageManager
                                val appName = appInfo.loadLabel(pm).toString()
                                val pkg = appInfo.activityInfo.packageName
                                val icon = remember(pkg) { appInfo.loadIcon(pm) }
                                ListItem(
                                    leadingContent = {
                                        coil.compose.AsyncImage(
                                            model = icon,
                                            contentDescription = appName,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    },
                                    headlineContent = { Text(appName) },
                                    supportingContent = { Text(pkg) },
                                    modifier = Modifier.clickable {
                                        viewModel.addNativeApp(context, appName, pkg)
                                        showNativeAppDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showNativeAppDialog = false }) { Text("Đóng") } }
        )
    }

    if (showPlatformDialog && pendingUris.isNotEmpty()) {
        val platformOptions = com.example.utils.EmulatorIntentFactory.getAvailablePlatforms()
        var selectedPlatform by remember(platformOptions) { mutableStateOf(platformOptions.firstOrNull() ?: "PS2") }
        var showNewPlatformDialog by remember { mutableStateOf(false) }

        if (showNewPlatformDialog) {
            var newName by remember { mutableStateOf("") }
            var newPkg by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showNewPlatformDialog = false },
                title = { Text("Thêm Hệ Máy Mới") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Tên hệ máy (VD: N64)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = newPkg, onValueChange = { newPkg = it }, label = { Text("Package (VD: org.ps2.abc)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newName.isNotBlank() && newPkg.isNotBlank()) {
                            viewModel.addCustomPlatform(context, com.example.data.PlatformConfig(newName, newPkg, false))
                            showNewPlatformDialog = false
                            selectedPlatform = newName
                        }
                    }) { Text("Lưu") }
                },
                dismissButton = {
                    TextButton(onClick = { showNewPlatformDialog = false }) { Text("Hủy") }
                }
            )
        }
        
        AlertDialog(
            onDismissRequest = { 
                showPlatformDialog = false 
                pendingUris = emptyList()
            },
            title = { Text("Chọn Hệ Máy") },
            text = {
                Column(Modifier.selectableGroup()) {
                    platformOptions.forEach { platform ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlatform = platform }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedPlatform == platform,
                                onClick = { selectedPlatform = platform }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(platform)
                        }
                    }
                    TextButton(
                        onClick = { showNewPlatformDialog = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("+ Thêm hệ máy mới")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPlatformDialog = false
                    viewModel.addROMs(context, pendingUris, selectedPlatform)
                    pendingUris = emptyList()
                }) {
                    Text("Thêm / Khôi Phục")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPlatformDialog = false 
                    pendingUris = emptyList()
                }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScraperSettingsFullScreen(
    viewModel: MainViewModel,
    context: android.content.Context,
    rawgApiKey: String,
    onBack: () -> Unit
) {
    var input by remember { mutableStateOf(rawgApiKey) }
    var usePlatform by remember { mutableStateOf(true) }
    var keywordOpts = listOf("boxart", "Disc boxart", "gamecard", "cover", "trống (không dùng)")
    var inputKeyword by remember { mutableStateOf("boxart") }
    var inputPlatform by remember { mutableStateOf("{platform}") }
    var expMenu by remember { mutableStateOf(false) }

    val scraperSteamEnabled by viewModel.scraperSteamEnabled.collectAsState()
    val scraperRawgEnabled by viewModel.scraperRawgEnabled.collectAsState()
    val scraperWikipediaEnabled by viewModel.scraperWikipediaEnabled.collectAsState()
    val scraperGoogleEnabled by viewModel.scraperGoogleEnabled.collectAsState()
    val scraperTheGamesDbEnabled by viewModel.scraperTheGamesDbEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt Scraper") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val bottomPadding = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomPadding)) {
            Text(
                "Hỗ trợ tìm kiếm tự động thông qua IGDB, TheGamesDB, Steam, Google Images, Wikipedia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it; viewModel.saveRawgApiKey(context, it) },
                label = { Text("API Key của IGDB/RAWG (Tùy chọn)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Nguồn Boxart (Bật/Tắt):", style = MaterialTheme.typography.labelLarge)
            fun updateToggles(steam: Boolean = scraperSteamEnabled, rawg: Boolean = scraperRawgEnabled, wiki: Boolean = scraperWikipediaEnabled, google: Boolean = scraperGoogleEnabled, db: Boolean = scraperTheGamesDbEnabled) {
                viewModel.saveScraperToggles(context, steam, rawg, wiki, google, db)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Web Image Search (Google/Bing)", modifier = Modifier.weight(1f))
                Switch(checked = scraperGoogleEnabled, onCheckedChange = { updateToggles(google = it) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Steam (PC Games)", modifier = Modifier.weight(1f))
                Switch(checked = scraperSteamEnabled, onCheckedChange = { updateToggles(steam = it) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("IGDB/RAWG", modifier = Modifier.weight(1f))
                Switch(checked = scraperRawgEnabled, onCheckedChange = { updateToggles(rawg = it) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("TheGamesDB", modifier = Modifier.weight(1f))
                Switch(checked = scraperTheGamesDbEnabled, onCheckedChange = { updateToggles(db = it) })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Wikipedia", modifier = Modifier.weight(1f))
                Switch(checked = scraperWikipediaEnabled, onCheckedChange = { updateToggles(wiki = it) })
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Tùy chọn tải hàng loạt:", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = inputPlatform,
                onValueChange = { inputPlatform = it },
                label = { Text("Hỗ trợ hệ máy (hoặc để trống)") },
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
                        IconButton(onClick = { expMenu = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn từ khóa")
                        }
                    }
                )
                DropdownMenu(expanded = expMenu, onDismissRequest = { expMenu = false }) {
                    keywordOpts.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = { 
                                inputKeyword = if (opt == "trống (không dùng)") "" else opt
                                expMenu = false 
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.batchDownloadBoxarts(context, inputPlatform, inputKeyword) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Tải lại Boxart toàn bộ game") }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    viewModel.saveRawgApiKey(context, input)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Lưu Cài Đặt") }
        }
    }
}

@Composable
fun PlatformEditDialog(
    initialName: String = "",
    initialPackage: String = "",
    initialPlayInApp: Boolean = false,
    isPreset: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (com.example.data.PlatformConfig) -> Unit
) {
    var newName by remember { mutableStateOf(initialName) }
    var newPkg by remember { mutableStateOf(initialPackage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "Thêm hệ máy mới" else "Chỉnh sửa hệ máy") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Tên hệ máy") },
                    readOnly = isPreset,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPkg,
                    onValueChange = { newPkg = it },
                    label = { Text("Package Name (vd: org.ppsspp.ppsspp)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (newName.isNotBlank()) {
                        onSave(com.example.data.PlatformConfig(newName, newPkg, initialPlayInApp, isPreset))
                    }
                }
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorSettingsFullScreen(
    viewModel: MainViewModel,
    context: android.content.Context,
    customPlatforms: List<com.example.data.PlatformConfig>,
    onBack: () -> Unit
) {
    var cpToDelete by remember { mutableStateOf<String?>(null) }
    var cpToEdit by remember { mutableStateOf<com.example.data.PlatformConfig?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    
    if (cpToDelete != null) {
        AlertDialog(
            onDismissRequest = { cpToDelete = null },
            title = { Text("Xác nhận") },
            text = { Text("Bạn có chắc chắn muốn xóa hệ máy '$cpToDelete'?") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.removeCustomPlatform(context, cpToDelete!!)
                    cpToDelete = null
                }) { Text("Xóa") }
            },
            dismissButton = {
                TextButton(onClick = { cpToDelete = null }) { Text("Hủy") }
            }
        )
    }

    if (isAddingNew) {
        PlatformEditDialog(
            onDismiss = { isAddingNew = false },
            onSave = { config ->
                viewModel.addCustomPlatform(context, config)
                isAddingNew = false
            }
        )
    }

    if (cpToEdit != null) {
        PlatformEditDialog(
            initialName = cpToEdit!!.name,
            initialPackage = cpToEdit!!.packageName,
            initialPlayInApp = cpToEdit!!.playInApp,
            isPreset = cpToEdit!!.isPreset,
            onDismiss = { cpToEdit = null },
            onSave = { config ->
                viewModel.addCustomPlatform(context, config)
                cpToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tùy chỉnh hệ máy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm hệ máy")
            }
        }
    ) { padding ->
        val bottomPadding = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(modifier = Modifier.padding(padding).padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomPadding)) {
            Text("Danh sách hệ máy:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(customPlatforms, key = { it.name }) { cp ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.animateItem().fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cp.name, style = MaterialTheme.typography.titleSmall)
                            Text(cp.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { cpToEdit = cp }) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa", modifier = Modifier.size(20.dp))
                        }
                        if (!cp.isPreset) {
                            IconButton(onClick = { cpToDelete = cp.name }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", modifier = Modifier.size(20.dp))
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
fun GameCardSettingsFullScreen(
    viewModel: MainViewModel,
    context: android.content.Context,
    defaultCardStyle: String,
    defaultImageScale: String,
    defaultShowLabel: Boolean,
    defaultLabelInside: Boolean,
    platforms: List<String>,
    gridColumns: Int,
    globalGameCardScale: Float,
    multiLineLabel: Boolean,
    gameTitleFontWeight: String,
    gameTitleFontSize: Float,
    onBack: () -> Unit
) {
    var cardStyle by remember { mutableStateOf(defaultCardStyle) }
    var imageScale by remember { mutableStateOf(defaultImageScale) }
    var showLabel by remember { mutableStateOf(defaultShowLabel) }
    var labelInside by remember { mutableStateOf(defaultLabelInside) }
    var applyPlatform by remember { mutableStateOf<String?>(null) }
    var isPlatformDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Định dạng thẻ Game") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.pressScale()) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
                    }
                }
            )
        }
    ) { padding ->
        val bottomPadding = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomPadding)) {
            Text("Phong cách thẻ game", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { cardStyle = "default" }.padding(vertical = 4.dp)) {
                RadioButton(selected = cardStyle == "default", onClick = { cardStyle = "default" })
                Spacer(Modifier.width(8.dp))
                Text("Khung mặc định (Material)")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { cardStyle = "switch" }.padding(vertical = 4.dp)) {
                RadioButton(selected = cardStyle == "switch", onClick = { cardStyle = "switch" })
                Spacer(Modifier.width(8.dp))
                Text("Khung Switch")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { cardStyle = "ds" }.padding(vertical = 4.dp)) {
                RadioButton(selected = cardStyle == "ds", onClick = { cardStyle = "ds" })
                Spacer(Modifier.width(8.dp))
                Text("Khung DS")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { cardStyle = "transparent" }.padding(vertical = 4.dp)) {
                RadioButton(selected = cardStyle == "transparent", onClick = { cardStyle = "transparent" })
                Spacer(Modifier.width(8.dp))
                Text("Không khung")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { cardStyle = "clear" }.padding(vertical = 4.dp)) {
                RadioButton(selected = cardStyle == "clear", onClick = { cardStyle = "clear" })
                Spacer(Modifier.width(8.dp))
                Text("Khung trong suốt")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Kiểu hiển thị Boxart", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { imageScale = "original" }.padding(vertical = 4.dp)) {
                RadioButton(selected = imageScale == "original", onClick = { imageScale = "original" })
                Spacer(Modifier.width(8.dp))
                Text("Original (Không bo, vừa khung)")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().bounceClick { imageScale = "fit" }.padding(vertical = 4.dp)) {
                RadioButton(selected = imageScale == "fit", onClick = { imageScale = "fit" })
                Spacer(Modifier.width(8.dp))
                Text("Fit (Phóng to, bo viền 4 góc)")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showLabel, onCheckedChange = { showLabel = it })
                Text("Hiển thị tên game", style = MaterialTheme.typography.titleMedium)
            }
            if (showLabel) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.bounceClick { labelInside = false }) {
                        RadioButton(selected = !labelInside, onClick = { labelInside = false })
                        Text("Bên ngoài")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.bounceClick { labelInside = true }) {
                        RadioButton(selected = labelInside, onClick = { labelInside = true })
                        Text("Bên trong")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Cài đặt tổng thể", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            Text("Số cột lưới: $gridColumns", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = gridColumns.toFloat(),
                onValueChange = { viewModel.updateGridColumns(context, it.toInt()) },
                valueRange = 2f..8f,
                steps = 5
            )

            Text("Kích cỡ thẻ: ${String.format("%.1f", globalGameCardScale)}x", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = globalGameCardScale,
                onValueChange = { viewModel.updateGlobalGameCardScale(context, it) },
                valueRange = 0.5f..2.0f,
                steps = 14
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Checkbox(checked = multiLineLabel, onCheckedChange = { viewModel.toggleMultiLineLabel(context) })
                Text("Nhãn game nhiều dòng", style = MaterialTheme.typography.bodyMedium)
            }

            Text("Font chữ nhãn:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.FilterChip(
                    selected = gameTitleFontWeight == "normal",
                    onClick = { viewModel.updateGameTitleFontWeight(context, "normal") },
                    label = { Text("Thường") },
                    modifier = Modifier.padding(end = 8.dp).pressScale()
                )
                androidx.compose.material3.FilterChip(
                    selected = gameTitleFontWeight == "bold",
                    onClick = { viewModel.updateGameTitleFontWeight(context, "bold") },
                    label = { Text("Đậm") },
                    modifier = Modifier.pressScale()
                )
            }

            Text("Cỡ chữ tên game: ${gameTitleFontSize.toInt()} sp", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            Slider(
                value = gameTitleFontSize,
                onValueChange = { viewModel.updateGameTitleFontSize(context, it) },
                valueRange = 8f..32f,
                steps = 23
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Áp dụng kiểu hệ thống:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Button(onClick = { 
                viewModel.saveDefaultTemplate(context, cardStyle, imageScale, showLabel, labelInside)
                onBack()
            }, modifier = Modifier.fillMaxWidth().pressScale()) {
                Text("Làm Mặc Định Cho Game Mới")
            }
            Button(onClick = {
                viewModel.batchUpdateGameSettings(null, cardStyle, imageScale, showLabel, labelInside)
                onBack()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Áp Dụng Cho Toàn Bộ Game Hiện Tại")
            }

            if (platforms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { isPlatformDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (applyPlatform == null) "Chọn hệ máy..." else "Hệ máy: $applyPlatform")
                        }
                        DropdownMenu(
                            expanded = isPlatformDropdownExpanded,
                            onDismissRequest = { isPlatformDropdownExpanded = false }
                        ) {
                            platforms.forEach { plat ->
                                DropdownMenuItem(
                                    text = { Text(plat) },
                                    onClick = {
                                        applyPlatform = plat
                                        isPlatformDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (applyPlatform != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.batchUpdateGameSettings(applyPlatform, cardStyle, imageScale, showLabel, labelInside)
                            onBack()
                        }) {
                            Text("Áp Dụng")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsFullScreen(
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dữ liệu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val bottomPadding = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomPadding)) {
            Text(
                "Đồng bộ hóa, cập nhật dữ liệu hàng loạt và sao lưu thông tin hệ thống.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Sao Lưu & Khôi Phục", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onExportClick, modifier = Modifier.fillMaxWidth()) {
                Text("Xuất Dữ Liệu (Backup zip)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onImportClick, modifier = Modifier.fillMaxWidth()) {
                Text("Nhập Dữ Liệu (Restore zip)")
            }
        }
    }
}
