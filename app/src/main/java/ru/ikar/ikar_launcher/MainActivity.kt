package ru.ikar.ikar_launcher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private lateinit var packageManager: PackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageManager = applicationContext.packageManager

        val installedApps = getInstalledApps(packageManager)

        setContent {
            AppLauncher(installedApps)
        }
    }

    //функция извлечения списка приложений
    private fun getInstalledApps(packageManager: PackageManager): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
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
fun AppLauncher(installedApps: List<ApplicationInfo>) {
    val context = LocalContext.current
    LazyColumn {
        itemsIndexed(installedApps) { _, app ->
            AppItem(app, context.packageManager)
        }
    }
}

@Composable
fun AppItem(app: ApplicationInfo, packageManager: PackageManager) {
    val appName = packageManager.getApplicationLabel(app).toString()
    Text(text = appName,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth())
}