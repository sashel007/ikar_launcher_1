package ru.ikar.ikar_launcher.recyclerview

import android.content.pm.ResolveInfo
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import ru.ikar.ikar_launcher.R
import ru.ikar.ikar_launcher.composables.AppItem

class AppViewHolder(
    itemView: View,
    private val appItemCompose: @Composable (app: ResolveInfo, hideApp: (ResolveInfo) -> Unit) -> Unit,
    private val hideApp: (ResolveInfo) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    private val composeView: ComposeView = itemView.findViewById(R.id.appItemCompose)

    fun bind(app: ResolveInfo) {
        composeView.setContent {
            appItemCompose(app, hideApp)
        }
    }
}


