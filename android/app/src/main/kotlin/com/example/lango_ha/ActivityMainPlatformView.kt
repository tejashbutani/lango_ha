package com.example.lango_ha

import android.view.View
import io.flutter.plugin.platform.PlatformView

class ActivityMainPlatformView(private val view: View) : PlatformView {
    override fun getView(): View = view
    override fun dispose() {}
} 