package ru.ikar.ikar_launcher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

class MainActivity : ComponentActivity() {

    private lateinit var packageManager: PackageManager
    private var showAllApps by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageManager = applicationContext.packageManager

        val installedApps = getInstalledApps(packageManager)
        val userInstalledApps = filterSystemApps(installedApps)

        setContent {
            AppLauncher(installedApps,showAllApps) {
                showAllApps = !showAllApps
            }
        }
    }

    //функция извлечения списка приложений
    private fun getInstalledApps(packageManager: PackageManager): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0)
    }

    //фильтр, выдающий только системные аппки
    private fun filterSystemApps(apps: List<ResolveInfo>): List<ResolveInfo> {
        return apps.filter { app ->
            app.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
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
        if (showAllApps) {
            LazyVerticalGrid(cells) {
                items(installedApps.size) { index ->
                    val app = installedApps[index]
                    AppItem(app, context.packageManager)
                }
            }
        }

        Button(
            onClick = { onToggleAllApps() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = if (showAllApps) "Hide All Apps" else "Show All Apps")
        }
    }
}

@Composable
fun AppItem(app: ResolveInfo, packageManager: PackageManager) {
    val appName = app.loadLabel(packageManager).toString()
    val appIcon = app.loadIcon(packageManager).toBitmap().asImageBitmap()

    Column(modifier = Modifier.padding(8.dp)) {
        Image(
            bitmap = appIcon,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Text(text = appName)
    }
}

//@Composable
//fun DefaultIcon() {
//    val defaultIconId = android.R.drawable.sym_def_app_icon
//    val defaultIcon = LocalContext.current.resources.getDrawable(defaultIconId, null).toBitmap()
//        .asImageBitmap()
//    Image(
//        bitmap = defaultIcon,
//        contentDescription = null,
//        modifier = Modifier.size(48.dp)
//    )
//}

//@Composable
//fun AppIcon(appIcon: ImageBitmap) {
//    Image(
//        bitmap = appIcon,
//        contentDescription = null,
//        modifier = Modifier.size(48.dp)
//    )
//}

//    private fun getInstalledApps(packageManager: PackageManager): List<ApplicationInfo> {
//        val intent = Intent(Intent.ACTION_MAIN, null).apply {
//            addCategory(Intent.CATEGORY_LAUNCHER)
//        }
//        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
//        val installedApps = mutableListOf<ApplicationInfo>()
//
//        for (resolveInfo in resolveInfoList) {
//            val packageName = resolveInfo.activityInfo.packageName
//            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
//            val appInfo = packageInfo.applicationInfo
//            installedApps.add(appInfo)
//        }
//
//        return installedApps
//    }
