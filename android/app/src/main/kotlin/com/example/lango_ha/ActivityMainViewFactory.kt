package com.example.lango_ha

import android.content.Context
import android.view.LayoutInflater
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class ActivityMainViewFactory(private val messenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.activity_main, null, false)
        val channel = MethodChannel(messenger, "custom_canvas_view_$viewId")
        val creationParams = args as? Map<String, Any>
        return ActivityMainPlatformView(view, channel, creationParams)
    }
}