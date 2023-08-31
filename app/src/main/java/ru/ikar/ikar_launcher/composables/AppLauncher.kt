package ru.ikar.ikar_launcher.composables

import android.content.pm.ResolveInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppLauncher(
    installedApps: List<ResolveInfo>,
    showAllApps: Boolean,
    onToggleAllApps: () -> Unit,
    hideApp: (ResolveInfo) -> Unit
) {
    val context = LocalContext.current
    var isIconButtonClicked by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(true) }
    val columns = 4
    val pages = installedApps.chunked(6 * columns)
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val backgroundPageColor = 0xF57777
    val backgroundTransparency = 0.95f
    val coroutineScope = rememberCoroutineScope()
    val thresholdPx = with(LocalDensity.current) { 25.dp.toPx() }

    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
    ) {

        if (showAllApps) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(backgroundPageColor).copy(alpha = backgroundTransparency))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // This removes the ripple effect
                        role = Role.Button
                    ) {
                        if (showAllApps) onToggleAllApps()
                        showButton = true
                    }
            ) { page ->
                val appsForPage = pages[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    HorizontalAppGrid(
                        installedApps = appsForPage,
                        context = context,
                        hideApp = hideApp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            HorizontalPageIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                activeColor = Color.White,
                inactiveColor = Color.Black
            )
        }

        if (showButton) {
            //кнопка "Открыть приложения"
            IconButton(
                onClick = {
                    onToggleAllApps()
                    isIconButtonClicked = !isIconButtonClicked
                    showButton = false
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .size(30.dp),
                enabled = true,
                colors = IconButtonDefaults.filledIconButtonColors(
                    contentColor = Color.Magenta,
                    disabledContentColor = Color.Gray
                )

            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "App Drawer Icon",
                    tint = Color.White
                )
            }
        }
    }
}