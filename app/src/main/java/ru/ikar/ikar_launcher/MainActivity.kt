package ru.ikar.ikar_launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ru.ikar.ikar_launcher.composables.AppDrawer
import ru.ikar.ikar_launcher.composables.MainScreen

class MainActivity : ComponentActivity() {

    private lateinit var packageManager: PackageManager
    private var showAllApps by mutableStateOf(false)
    private var installedApps by mutableStateOf(emptyList<ResolveInfo>())
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private val contractList: MutableList<Uri> = ArrayList()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("app_package_names", Context.MODE_PRIVATE)

        //скрыл аппбар и навигейшн бар
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        packageManager = applicationContext.packageManager

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT
        ).apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = android.graphics.PixelFormat.TRANSPARENT
            width = 56
            height = 56
            x = 0
            y = 0
        }

        refreshInstalledApps() // Обновит список приложений, если пользователь перед этим скрыл
        // какие-то приложения руками, перед тем как зайти в setContent

        requestSystemAlertWindowPermission()

        setContent {
            val context = LocalContext.current

            Box(modifier = Modifier.fillMaxSize()) {
//                Image(
//                    painter = painterResource(R.drawable.back),
//                    contentDescription = "Background Image",
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.FillBounds
//                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MainScreen(contractList, sharedPreferences)
                }
                AppDrawer(
                    installedApps = installedApps,
                    showAllApps = showAllApps,
                    onToggleAllApps = { showAllApps = !showAllApps },
                    hideApp = { app -> hideApp(context, app) },
                    sharedPreferences = sharedPreferences
                )
//                FloatingToucher()


            }

        }

        //вернуть при тестировании на панели

//        val tvInputManager = getSystemService(TV_INPUT_SERVICE) as TvInputManager
//        var contract: String? = null
//        for (tvInputInfo in tvInputManager.tvInputList) {
//            when (tvInputInfo.type) {
//                TvInputInfo.TYPE_HDMI -> {
//                    contract += TvContract.buildChannelUriForPassthroughInput(tvInputInfo.id).toString() + "\n"
//                    contractList.add(TvContract.buildChannelUriForPassthroughInput(tvInputInfo.id))
//                }
//                TvInputInfo.TYPE_DISPLAY_PORT -> Log.d("23", "TYPE_DISPLAY_PORT")
//                TvInputInfo.TYPE_TUNER -> Log.d("23", "TYPE_TUNER")
//            }
//        }
//        Log.d("123", "Intent action: ${intent.action}")
    }

    /*
    функция извлечения списка пользовательских приложений; фильтрация от системных аппок
    путём отсева приложений с флагом (FLAG_SYSTEM == 0)
     */

    private fun getInstalledApps(packageManager: PackageManager): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        return packageManager.queryIntentActivities(intent, 0)
    }

    private fun requestSystemAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Request the permission
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, PERMISSION_REQUEST_SYSTEM_ALERT_WINDOW)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_SYSTEM_ALERT_WINDOW = 1001
    }

    //обновляет список приложений с учетом того, что пользователь мог скрыть какие-то из списка
//    не работает!!!
    private fun refreshInstalledApps() {
        installedApps = getInstalledApps(packageManager)
    }

    private fun hideApp(context: Context, app: ResolveInfo) {
        val packageName = app.activityInfo.packageName
        val componentName = ComponentName(packageName, app.activityInfo.name)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        Toast.makeText(context, "Приложение спрятано", Toast.LENGTH_SHORT).show()
        Log.d("AppLauncher", "hideApp - Before refreshInstalledApps")
        refreshInstalledApps()
        Log.d("AppLauncher", "hideApp - After refreshInstalledApps")
    }
}


/*
 функция скрытия выбранного приложения из списка
 ___(В ДОРАБОТКЕ, пока на уровне логов)
 */
