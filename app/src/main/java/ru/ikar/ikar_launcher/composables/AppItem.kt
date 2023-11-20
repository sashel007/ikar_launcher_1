package ru.ikar.ikar_launcher.composables

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap

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

    //функция запуска приложений внутри развернутого списка
    fun launchApp(context: Context, app: ResolveInfo) {
        val packageName = app.activityInfo.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        context.startActivity(launchIntent)
    }

    //Икона приложения с названием под ним + диалоговое окно при зажатии на приложении
    Column(
        modifier = Modifier.padding(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
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
                modifier = Modifier.size(70.dp)
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = Color(android.graphics.Color.WHITE),
            modifier = Modifier.padding(top = 4.dp).width(80.dp)
        )
    }
}