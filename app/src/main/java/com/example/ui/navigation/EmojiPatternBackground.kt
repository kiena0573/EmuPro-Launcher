package com.example.ui.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import kotlin.math.abs

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset

@Composable
fun EmojiPatternBackground(
    color: Color,
    preset: String,
    style: String,
    density: Int,
    animated: Boolean = true
) {
    val alpha = when (style) {
        "Rất nhạt" -> 0.04f
        "Nhạt" -> 0.08f
        "Vừa" -> 0.16f
        "Đậm" -> 0.25f
        else -> 0.08f
    }
    val tintColor = color.copy(alpha = alpha)

    val infiniteTransition = rememberInfiniteTransition(label = "pattern_animation")
    val driftX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 30f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX"
    )
    val driftY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 20f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )

    if (preset == "Hình học Grid") {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = alpha * 3f)
        ) {
            val step = (100 - density * 15).coerceAtLeast(30).dp.toPx()
            val dotRadius = 2.dp.toPx()
            var x = (driftX % step) - step
            while (x < size.width + step) {
                var y = (driftY % step) - step
                while (y < size.height + step) {
                    drawCircle(
                        color = color,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    y += step
                }
                x += step
            }
        }
        return
    }

    val gamerEmojis = listOf("🎮", "🕹️", "👾", "🎯", "🎲", "🃏", "⚡", "⭐️", "💎", "⚔️", "🛡️", "🏆")
    val spaceEmojis = listOf("🌙", "⭐", "✨", "🪐", "☄️", "🌌", "☀️", "☁️", "🚀")
    val animeEmojis = listOf("🌸", "⚡", "🍥", "🎏", "⛩️", "🍡", "🍵", "🏮")
    val flowerEmojis = listOf("🌸", "🌹", "🌺", "🌻", "🌼", "🌷", "🪷", "💮", "🌿")
    val catEmojis = listOf("🐱", "🐈", "🙀", "😻", "😽", "😼", "😸", "😹")
    val dogEmojis = listOf("🐶", "🐕", "🦮", "🐩", "🐾", "🐕‍🦺", "🦊", "🐼")
    val fruitEmojis = listOf("🍎", "🍓", "🍉", "🍒", "🍑", "🍍", "🥭", "🍇", "🍌", "🍋")
    val mixEmojis = gamerEmojis + flowerEmojis + catEmojis + dogEmojis + fruitEmojis + spaceEmojis + animeEmojis

    val emojis = when (preset) {
        "Gamer Retro" -> gamerEmojis
        "Vũ trụ Stars" -> spaceEmojis
        "Anime Pop" -> animeEmojis
        "Hoa" -> flowerEmojis
        "Mèo" -> catEmojis
        "Cún" -> dogEmojis
        "Hoa quả" -> fruitEmojis
        else -> mixEmojis
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(color = tintColor, blendMode = BlendMode.SrcIn)
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val patternSize = (110 - (density * 15)).coerceAtLeast(40).dp
            val cols = (maxWidth.value / patternSize.value).toInt() + 2
            val rows = (maxHeight.value / patternSize.value).toInt() + 2

            for (i in -1..cols) {
                for (j in -1..rows) {
                    val isOffset = j % 2 != 0
                    val xOffset = if (isOffset) patternSize / 2 else 0.dp
                    
                    val emojiIndex = abs(i * 7 + j * 13) % emojis.size
                    val emoji = emojis[emojiIndex]
                    
                    val rotation = ((i * 31 + j * 17) % 4) * 45f - 45f

                    Box(
                        modifier = Modifier
                            .offset(
                                x = (i * patternSize.value).dp + xOffset + driftX.dp,
                                y = (j * patternSize.value).dp + driftY.dp
                            )
                            .size(patternSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            style = TextStyle(fontSize = (patternSize.value * 0.42).sp),
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }
        }
    }
}

