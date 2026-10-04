package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val filePath: String,
    val platform: String,
    val boxartUrl: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "1") val showLabel: Boolean = true,
    @ColumnInfo(defaultValue = "0") val labelInside: Boolean = false,
    @ColumnInfo(defaultValue = "'default'") val cardStyle: String = "default",
    @ColumnInfo(defaultValue = "'fit'") val imageScale: String = "fit",
    @ColumnInfo(defaultValue = "1.0") val sizeScale: Float = 1.0f
)
