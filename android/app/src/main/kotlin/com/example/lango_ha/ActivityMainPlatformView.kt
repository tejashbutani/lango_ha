package com.example.lango_ha

import android.graphics.Color
import android.view.View
import com.xbh.simplewhiteboarddemo.DrawSurfaceView
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class ActivityMainPlatformView(
    private val v: View,
    private val methodChannel: MethodChannel,
    creationParams: Map<String, Any>?
) : PlatformView, MethodChannel.MethodCallHandler {

    //Add code to get the view by ID
    private val view: DrawSurfaceView = v.findViewById(R.id.drawSv)


    init{
        methodChannel.setMethodCallHandler(this)
        view.setMethodChannel(methodChannel)

        // Handle initial pen settings
        val isDashed = creationParams?.get("isDashed") as? Boolean ?: false
        view.setDashed(isDashed)
        view.updatePenColor(
            (creationParams?.get("color") as? Number)?.toInt() ?: Color.BLACK
        )
        view.updatePenWidth(
            (creationParams?.get("width") as? Double)?.toFloat() ?: 1.0f
        )
        view.setDoublePenThreshold(
            (creationParams?.get("doublePenThreshold") as? Double)?.toFloat() ?: 5.0f
        )
        view.setFingerAsEraserThreshold(
            (creationParams?.get("fingerAsEraserThreshold") as? Double)?.toFloat() ?: 5.0f
        )
        view.setFistAsEraserThreshold(
            (creationParams?.get("fistAsEraserThreshold") as? Double)?.toFloat() ?: 5.0f
        )
        view.setFingerAsEraserEnabled(
            (creationParams?.get("fingerAsEraserEnabled") as? Boolean) ?: false
        )
        view.setFistAsEraserEnabled(
            (creationParams?.get("fistAsEraserEnabled") as? Boolean) ?: false
        )
        view.setDoublePenModeEnabled(
            (creationParams?.get("doublePenModeEnabled") as? Boolean) ?: false
        )
        view.setDoublePenColor1(
            (creationParams?.get("doublePenColor1") as? Number)?.toInt() ?: Color.BLACK
        )
        view.setDoublePenColor2(
            (creationParams?.get("doublePenColor2") as? Number)?.toInt() ?: Color.BLACK
        )
    }

    override fun getView(): View = v

    override fun dispose() {
        methodChannel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "updatePenColor" -> {
                val color = (call.argument<Number>("color"))?.toInt()
                android.util.Log.d("PenSettings", "Received method call 0 - Color: $color")
                if (color != null) {
                    view.updatePenColor(color)
                    result.success(null)
                } else {
                    android.util.Log.e("PenSettings", "Invalid arguments - Color: $color")
                    result.error("INVALID_ARGUMENTS", "Color is null", null)
                }
            }
            "updatePenWidth" -> {
                val width = call.argument<Double>("width")
                // android.util.Log.d("PenSettings", "Received method call - Color: $color, Width: $width")
                if (width != null) {
                    view.updatePenWidth(width.toFloat())
                    result.success(null)
                } else {
                    // android.util.Log.e("PenSettings", "Invalid arguments - Width: $width")
                    result.error("INVALID_ARGUMENTS", "Width is null", null)
                }
            }
            "setDashed" -> {
                val isDashed = call.argument<Boolean>("dashed") ?: false
                view.setDashed(isDashed)
                result.success(null)
            }
            "setDoublePenModeEnabled" -> {
                val isDoublePenModeEnabled = call.argument<Boolean>("doublePenModeEnabled") ?: false
                view.setDoublePenModeEnabled(isDoublePenModeEnabled)
                result.success(null)
            }
            "setDoublePenColor1" -> {
                val color = (call.argument<Number>("doublePenColor1"))?.toInt()

                if (color != null) {
                    view.setDoublePenColor1(color)
                    result.success(null)
                } else {
                    result.error("INVALID_ARGUMENTS", "Color is null", null)
                }
            }
            "setDoublePenColor2" -> {
                val color = (call.argument<Number>("doublePenColor2"))?.toInt()

                if (color != null) {
                    view.setDoublePenColor2(color)
                    result.success(null)
                } else {
                    result.error("INVALID_ARGUMENTS", "Color is null", null)
                }
            }
            "setFingerAsEraserEnabled" -> {
                val isFingerAsEraserEnabled = call.argument<Boolean>("fingerAsEraserEnabled") ?: false
                view.setFingerAsEraserEnabled(isFingerAsEraserEnabled)
                result.success(null)
            }
            "setFistAsEraserEnabled" -> {
                val isFistAsEraserEnabled = call.argument<Boolean>("fistAsEraserEnabled") ?: false
                view.setFistAsEraserEnabled(isFistAsEraserEnabled)
                result.success(null)
            }
            "setFingerAsEraserThreshold" -> {
                val fingerAsEraserThreshold = call.argument<Double>("fingerAsEraserThreshold") ?: 5.0
                view.setFingerAsEraserThreshold(fingerAsEraserThreshold.toFloat())
                result.success(null)
            }
            "setFistAsEraserThreshold" -> {
                val fistAsEraserThreshold = call.argument<Double>("fistAsEraserThreshold") ?: 5.0
                view.setFistAsEraserThreshold(fistAsEraserThreshold.toFloat())
                result.success(null)
            }
            "setDoublePenThreshold" -> {
                val doublePenThreshold = call.argument<Double>("doublePenThreshold") ?: 5.0
                view.setDoublePenThreshold(doublePenThreshold.toFloat())
                result.success(null)
            }
            "clear" -> {
                view.clearCanvas()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }
}