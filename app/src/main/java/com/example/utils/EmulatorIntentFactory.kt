package com.example.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

interface EmulatorPlatform {
    fun getPackageNames(): List<String>
    fun getLaunchIntent(context: Context, packageName: String, filePath: String): Intent
    fun getResumeIntent(context: Context, packageName: String): Intent
}

class PpssppEmulator : EmulatorPlatform {
    override fun getPackageNames() = listOf("org.ppsspp.ppssppgold", "org.ppsspp.ppsspp")
    
    override fun getLaunchIntent(context: Context, packageName: String, filePath: String): Intent {
        val i = Intent(Intent.ACTION_VIEW)
        i.setDataAndType(Uri.parse(filePath), "application/octet-stream")
        i.setPackage(packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return i
    }

    override fun getResumeIntent(context: Context, packageName: String): Intent {
        val i = context.packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        return i
    }
}

class AetherSx2Emulator : EmulatorPlatform {
    override fun getPackageNames() = listOf("xyz.aethersx2.android")
    
    override fun getLaunchIntent(context: Context, packageName: String, filePath: String): Intent {
        val i = Intent(Intent.ACTION_MAIN)
        i.setClassName(packageName, "xyz.aethersx2.android.MainActivity")
        i.putExtra("bootPath", filePath)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return i
    }

    override fun getResumeIntent(context: Context, packageName: String): Intent {
        val i = context.packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        return i
    }
}

class GbaEmuEmulator : EmulatorPlatform {
    override fun getPackageNames() = listOf("com.explusalpha.GbaEmu")
    
    override fun getLaunchIntent(context: Context, packageName: String, filePath: String): Intent {
        val i = Intent(Intent.ACTION_VIEW)
        i.setDataAndType(Uri.parse(filePath), "application/octet-stream")
        i.setPackage(packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return i
    }

    override fun getResumeIntent(context: Context, packageName: String): Intent {
        val i = context.packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        return i
    }
}

class CustomEmulatorPlatform(private val packageNameStr: String) : EmulatorPlatform {
    override fun getPackageNames() = listOf(packageNameStr)
    
    override fun getLaunchIntent(context: Context, packageName: String, filePath: String): Intent {
        val i = Intent(Intent.ACTION_VIEW)
        i.setDataAndType(Uri.parse(filePath), "application/octet-stream")
        i.setPackage(packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return i
    }

    override fun getResumeIntent(context: Context, packageName: String): Intent {
        val i = context.packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        return i
    }
}

object EmulatorIntentFactory {
    private val defaultEmulators = mapOf(
        "PSP" to PpssppEmulator(),
        "PS2" to AetherSx2Emulator(),
        "GBA" to GbaEmuEmulator()
    )
    
    private val emulators = mutableMapOf<String, EmulatorPlatform>().apply {
        putAll(defaultEmulators)
    }

    private val customConfigs = mutableMapOf<String, com.example.data.PlatformConfig>()

    fun loadCustomPlatforms(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val customPlatformsJson = prefs.getString("custom_platforms", "[]")
        
        // Reset to default
        emulators.clear()
        emulators.putAll(defaultEmulators)
        customConfigs.clear()
        
        try {
            val jsonArray = org.json.JSONArray(customPlatformsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val pkg = obj.optString("package", "")
                val playInApp = obj.optBoolean("playInApp", false)
                val isPreset = obj.optBoolean("isPreset", false)
                
                customConfigs[name] = com.example.data.PlatformConfig(name, pkg, playInApp, isPreset)
                if (!isPreset) {
                    emulators[name] = CustomEmulatorPlatform(pkg)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAvailablePlatforms(): List<String> {
        val allKeys = emulators.keys + customConfigs.keys
        return allKeys.toList().distinct().sorted()
    }

    fun getDirectLaunchIntent(context: Context, platform: String, filePath: String): Intent? {
        if (filePath.startsWith("package:")) {
            val packageName = filePath.removePrefix("package:")
            return context.packageManager.getLaunchIntentForPackage(packageName)
        }
        val config = customConfigs[platform]
        if (config?.playInApp == true) {
            // Web emulator handled in launchGame, no direct external intent
            return null
        }
        
        val emulator = emulators[platform] ?: return null
        val pkgToUse = config?.packageName?.takeIf { it.isNotBlank() }
        
        val installedPackage = if (pkgToUse != null && isPackageInstalled(context, pkgToUse)) {
            pkgToUse
        } else {
            emulator.getPackageNames().firstOrNull { isPackageInstalled(context, it) }
        } ?: return null
        
        return emulator.getLaunchIntent(context, installedPackage, filePath).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun launchGame(context: Context, platform: String, filePath: String) {
        if (filePath.startsWith("package:")) {
            val packageName = filePath.removePrefix("package:")
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(context, "Không tìm thấy ứng dụng", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val config = customConfigs[platform]
        if (config?.playInApp == true) {
            Toast.makeText(context, "Sắp tới sẽ mở Web Emulator cho ${config.name}!", Toast.LENGTH_SHORT).show()
            return
        }

        val emulator = emulators[platform]
        if (emulator == null) {
            Toast.makeText(context, "Không tìm thấy cấu hình cho giả lập $platform", Toast.LENGTH_SHORT).show()
            return
        }
        
        val pkgToUse = config?.packageName?.takeIf { it.isNotBlank() }
        
        val installedPackage = if (pkgToUse != null && isPackageInstalled(context, pkgToUse)) {
            pkgToUse
        } else {
            emulator.getPackageNames().firstOrNull { isPackageInstalled(context, it) }
        }
        
        if (installedPackage == null) {
            val nameToShow = pkgToUse ?: emulator.getPackageNames().first()
            Toast.makeText(context, "Bạn chưa cài đặt giả lập $nameToShow", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (isProcessRunning(context, installedPackage)) {
                Log.d("EmulatorIntentFactory", "Resuming $installedPackage")
                val resumeIntent = emulator.getResumeIntent(context, installedPackage)
                context.startActivity(resumeIntent)
            } else {
                Log.d("EmulatorIntentFactory", "Launching new for $installedPackage")
                val launchIntent = emulator.getLaunchIntent(context, installedPackage, filePath)
                // Important: grant read permission for Uri
                launchIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: try launching again without check
            try {
                val launchIntent = emulator.getLaunchIntent(context, installedPackage, filePath)
                launchIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(launchIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "Lỗi khi mở giả lập: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isProcessRunning(context: Context, packageName: String): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = am.runningAppProcesses ?: return false
        for (processInfo in runningAppProcesses) {
            if (processInfo.processName == packageName) {
                return true
            }
        }
        return false
    }
}
