package ru.ikar.ikar_launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.compose.foundation.gestures.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import android.content.pm.ApplicationInfo


class MainActivity : ComponentActivity() {

    private lateinit var packageManager: PackageManager
    private var showAllApps by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageManager = applicationContext.packageManager

        val installedApps = getInstalledApps(packageManager)

        setContent {
            AppLauncher(installedApps,showAllApps) {
                showAllApps = !showAllApps
            }
        }
    }

    //функция извлечения списка приложений
    @Suppress("DEPRECATION")
    private fun getInstalledApps(packageManager: PackageManager): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        return resolveInfoList.filter { resolveInfo ->
            val flags = PackageManager.MATCH_UNINSTALLED_PACKAGES
            val appInfo = packageManager.getApplicationInfo(
                resolveInfo.activityInfo.packageName,
                flags
            )
            appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
        }
    }
}

@Composable
fun AppLauncher(
    installedApps: List<ResolveInfo>,
    showAllApps: Boolean,
    onToggleAllApps: () -> Unit
) {
    val context = LocalContext.current
    val columns = 4
    val cells = GridCells.Fixed(columns)

    Box(Modifier.fillMaxSize()) {

        Button(
            onClick = { onToggleAllApps() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = if (showAllApps) "Скрыть приложения" else "Открыть приложения")
        }

        if (showAllApps) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .padding(bottom = 72.dp)
            ) {
                LazyVerticalGrid(cells) {
                    items(installedApps.size) { index ->
                        val app = installedApps[index]
                        AppItem(app, context.packageManager)
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(app: ResolveInfo, packageManager: PackageManager) {
    val context = LocalContext.current
    val appName = app.loadLabel(packageManager).toString()
    val appIcon = app.loadIcon(packageManager).toBitmap().asImageBitmap()

    var isPopupVisible by remember { mutableStateOf(false) }
    val longPressInProgress = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { longPressInProgress.value = true },
                    onPress = { longPressInProgress.value = false }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            bitmap = appIcon,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clickable {
                    launchApp(context,app)
                },
        )
        Text(text = appName,
             textAlign = TextAlign.Center,
             modifier = Modifier.fillMaxWidth())

        if (isPopupVisible) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(48.dp)
                    .background(Color.Gray.copy(alpha = 0.5f))
                    .border(1.dp, Color.Gray)
            ) {
                Button(
                    onClick = {
                        hideApp(context, app)
                        // Dismiss the pop-up window
                        isPopupVisible = false
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                ) {
                    Text(text = "Hide Application")
                }
            }
        }
    }
}

private fun launchApp(context: Context, app: ResolveInfo) {
    val packageName = app.activityInfo.packageName
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    context.startActivity(launchIntent)
}

private fun hideApp(context: Context, app: ResolveInfo) {
    val packageName = app.activityInfo.packageName
    val packageManager = context.packageManager

    try {
        packageManager.setApplicationEnabledSetting(
            packageName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        Toast.makeText(context, "Приложение спрятано", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Спрятать не удалось", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}



