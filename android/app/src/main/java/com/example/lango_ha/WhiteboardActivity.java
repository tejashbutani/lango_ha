package com.example.lango_ha;

import android.app.Activity;
import android.os.Bundle;
import com.xbh.simplewhiteboarddemo.DrawSurfaceView;

public class WhiteboardActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrawSurfaceView drawSurfaceView = new DrawSurfaceView(this);
        setContentView(drawSurfaceView);
    }
} 