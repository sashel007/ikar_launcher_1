package ru.ikar.ikar_launcher.composables

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(Intent(Intent.ACTION_VIEW,contractList[0]))
                Log.d("intent_INPUT", "Intent action: ${intent.action}")
            }) {

            }
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, contractList[1])
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }) {

            }
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, contractList[2])
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }) {
            }
        }
    }
}
