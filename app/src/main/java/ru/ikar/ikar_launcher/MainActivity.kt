package ru.ikar.ikar_launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.AudioManager
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import ru.ikar.ikar_launcher.ui.theme.SelectBackgroundActivity
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class MainActivity : ComponentActivity() {

    private lateinit var packageManager: PackageManager
    private var showAllApps by mutableStateOf(false)
    private var installedApps by mutableStateOf(emptyList<ResolveInfo>())
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var backgroundImageUri: Uri? = null
    val contractList: MutableList<Uri> = ArrayList()

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

        requestSystemAlertWindowPermission()

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(R.drawable.back),
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
                    MediaAndClock(contractList)
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

        val tvInputManager = getSystemService(TV_INPUT_SERVICE) as TvInputManager
        var contract: String? = null
        for (tvInputInfo in tvInputManager.tvInputList) {
            when (tvInputInfo.type) {
                TvInputInfo.TYPE_HDMI -> {
                    contract += TvContract.buildChannelUriForPassthroughInput(tvInputInfo.id).toString() + "\n"
                    contractList.add(TvContract.buildChannelUriForPassthroughInput(tvInputInfo.id))
                }
                TvInputInfo.TYPE_DISPLAY_PORT -> Log.d("23", "TYPE_DISPLAY_PORT")
                TvInputInfo.TYPE_TUNER -> Log.d("23", "TYPE_TUNER")
            }
        }
        Log.d("123", "Intent action: ${intent.action}")
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
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, PERMISSION_REQUEST_SYSTEM_ALERT_WINDOW)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_SYSTEM_ALERT_WINDOW = 1001
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

    Box(Modifier.fillMaxSize()) {
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
                    .background(Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Check if the tap is inside the grid area, if not, close the grid
                            if (offset.y < 200.dp.toPx()) {
                                onToggleAllApps()
                                showButton = true
                            }
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
fun MediaAndClock(contractList: MutableList<Uri>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            MediaScreen(contractList)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center) {
            ClockWidget()
        }
    }
}

