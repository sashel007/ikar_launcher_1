package ru.ikar.ikar_launcher.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.Black,
    indicatorSize: Dp = 8.dp,
    spacing: Dp = 4.dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier
    ) {
        for (i in 0 until pagerState.pageCount) {
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .background(
                        color = if (pagerState.currentPage == i) activeColor else inactiveColor,
                        shape = CircleShape
                    )
            )
        }
    }
}
