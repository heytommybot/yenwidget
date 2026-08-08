package com.tommyg.yenwidget

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RefreshWorker.schedule(this)
        TrainRefreshWorker.schedule(this)
        val tv = TextView(this).apply {
            text = "Yen Widget\n\n" +
                "Long-press your home screen → Widgets → Yen Widget, then drag it anywhere.\n\n" +
                "It refreshes hourly on its own. Tap the widget any time to refresh now."
            textSize = 16f
            setPadding(64, 160, 64, 64)
        }
        setContentView(tv)
    }
}
