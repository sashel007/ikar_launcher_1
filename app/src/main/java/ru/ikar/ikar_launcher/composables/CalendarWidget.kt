package ru.ikar.ikar_launcher.composables

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

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