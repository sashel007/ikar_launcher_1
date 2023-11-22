package ru.ikar.ikar_launcher.composables

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreenAppGrid(sharedPreferences: SharedPreferences) {
    val context = LocalContext.current
    val state = rememberLazyGridState()
    val itemList = List(8) { }
    val packageNames = getSavedPackageName(sharedPreferences)
    val appIcons = packageNames.map { getAppIconByPackageName(context, it)?.toBitmap()?.asImageBitmap() }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(2), state = state,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(appIcons.size) { index ->
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .background(Color.LightGray)
            ) {
                appIcons[index]?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = "App Icon"
                    )
                }
            }
        }
    }
}

fun getSavedPackageName(sharedPreferences: SharedPreferences): List<String> {
    return (1..8).mapNotNull { key ->
        sharedPreferences.getString("app_$key", null)
    }.filterNotNull()
}

fun getAppIconByPackageName(context: Context, packageName: String): Drawable? {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        null // Или какой-то стандартный Drawable, если пакет не найден
    }
}

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}