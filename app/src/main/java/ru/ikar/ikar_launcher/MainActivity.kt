package ru.ikar.ikar_launcher

import android.content.ComponentName
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
import android.util.Log
import androidx.compose.ui.window.Dialog

class MainActivity : ComponentActivity() {

    private lateinit var packageManager: PackageManager
    private var showAllApps by mutableStateOf(false)
    private var installedApps by mutableStateOf(emptyList<ResolveInfo>())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageManager = applicationContext.packageManager

        refreshInstalledApps() // Обновит список приложений, если пользователь перед этим скрыл
                                // какие-то приложения руками, перед тем как зайти в setContent


        setContent {
            val context = LocalContext.current
            AppLauncher(
                installedApps = installedApps,
                showAllApps = showAllApps,
                onToggleAllApps = { showAllApps = !showAllApps },
                hideApp = { app -> hideApp(context, app) }
            )
        }

    }

    /*
    функция извлечения списка пользовательских приложений; фильтрация от системных аппок
    путём отсева приложений с флагом (FLAG_SYSTEM == 0)
     */
    @Suppress("DEPRECATION")
    private fun getInstalledApps(packageManager: PackageManager): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        //фильтруем только установленные аппки
        return resolveInfoList.filter { resolveInfo ->
            val flags = PackageManager.MATCH_UNINSTALLED_PACKAGES
            val appInfo = packageManager.getApplicationInfo(
                resolveInfo.activityInfo.packageName,
                flags
            )
            appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
        }
    }

    //обновляет список приложений с учетом того, что пользователь мог скрыть какие-то из списка
    fun refreshInstalledApps() {
        installedApps = getInstalledApps(packageManager)
    }

    fun hideApp(context: Context, app: ResolveInfo) {
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

//стартовове вью с кнопкой и скрытым списком
@Composable
fun AppLauncher(
    installedApps: List<ResolveInfo>,
    showAllApps: Boolean,
    onToggleAllApps: () -> Unit,
    hideApp: (ResolveInfo) -> Unit
) {
    val context = LocalContext.current
    val columns = 4
    val cells = GridCells.Fixed(columns)

    Box(Modifier.fillMaxSize()) {
        //кнопка "Открыть приложения"
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
                        AppItem(app, context.packageManager, hideApp)
                    }
                }
            }
        }
    }
}

//отрисовка приложений внутри списка приложений
@Composable
fun AppItem(app: ResolveInfo, packageManager: PackageManager, hideApp: (ResolveInfo) -> Unit) {
    val context = LocalContext.current
    val appName = app.loadLabel(packageManager).toString()
    val appIcon = app.loadIcon(packageManager).toBitmap().asImageBitmap()

    val popupWidth = 200.dp
    val popupHeight = 100.dp

    /*
    запоминалка состояний при сворачивании списка через функцию
    рекомпозиции mutableStateOf()
     */

    var isPopupVisible by remember { mutableStateOf(false) }

    //Икона приложения с названием под ним + диалоговое окно при зажатии на приложении
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            modifier = Modifier
                .size(48.dp)
                .pointerInput(Unit) {
                    /*
                    одно нажатие на аппку = открыть приложение;
                    зажать палец на аппке = открыть диалоговое окно
                    с предложением скрыть приложения из списка
                     */
                    detectTapGestures(
                        onTap = { launchApp(context, app) },
                        onLongPress = { isPopupVisible = true}
                    )
                }
        ) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            if (isPopupVisible) {
                Dialog(
                    onDismissRequest = { isPopupVisible = false },
                    content = {
                        Column(
                            modifier = Modifier
                                .width(popupWidth)
                                .height(popupHeight)
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Скрыть приложение?",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { hideApp(app)
                                    isPopupVisible = false
                                          },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Да ")
                            }
                        }
                    })
            }

        }
        Text(
            text = appName,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

//функция открывания приложений внутри развернутого списка
fun launchApp(context: Context, app: ResolveInfo) {
    val packageName = app.activityInfo.packageName
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    context.startActivity(launchIntent)
}

/*
 функция скрытия выбранного приложения из списка
 ___(В ДОРАБОТКЕ, пока на уровне логов)
 */




