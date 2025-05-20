package com.example.lango_ha

import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val TAG = "FlutterActivity"
    private val channelName = "PLATFORM"

    /// Flutter Platform Channel Code
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory("custom_canvas_view", CustomViewFactory(flutterEngine.dartExecutor.binaryMessenger))
    }
}
