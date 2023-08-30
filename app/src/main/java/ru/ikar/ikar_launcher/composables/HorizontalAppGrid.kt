package ru.ikar.ikar_launcher.composables

import android.content.Context
import android.content.pm.ResolveInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalAppGrid(
    installedApps: List<ResolveInfo>,
    context: Context,
    hideApp: (ResolveInfo) -> Unit
) {
    val rows = 4 // or however many rows you want
    val columns = (installedApps.size + rows - 1) / rows // calculate the number of columns needed

    LazyRow {
        items(columns) { columnIndex ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp), // space between apps
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                for (rowIndex in 0 until rows) {
                    val appIndex = columnIndex * rows + rowIndex
                    if (appIndex < installedApps.size) {
                        val app = installedApps[appIndex]
                        AppItem(app, context.packageManager, hideApp)
                    }
                }
            }
        }
    }
}
