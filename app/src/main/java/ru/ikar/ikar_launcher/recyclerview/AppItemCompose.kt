package ru.ikar.ikar_launcher.recyclerview

import android.content.Context
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.platform.LocalContext as LocalContext1

@Composable
fun AppItemCompose(app: ResolveInfo, hideApp: (ResolveInfo) -> Unit) {
    val context = LocalContext1.current
    val appName = app.loadLabel(context.packageManager).toString()
    val appIcon = app.loadIcon(context.packageManager).toBitmap().asImageBitmap()

    val popupWidth = 200.dp
    val popupHeight = 100.dp

    var isPopupVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .padding(4.dp)
                .pointerInput(Unit) {
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
                                .height(height = popupHeight)
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Hide the app?",
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
                                Text(text = "Yes")
                            }
                        }
                    }
                )
            }
        }
        Text(
            text = appName,
            textAlign = TextAlign.Center,
            color = Color(android.graphics.Color.rgb(72, 3, 111)),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

//функция запуска приложений внутри развернутого списка
fun launchApp(context: Context, app: ResolveInfo) {
    val packageName = app.activityInfo.packageName
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    context.startActivity(launchIntent)
}

