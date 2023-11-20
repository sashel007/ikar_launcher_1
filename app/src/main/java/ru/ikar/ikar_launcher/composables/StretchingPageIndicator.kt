package ru.ikar.ikar_launcher.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.lang.Math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StretchingPageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.Black,
    indicatorSize: Dp = 8.dp,
    spacing: Dp = 4.dp
) {
    val currentPage = pagerState.currentPage
    val targetPage = pagerState.targetPage

    val pageOffset: Float = if (currentPage != targetPage) {
        if (currentPage < targetPage) 1f else -1f
    } else {
        0f
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier
    ) {
        for (i in 0 until pagerState.pageCount) {
            val scale = if (i == currentPage || i == currentPage + 1 && pageOffset > 0 || i == currentPage - 1 && pageOffset < 0) {
                1f + abs(pageOffset)
            } else {
                1f
            }

            val dotSize = animateDpAsState(indicatorSize * scale).value

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(
                        color = if (pagerState.currentPage == i) activeColor else inactiveColor,
                        shape = CircleShape
                    )
            )
        }
    }
}





