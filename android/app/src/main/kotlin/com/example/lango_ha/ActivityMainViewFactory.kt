package com.example.lango_ha

import android.content.Context
import android.view.LayoutInflater
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class ActivityMainViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.activity_main, null, false)
        return ActivityMainPlatformView(view)
    }
} 