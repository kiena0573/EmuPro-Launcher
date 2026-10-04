package com.example.data

data class PlatformConfig(
    val name: String,
    val packageName: String,
    val playInApp: Boolean = false,
    val isPreset: Boolean = false
)
