package ru.ikar.ikar_launcher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

class MainActivity : ComponentActivity() {

    private lateinit var packageManager: PackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageManager = applicationContext.packageManager

        val installedApps = getInstalledApps(packageManager)
        val userInstalledApps = filterSystemApps(installedApps)

        setContent {
            AppLauncher(installedApps)
        }
    }

    //функция извлечения списка приложений
    private fun getInstalledApps(packageManager: PackageManager): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0)
    }

    private fun filterSystemApps(apps: List<ResolveInfo>): List<ResolveInfo> {
        return apps.filter { app ->
            app.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
        }
    }

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
}

@Composable
fun AppLauncher(installedApps: List<ResolveInfo>) {
    val context = LocalContext.current
    val columns = 4
    val cells = GridCells.Fixed(columns)

    LazyVerticalGrid(cells) {
        items(installedApps.size) { index ->
            val app = installedApps[index]
            AppItem(app, context.packageManager)
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