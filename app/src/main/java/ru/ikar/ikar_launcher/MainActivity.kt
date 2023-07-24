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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import android.util.Log
import android.view.WindowManager
import android.widget.CalendarView
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var packageManager: PackageManager
    private var showAllApps by mutableStateOf(false)
    private var installedApps by mutableStateOf(emptyList<ResolveInfo>())
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
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

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(R.drawable.img),
                    contentDescription = "Background Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                val context = LocalContext.current
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalendarAndClock()
                }
                AppLauncher(
                    installedApps = installedApps,
                    showAllApps = showAllApps,
                    onToggleAllApps = { showAllApps = !showAllApps },
                    hideApp = { app -> hideApp(context, app) }
                )
                FloatingToucher()
           }
        }
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



//    @Suppress("DEPRECATION")
//    private fun getInstalledApps(packageManager: PackageManager): List<ResolveInfo> {
//        val intent = Intent(Intent.ACTION_MAIN, null).apply {
//            addCategory(Intent.CATEGORY_LAUNCHER)
//        }
//        val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
//            intent,
//            PackageManager.MATCH_DEFAULT_ONLY
//        )
//        return resolveInfoList
//
//        //фильтруем только установленные аппки
////        return resolveInfoList.filter { resolveInfo ->
////            val flags = PackageManager.MATCH_UNINSTALLED_PACKAGES
////            val appInfo = packageManager.getApplicationInfo(
////                resolveInfo.activityInfo.packageName,
////                flags
////            )
////            appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
////        }
//    }

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

//стартововые вью с кнопкой и скрытым списком
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

    var swipeProgress by remember { mutableStateOf(0f) }
    var startY by remember { mutableStateOf(0f) }
    var swipingInProgress by remember { mutableStateOf(false) }
    var totalDragDistance by remember { mutableStateOf(0f) }
    var verticalDragDistance by remember { mutableStateOf(0f) }


    Box(Modifier.fillMaxSize()) {
        if (isIconButtonClicked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.5f))
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

        val columns = 4
        val cells = GridCells.Fixed(columns)

        if (showAllApps) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .background(Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            verticalDragDistance += dragAmount
                            if (verticalDragDistance > with(LocalDensity.current) { 200.dp.toPx() }) {
                                onToggleAllApps() // Close the grid
                                verticalDragDistance = 0f // Reset the drag distance after closing
                            }
                            change.consume()
                        }
                    }
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
    ) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .pointerInput(Unit) {
                    /*
                    одно нажатие на аппку = открыть приложение;
                    зажать палец на аппке = открыть диалоговое окно
                    с предложением скрыть приложения из списка
                     */
                    detectTapGestures(
                        onTap = { launchApp(context, app) },
                        onLongPress = { isPopupVisible = true }
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
                                onClick = {
                                    hideApp(app)
                                    isPopupVisible = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Да ")
                            }
                        }
                    }
                )
            }
        }
        Text(
            text = appName,
            textAlign = TextAlign.Center,
            color = Color(android.graphics.Color.rgb(72, 3,111)),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun CalendarAndClock() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            CalendarWidget()
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            ClockWidget()
        }
    }
}

// календарь
@Composable
fun CalendarWidget() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(android.graphics.Color.rgb(255, 153, 184)))
        ) {
            AndroidView(
                factory = { ctx ->
                    CalendarView(ctx).apply {
                        setBackgroundColor(
                            Color(
                                android.graphics.Color.rgb(
                                    255,
                                    153,
                                    184
                                )
                            ).toArgb()
                        )
                        setPadding(0,0,0,0)
                    }
                }
            )
        }
    }
}

//часы
@Composable
fun ClockWidget() {
    val currentTime = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }
    Text(
        text = currentTime.value,
        style = TextStyle(fontSize = 16.sp)
    )
}

//функция запуска приложений внутри развернутого списка
fun launchApp(context: Context, app: ResolveInfo) {
    val packageName = app.activityInfo.packageName
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    context.startActivity(launchIntent)
}

@Composable
fun FloatingToucher() {
    val context = LocalContext.current
    val toucherSize = 56.dp
    var toucherPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    var isDragging by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var isMenuVisible by remember { mutableStateOf(false) } // State to control menu visibility

    Box(
        modifier = Modifier
            .padding(16.dp)
            .size(toucherSize)
            .graphicsLayer(
                translationX = toucherPosition.x,
                translationY = toucherPosition.y
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom == 1f) {
                        if (isDragging) {
                            toucherPosition = toucherPosition.plus(pan)
                        } else {
                            isDragging = true
                            touchOffset = Offset(
                                toucherPosition.x - pan.x,
                                toucherPosition.y - pan.y
                            )
                        }
                    }
                }
            }
            .clickable {
                // Handle tap action here
                isMenuVisible = true // Show the menu on button click
            },
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                // Handle button click action here
                isMenuVisible = true // Show the menu on button click
            },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomCenter)
                .border(2.dp, Color.Red, CircleShape) // Add a 2dp red border around the button
        ) {
            Text("Button")
        }
    }

    // Popup menu
    if (isMenuVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTapGestures {
                        isMenuVisible = false // Close the menu on outside tap
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Column {
                    Button(
                        onClick = {
                            // Handle menu option 1 action here
                            Toast.makeText(context, "Option 1 clicked!", Toast.LENGTH_SHORT).show()
                            isMenuVisible = false // Close the menu after clicking an option
                        }
                    ) {
                        Text("Option 1")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // Handle menu option 2 action here
                            Toast.makeText(context, "Option 2 clicked!", Toast.LENGTH_SHORT).show()
                            isMenuVisible = false // Close the menu after clicking an option
                        }
                    ) {
                        Text("Option 2")
                    }
                }
            }
        }
    }
}




/*
 функция скрытия выбранного приложения из списка
 ___(В ДОРАБОТКЕ, пока на уровне логов)
 */
