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
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding

private val CardBg = Color(0xFFF3F0EA)
private val Ink = Color(0xFF1D1D1F)
private val Label = Color(0x99000000)
private val Good = Color(0xFF137333)
private val Bad = Color(0xFFB3261E)

class YamanoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val status = TrainRepository.status(context)
        provideContent { TrainContent(status) }
    }
}

@Composable
private fun TrainContent(status: TrainRepository.Status) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(CardBg))
            .cornerRadius(28.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(actionRunCallback<TrainRefreshAction>())
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "YAMANOTE LINE",
                style = TextStyle(
                    color = ColorProvider(Label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )
            )
            val normal = status.normal
            Text(
                when (normal) {
                    null -> "tap to load"
                    true -> "✓ ${status.text ?: "Normal service"}"
                    false -> "! ${status.text ?: "Disruption"}"
                },
                style = TextStyle(
                    color = ColorProvider(
                        when (normal) {
                            null -> Ink
                            true -> Good
                            false -> Bad
                        }
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
    }
}

class TrainRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        TrainRepository.refresh(context)
        YamanoteWidget().update(context, glanceId)
    }
}
