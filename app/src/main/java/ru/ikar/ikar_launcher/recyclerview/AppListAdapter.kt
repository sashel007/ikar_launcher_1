package ru.ikar.ikar_launcher.recyclerview

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.recyclerview.widget.RecyclerView
import ru.ikar.ikar_launcher.R

class AppListAdapter(
    private val apps: List<ResolveInfo>,
    private val hideApp: (ResolveInfo) -> Unit,
    private val appItemComposable: @Composable (app: ResolveInfo, hideApp: (ResolveInfo) -> Unit) -> Unit
) : RecyclerView.Adapter<AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view, appItemComposable, hideApp)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int = apps.size
}
