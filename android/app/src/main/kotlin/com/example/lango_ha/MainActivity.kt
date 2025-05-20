package com.example.lango_ha

import android.content.Intent
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val TAG = "FlutterActivity"
    private val CHANNEL = "com.example.lango_ha/native"

    /// Flutter Platform Channel Code
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "launchWhiteboard") {
                val intent = Intent(this, WhiteboardActivity::class.java)
                startActivity(intent)
                result.success(null)
            } else {
                result.notImplemented()
            }
        }
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory("custom_canvas_view", ActivityMainViewFactory())
    }
}
