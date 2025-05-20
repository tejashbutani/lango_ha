package com.example.lango_ha

import android.content.Context
import android.util.Log
import android.view.View
import com.xbh.simplewhiteboarddemo.DrawSurfaceView
import com.xbh.whiteboard.AccelerateDraw
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class CustomPlatformView(
    context: Context,
    private val methodChannel: MethodChannel,
    creationParams: Map<String, Any>?
) : PlatformView, MethodChannel.MethodCallHandler {
    private val drawView: DrawSurfaceView = DrawSurfaceView(
        context,
    )

    init {
        methodChannel.setMethodCallHandler(this)
        val mAcd = AccelerateDraw.getInstance()
        Log.d("ACCELERATEDRAW", "version: " + mAcd.getVersion())
//        rendLibView.setMethodChannel(methodChannel)
    }

    override fun getView(): View {
        return drawView
    }

    override fun dispose() {
        methodChannel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "clear" -> {
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }
}