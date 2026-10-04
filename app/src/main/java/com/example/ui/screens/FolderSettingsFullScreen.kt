package com.example.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.FolderActivity
import com.example.getBlurredWallpaper
import com.example.ui.theme.bounceClick
import com.example.ui.theme.pressScale
import com.example.ui.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSettingsFullScreen(
    viewModel: MainViewModel,
    context: Context,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    val componentName = ComponentName(context, FolderActivity::class.java)
    val pm = context.packageManager
    var isEnabled by remember { 
        mutableStateOf(pm.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    val prefs = context.getSharedPreferences("folder_settings", Context.MODE_PRIVATE)
    var blurLevel by remember { mutableStateOf(prefs.getFloat("blur_level", 50f)) }
    var isCreatingShortcut by remember { mutableStateOf(false) }
    var isFetchingWallpaper by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(it)?.use { input ->
                        val file = File(context.filesDir, "folder_bg.png")
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thư mục Game Launcher Fold") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.pressScale()) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        headlineContent = { Text("Hiển thị ứng dụng phụ") },
                        supportingContent = { Text("Hiển thị Game Launcher Fold trên màn hình chính (App Drawer)") },
                        trailingContent = {
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    isEnabled = checked
                                    val state = if (checked) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                    pm.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP)
                                }
                            )
                        }
                    )
                }
            }

            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Phím Tắt Lối Tắt", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tạo lối tắt thư mục trực tiếp ra màn hình chính thiết bị.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                isCreatingShortcut = true
                                createFolderShortcut(context)
                                isCreatingShortcut = false
                            },
                            enabled = !isCreatingShortcut,
                            modifier = Modifier.fillMaxWidth().pressScale()
                        ) {
                            Text("Thêm Shortcut ra màn hình chính")
                        }
                    }
                }
            }
        }
    }
}

private fun createFolderShortcut(context: Context) {
    val intent = android.content.Intent(context, FolderActivity::class.java).apply {
        action = android.content.Intent.ACTION_MAIN
        addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        setClassName(context.packageName, FolderActivity::class.java.name)
    }

    val shortcutInfo = ShortcutInfoCompat.Builder(context, "folder_shortcut_${System.currentTimeMillis()}")
        .setShortLabel("Samsung8")
        .setLongLabel("Game Launcher Fold")
        .setIcon(IconCompat.createWithResource(context, com.example.R.mipmap.ic_folder_launcher))
        .setIntent(intent)
        .build()

    ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
}

