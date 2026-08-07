package com.tommyg.yenwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.util.Locale
import kotlin.math.abs

class YenWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snap = RateRepository.snapshot(context)
        provideContent { WidgetContent(snap) }
    }
}

private val Surface = Color(0xFFF3F0EA)
private val Ink = Color(0xFF1D1D1F)
private val Dim = Color(0x99000000)
private val Up = Color(0xFF137333)

@Composable
private fun WidgetContent(snap: RateRepository.Snapshot) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Surface))
            .cornerRadius(28.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(actionRunCallback<RefreshAction>())
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    "USD → JPY",
                    style = TextStyle(
                        color = ColorProvider(Dim),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                Text(
                    snap.rate?.let { "¥" + String.format(Locale.US, "%.2f", it) } ?: "—",
                    style = TextStyle(
                        color = ColorProvider(Ink),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val ch = snap.changePct
                if (ch != null) {
                    Text(
                        (if (ch >= 0) "▲ " else "▼ ") +
                            String.format(Locale.US, "%.2f%%", abs(ch)),
                        style = TextStyle(
                            color = ColorProvider(Up),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        RateRepository.refresh(context)
        YenWidget().update(context, glanceId)
    }
}