@Composable
fun MediaScreen(contractList: MutableList<Uri>) {

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .width(700.dp)
            .height(500.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(25.dp)
            .background(color = Color.Black),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, contractList[0])
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(Intent(Intent.ACTION_VIEW,contractList[0]))
                Log.d("intent_INPUT", "Intent action: ${intent.action}")
            }) {

            }
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, contractList[1])
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }) {

            }
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, contractList[2])
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }) {

            }
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
        style = TextStyle(fontSize = 40.sp)
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
    val basicButtonSize = 40.dp
    val iconButtonSize = 30.dp
    var isMenuVisible by remember { mutableStateOf(false) } // State to control menu visibility
    var toucherPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    var isDragging by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val buttonColors = Color(0xFF9B1E1E)
    var isVolumeSliderVisible by remember { mutableStateOf(false) }
    var isBrightnessSliderVisible by remember { mutableStateOf(false) }
    var volumeIconX by remember { mutableStateOf(0f) }
    var volumeIconY by remember { mutableStateOf(0f) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var brightnessPosition by remember { mutableStateOf(0f) }

    fun getSystemIcon(index: Int): Int {
        return when (index) {
            0 -> R.drawable.baseline_settings_24
            1 -> R.drawable.baseline_home_24
            2 -> R.drawable.baseline_volume_up_24
            3 -> R.drawable.baseline_brightness_medium_24
            4 -> R.drawable.baseline_desktop_mac_24
            else -> R.drawable.setting_icon // Return null for any other index or add more cases as needed
        }
    }

    fun openSettings(context: Context) {
        val settingsIntent = Intent(Settings.ACTION_SETTINGS)
        context.startActivity(settingsIntent)
    }

    Box(
        Modifier
            .fillMaxSize()
            .border(
                width = 10.dp,
                color = Color.Green
            )) {
        Box(
            modifier = Modifier
                .size(basicButtonSize)
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
                    detectTapGestures {
                        isMenuVisible = !isMenuVisible // Toggle the menu on button tap
                    }
                }
        ) {
            Button(
                onClick = {
                    // Handle main button click action here
                    isMenuVisible = !isMenuVisible // Toggle the menu on button click
                },
                modifier = Modifier.fillMaxSize(),
                colors = ButtonDefaults.buttonColors(buttonColors.copy(alpha = 0.8f))
            ) {
            }

            // Little buttons (5 buttons around the main button)
            if (isMenuVisible) {
                Box(Modifier.fillMaxSize()) {
                    for (i in 0 until 5) {
                        val angle = i * (360f / 5f)
                        val radius = 120.dp // Adjust the radius as needed

                        val x = cos(Math.toRadians(angle.toDouble())).toFloat() * radius.value
                        val y = sin(Math.toRadians(angle.toDouble())).toFloat() * radius.value

                        if (i == 2) {  // If the current button is the volume icon
                            volumeIconX = with(LocalDensity.current) { x.dp.toPx() }
                            volumeIconY = with(LocalDensity.current) { y.dp.toPx() }
                        }

                        Box(
                            modifier = Modifier
                                .size(iconButtonSize)
                                .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                        ) {
                            val systemIcon = painterResource(getSystemIcon(i))
                            Image(
                                painter = systemIcon,
                                contentDescription = "System Icon",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        onClick = {
                                            when (i) {
                                                0 -> openSettings(context)
                                                1 -> {
                                                    TODO()
                                                }

                                                2 -> {
                                                    isVolumeSliderVisible = !isVolumeSliderVisible
                                                }

                                                3 -> {
                                                    isBrightnessSliderVisible =
                                                        !isBrightnessSliderVisible
                                                }

                                                4 -> {
                                                    val intent = Intent(
                                                        context,
                                                        SelectBackgroundActivity::class.java
                                                    )
                                                    context.startActivity(intent)
                                                }

                                                else -> {
                                                    TODO()
                                                }
                                            }
                                        }
                                    ),
                                contentScale = ContentScale.Fit // Adjust content scale as needed
                            )
                        }
                    }
                }
            }
        }
        if (isVolumeSliderVisible) {
            Box(modifier = Modifier
                .border(2.dp, Color.Yellow)
                .semantics { contentDescription = "Localized Description" }
                .rotate(-90f)
                .width(150.dp)
                .align(Alignment.CenterEnd)
            )
            {
                VolumeSlider(
                    value = sliderPosition,
                    onValueChange = { newSliderPosition ->
                        //прикрутка громкости через аудиоменеджер
                        sliderPosition = newSliderPosition
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        val newVolume = (newSliderPosition * maxVolume).toInt()
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                    }
                )
            }
        }

        if (isBrightnessSliderVisible) {
            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
            Box(modifier = Modifier
                .border(2.dp, Color.Yellow)
                .semantics { contentDescription = "Localized Description" }
                .rotate(-90f)
                .width(150.dp)
                .align(Alignment.CenterEnd)
            )
            {
                BrightnessSlider(
                    value = brightnessPosition,
                    onValueChange = { newBrightnessPosition ->
                        //прикрутка яркости через Setting.SYSTEM
                        brightnessPosition = newBrightnessPosition
                        val contentResolver = context.contentResolver
                        val brightnessMode = Settings.System.getInt(
                            contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE
                        )
                        if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                            // If the brightness mode is set to automatic, switch to manual mode
                            Settings.System.putInt(
                                contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                            )
                        }
                        val maxBrightness = 255
                        val newBrightness = (newBrightnessPosition * maxBrightness).toInt()
                        Settings.System.putInt(
                            contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS,
                            newBrightness
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Slider(
        onValueChange = onValueChange,
        value = value
    )
}

@Composable
fun BrightnessSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        modifier = modifier
    )
}

/*
 функция скрытия выбранного приложения из списка
 ___(В ДОРАБОТКЕ, пока на уровне логов)
 */
