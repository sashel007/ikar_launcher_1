package ru.ikar.ikar_launcher.composables

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalAppGrid(
    installedApps: List<ResolveInfo>,
    context: Context,
    hideApp: (ResolveInfo) -> Unit,
    modifier: Modifier,
    sharedPreferences: SharedPreferences
) {
    val rows = 4
    val columns = (installedApps.size + rows - 1) / rows

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        )
        {
            items(columns) { columnIndex ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp), // space between apps
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(6.dp)
                ) {
                    for (rowIndex in 0 until rows) {
                        val appIndex = columnIndex * rows + rowIndex
                        if (appIndex < installedApps.size) {
                            val app = installedApps[appIndex]
                            AppItem(app, context.packageManager, hideApp, sharedPreferences)
                        }
                    }
                }
            }
        }
    }
}
