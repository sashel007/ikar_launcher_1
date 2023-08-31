package ru.ikar.ikar_launcher.composables

import android.content.pm.ResolveInfo
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.google.accompanist.pager.PagerState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.ikar.ikar_launcher.recyclerview.AppItemCompose
import ru.ikar.ikar_launcher.recyclerview.AppListAdapter
import ru.ikar.ikar_launcher.ui.theme.GridSpacingItemDecoration
import androidx.compose.ui.unit.dp

//@Composable
//fun AppLauncher(
//    installedApps: List<ResolveInfo>,
//    showAllApps: Boolean,
//    onToggleAllApps: () -> Unit,
//    hideApp: (ResolveInfo) -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally, // Align children horizontally
//        modifier = Modifier.fillMaxWidth() // Fill the width of the parent
//    ) {
//        if (showAllApps) {
//            AndroidView(
//                factory = { context ->
//                    val recyclerView = RecyclerView(context)
//                    val spanCount = 5
//                    val spacing = 16.dp.value.toInt()
//                    recyclerView.layoutManager = GridLayoutManager(
//                        context,
//                        spanCount,
//                        RecyclerView.VERTICAL,
//                        false
//                    )
//                    recyclerView.adapter = AppListAdapter(
//                        apps = installedApps,
//                        hideApp = hideApp,
//                        appItemComposable = { app, hideApp -> AppItemCompose(app, hideApp) }
//                    )
//                    recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, true))
//                    recyclerView.layoutParams = ViewGroup.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT
//                    )
//                    recyclerView
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.weight(1f)) // Add spacer to push content to the top
//
//        IconButton(
//            onClick = onToggleAllApps,
//            modifier = Modifier
//                .size(40.dp) // Set the size of the button
//                .padding(bottom = 16.dp), // Adjust the padding as needed
//            enabled = true,
//            colors = IconButtonDefaults.filledIconButtonColors(
//                contentColor = Color.Magenta,
//                disabledContentColor = Color.Gray
//            )
//        ) {
//            Icon(
//                imageVector = Icons.Default.Menu,
//                contentDescription = "App Drawer Icon",
//                tint = Color.White
//            )
//        }
//    }
//}



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
    val cells = GridCells.Fixed(columns)
    val pages = installedApps.chunked(6 * columns)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(12.dp))
    ) {

        if (showAllApps) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
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

//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
//                    .padding(horizontal = 16.dp, vertical = 16.dp)
//                    .background(Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(15.dp))
//                    .clip(RoundedCornerShape(20.dp))
//                    .pointerInput(Unit) {
//                        detectTapGestures { offset ->
//                            // Check if the tap is inside the grid area, if not, close the grid
//                            if (offset.y < 200.dp.toPx()) {
//                                onToggleAllApps()
//                                showButton = true
//                            }
//                        }
//                    }
//
//
//            ) {
//                HorizontalAppGrid(
//                    installedApps = installedApps,
//                    context = context,
//                    hideApp = hideApp
//                )
//            }

    }
}