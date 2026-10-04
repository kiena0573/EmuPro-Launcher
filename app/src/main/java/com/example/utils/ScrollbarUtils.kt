package com.example.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.simpleVerticalScrollbar(
    state: LazyGridState,
    width: Dp = 4.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )

    drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        val totalItems = layoutInfo.totalItemsCount

        if (visibleItems.isEmpty() || totalItems <= visibleItems.size) {
            return@drawWithContent
        } // Don't draw if content is smaller than viewport

        if (alpha > 0f) {
            val elementHeight = size.height / visibleItems.size.toFloat()
            val firstVisibleIndex = visibleItems.first().index

            val scrollbarHeight = (visibleItems.size.toFloat() / totalItems.toFloat()) * size.height
            val scrollbarOffsetY = (firstVisibleIndex.toFloat() / totalItems.toFloat()) * size.height

            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.5f * alpha),
                topLeft = Offset(size.width - width.toPx() - 4.dp.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
            )
        }
    }
}
