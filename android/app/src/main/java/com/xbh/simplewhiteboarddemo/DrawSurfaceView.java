package com.xbh.simplewhiteboarddemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewTreeObserver;
import io.flutter.plugin.common.MethodChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.PointF;

import com.xbh.whiteboard.AccelerateDraw;

public class DrawSurfaceView extends TextureView implements TextureView.SurfaceTextureListener {
    private final String TAG = DrawSurfaceView.class.getSimpleName();
    private final Handler handler = new Handler();
    private ViewTreeObserver.OnGlobalLayoutListener mPreDrawListener;

    private Surface mSurface = null;
    private Paint mPaint = null;
    private Rect mScreenRect = null;
    private Bitmap mCacheBitmap = null;//成熟区，保存的是已经绘制的笔迹
    private Bitmap mDrawBitmap = null;//刷新区，保存的是书写过程传给加速库的笔迹
    private Canvas mCacheCanvas = null;
    private Canvas mDrawCanvas = null;//书写画布

    private SparseArray<IDrawer> mPencilList = new SparseArray<>();
    private static final Object PEN_LOCKER = new Object();
    private AccelerateDraw mAcd = AccelerateDraw.getInstance();

    private Rect mViewRect = new Rect();
    private MethodChannel methodChannel;
    private List<PointF> currentStrokePoints = new ArrayList<>();
    private boolean isDashed = false;
    private static final float DASH_LENGTH = 30f;
    private static final float GAP_LENGTH = 20f;
    private static final int defaultHighlighterAlpha = 75;

    // TODO: for double pen, finger as eraser, fist as eraser mode
    private float doublePenThreshold;
    private float fingerAsEraserThreshold;
    private float fistAsEraserThreshold;

    private boolean doublePenEnabled;
    private boolean fingerAsEraserEnabled;
    private boolean fistAsEraserEnabled;

    private int pen1Color;
    private int pen2Color;
    private int lastColor;

    private boolean isErasing = false;
    private Map<Integer, List<PointF>> mStrokePointsMap = new HashMap<>();
    private Map<Integer, Float> mLastXMap = new HashMap<>();
    private Map<Integer, Float> mLastYMap = new HashMap<>();

    public DrawSurfaceView(Context context) {
        super(context);
        // Use default values for initialization
        init(context, Color.BLACK, 5.0f, 0.5f, 0.3f, 0.4f, false, false, false, Color.BLACK, Color.BLUE);
    }

    public DrawSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Use default values for initialization
        init(context, Color.BLACK, 5.0f, 0.5f, 0.3f, 0.4f, false, false, false, Color.BLACK, Color.BLUE);
    }

    public DrawSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Use default values for initialization
        init(context, Color.BLACK, 5.0f, 0.5f, 0.3f, 0.4f, false, false, false, Color.BLACK, Color.BLUE);
    }

    public DrawSurfaceView(Context context, int initialColor, float initialWidth, float doublePenThreshold, float fingerAsEraserThreshold, float fistAsEraserThreshold, boolean fingerAsEraserEnabled, boolean fistAsEraserEnabled, boolean doublePenEnabled, int pen1Color, int pen2Color  ) {
        super(context);
        init(context, initialColor, initialWidth, doublePenThreshold, fingerAsEraserThreshold, fistAsEraserThreshold, fingerAsEraserEnabled, fistAsEraserEnabled, doublePenEnabled, pen1Color, pen2Color);
    }

    private void init(Context context, int initialColor, float initialWidth, float doublePenThreshold, float fingerAsEraserThreshold, float fistAsEraserThreshold, boolean fingerAsEraserEnabled, boolean fistAsEraserEnabled, boolean doublePenEnabled, int pen1Color, int pen2Color) {
        Log.d(TAG, "DrawSurfaceView");
        setSurfaceTextureListener(this);
        setOpaque(false);
        mScreenRect = new Rect(0, 0, Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT);

        // Create cache bitmap
        mCacheBitmap = Bitmap.createBitmap(Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
        mCacheBitmap.eraseColor(Color.TRANSPARENT);
        mCacheCanvas = new Canvas(mCacheBitmap);

        // Create drawing bitmap
        mDrawBitmap = Bitmap.createBitmap(Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
        mDrawBitmap.eraseColor(Color.TRANSPARENT);
        mDrawCanvas = new Canvas(mDrawBitmap);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(initialColor);
        lastColor = initialColor;
        float density = getResources().getDisplayMetrics().density;
        mPaint.setStrokeWidth(initialWidth * density);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setPathEffect(new android.graphics.CornerPathEffect(40f));

        this.doublePenEnabled = doublePenEnabled;
        this.fistAsEraserEnabled = fistAsEraserEnabled;
        this.fingerAsEraserEnabled = fingerAsEraserEnabled;
        this.doublePenThreshold = doublePenThreshold;
        this.fingerAsEraserThreshold = fingerAsEraserThreshold;
        this.fistAsEraserThreshold = fistAsEraserThreshold;
        this.pen1Color = pen1Color;
        this.pen2Color = pen2Color;

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                onPreDraw();
            }
        };

        mPreDrawListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 100);
            }
        };

        // Add the layout listener
        getViewTreeObserver().addOnGlobalLayoutListener(mPreDrawListener);
    }

    private void onPreDraw() {
        boolean update = updateViewRect();
        if (update) {
            mAcd.stopAndClearAccelerate();
            requestCacheDraw();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(mPreDrawListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Remove the layout listener when the view is detached
        if (mPreDrawListener != null) {
            getViewTreeObserver().removeOnGlobalLayoutListener(mPreDrawListener);
            mPreDrawListener = null;
        }
        // Clean up handler
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        mSurface = new Surface(surface);
        AccelerateDraw.getInstance().accelerateDeInit();
        AccelerateDraw.getInstance().accelerateInit(Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT);
        AccelerateDraw.getInstance().stopAndClearAccelerate();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged width = " + width + " height=" + height);
        Util.OVERRIDE_SCREEN_WIDTH = width;
        Util.OVERRIDE_SCREEN_HEIGHT = height;
        updateViewRect();
        requestCacheDraw();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        mAcd.stopAndClearAccelerate();
        mAcd.accelerateDeInit();
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // This method is called when the surface texture is updated
        // Usually we don't need to do anything here for drawing applications
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                MotionEventTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                MotionEventTouchMove(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                MotionEventTouchUp(event, false);
                break;
            case MotionEvent.ACTION_UP:
                MotionEventTouchUp(event, true);
                break;
            default:
                break;
        }
        return true;
    }

    private void MotionEventTouchDown(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        int maskedAction = event.getActionMasked();

        Log.d(TAG, "event.getSize()" + event.getSize());
        Log.d(TAG, "fingerAsEraserThreshold" + fingerAsEraserThreshold);
        Log.d(TAG, "fingerAsEraserEnabled" + fingerAsEraserEnabled);

        if(((event.getSize() > fingerAsEraserThreshold) && fingerAsEraserEnabled) || ((event.getSize() > fistAsEraserThreshold) && fistAsEraserEnabled)) {
            Log.d(TAG, "event.getSize()" + event.getSize());
            Log.d(TAG, "fingerAsEraserThreshold" + fingerAsEraserThreshold);
            Log.d(TAG, "fingerAsEraserEnabled" + fingerAsEraserEnabled);

            isErasing = true;
            Log.d(TAG, "ErasingModeOnAndroid" + isErasing);
            clearCanvas();
        }

        if(doublePenEnabled){
            if(event.getSize()>doublePenThreshold){
                mPaint.setColor(pen2Color);
            } else {
                mPaint.setColor(pen1Color);
            }
        } else {
            //mPaint.setColor(lastColor);
        }

        float startX = event.getX(actionIndex);
        float startY = event.getY(actionIndex);
        touchDown(pointerId, startX, startY);

        List<PointF> points = new ArrayList<>();
        points.add(new PointF(startX, startY));
        mStrokePointsMap.put(pointerId, points);

        mLastXMap.put(pointerId, startX);
        mLastYMap.put(pointerId, startY);
    }

    private void touchDown(int curPointerId, float x, float y) {
        IDrawer drawer = toCreateDrawer(curPointerId);
        mAcd.startAccelerateDraw();
        drawer.touchDown(x, y);
    }

    private void MotionEventTouchMove(MotionEvent event) {
        if(isErasing) {
            return;
        }

        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int id = event.getPointerId(i);
            float x = event.getX(i);
            float y = event.getY(i);

            List<PointF> currentPoints = mStrokePointsMap.get(id);

            paintDraw(event.getPointerId(i), event.getX(i), event.getY(i));

            currentPoints.add(new PointF(x, y));
            mLastXMap.put(id, event.getX(i));
            mLastYMap.put(id, event.getY(i));
        }
    }

    private void paintDraw(int curPointerId, float x, float y) {
        IDrawer drawer = getDrawer(curPointerId);
        if (drawer != null && drawer.touchMove(x, y)) {
            drawer.draw(this);
        }
    }

    private void MotionEventTouchUp(MotionEvent event, boolean releaseAll) {
        if(isErasing) {
            isErasing = false;
            return;
        }
        int curPointerIndex = event.getActionIndex();
        touchUp(event.getPointerId(curPointerIndex), event.getX(curPointerIndex),
                event.getY(curPointerIndex), releaseAll);
    }

    private void touchUp(int curPointerId, float x, float y, boolean releaseAll) {
        synchronized (PEN_LOCKER) {
            IDrawer drawer = getDrawer(curPointerId);
            if (drawer != null && methodChannel != null) {
                // Get the points from the drawer
                List<PointF> points = mStrokePointsMap.get(curPointerId); // You'll need to add this method to IDrawer
                Map<String, Object> strokeData = new HashMap<>();
                List<Map<String, Double>> pointsList = new ArrayList<>();
                float density = getResources().getDisplayMetrics().density;

                for (PointF point : points) {
                    Map<String, Double> pointMap = new HashMap<>();
                    pointMap.put("x", (double) (point.x / density));
                    pointMap.put("y", (double) (point.y / density));
                    pointsList.add(pointMap);
                }

                strokeData.put("points", pointsList);
                strokeData.put("color", mPaint.getColor());
                strokeData.put("width", (double) (mPaint.getStrokeWidth() / density));

                methodChannel.invokeMethod("onStrokeComplete", strokeData);
            }

            paintUp(curPointerId, x, y);
            if (releaseAll) {
//               clearAllDrawer();
                requestCacheDraw();
                // mAcd.stopAndClearAccelerate();
            }
        }
    }

    private void paintUp(int curPointerId, float x, float y) {
        IDrawer drawer = getDrawer(curPointerId);
        if (drawer != null) {
            drawer.touchUp(x, y, this);
            mPencilList.remove(curPointerId);
        }
    }

    public boolean updateViewRect() {
        int[] position = new int[2];
        getLocationOnScreen(position);
        int left = position[0];
        int top = position[1];
        int right = position[0] + getWidth();
        int bottom = position[1] + getHeight();
        if (left == mViewRect.left && top == mViewRect.top
                && right == mViewRect.right && bottom == mViewRect.bottom) {
            return false;
        }
        mViewRect.set(left, top, right, bottom);
        Log.d(TAG, "updateViewRect =" + mViewRect);
        return true;
    }

    public void refreshCache(Rect rect) {
        if (rect != null) {
            int left = mViewRect.left + rect.left;
            int top = mViewRect.top + rect.top;
            // Change the last parameter to true to preserve background
            mAcd.refreshAccelerateDrawV2(left, top, rect.width(), rect.height(),
                    mDrawBitmap, rect.left, rect.top, true);
        }
    }

    public void requestCacheDraw() {
        if (mSurface == null || !mSurface.isValid()) {
            return;
        }
        
        synchronized (mSurface) {
            // Use SRC_OVER for proper blending when combining bitmaps
            Paint blendPaint = new Paint();
            blendPaint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
            blendPaint.setAntiAlias(true);
            blendPaint.setFilterBitmap(true);
            
            mCacheCanvas.drawBitmap(mDrawBitmap, null, mScreenRect, blendPaint);
            mDrawBitmap.eraseColor(Color.TRANSPARENT);

            Canvas canvas = null;
            try {
                canvas = mSurface.lockCanvas(null);
                if (canvas != null) {
                    canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                    canvas.drawBitmap(mCacheBitmap, null, mScreenRect, null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error drawing to surface", e);
            } finally {
                if (canvas != null) {
                    mSurface.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private IDrawer getDrawer(int pointId) {
        return mPencilList.get(pointId);
    }

    private void clearAllDrawer() {
        mPencilList.clear();
    }

    private IDrawer toCreateDrawer(int pointId) {
        if (mPaint == null) {
            toCreatePaint();
        }
        IDrawer drawer = new Pencil(mPaint);
        mPencilList.append(pointId, drawer);
        return drawer;
    }

    private void toCreatePaint() {
        mPaint = new Paint();
        float size = 5.0f;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.GREEN);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(size);
    }

    public Canvas getDrawCanvas() {
        return mDrawCanvas;
    }

    public void setMethodChannel(MethodChannel channel) {
        this.methodChannel = channel;
    }

    public void updatePenColor(int color) {
        if (mPaint != null) {
            android.util.Log.d("PenSettings", "Received method call from Flutter - Color:" + mPaint.getColor());
            android.util.Log.d("PenSettings", "Received method call of mPaint - Color:" + mPaint.getColor());
            mPaint.setColor(color);
            android.util.Log.d("PenSettings", "Received method call after mPaint - Color:" + mPaint.getColor());
            if (Color.alpha(color) < 255) {
                // Highlighter settings
                mPaint.setStrokeCap(Paint.Cap.SQUARE);
                mPaint.setStrokeJoin(Paint.Join.ROUND);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setPathEffect(null);
                mPaint.setXfermode(new android.graphics.PorterDuffXfermode(
                        android.graphics.PorterDuff.Mode.SRC_OVER));
                mPaint.setAlpha(Color.alpha(color));
                mPaint.setAlpha(defaultHighlighterAlpha);
            } else {
                // Normal pen settings
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mPaint.setStrokeJoin(Paint.Join.ROUND);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setPathEffect(new android.graphics.CornerPathEffect(40f));
                mPaint.setXfermode(null);
                mPaint.setAlpha(255);
            }
        }
    }

    public void updatePenWidth(float width) {
        if (mPaint != null) {
            float density = getResources().getDisplayMetrics().density;
            mPaint.setStrokeWidth(width * density);
        }
    }

    public void setDashed(boolean dashed) {
        isDashed = dashed;
        if (mPaint != null) {
            if (dashed) {
                // Set up dashed effect if needed
                mPaint.setPathEffect(new android.graphics.DashPathEffect(
                        new float[]{DASH_LENGTH, GAP_LENGTH}, 0));
            } else {
                mPaint.setPathEffect(null);
            }
        }
        updatePenColor(mPaint.getColor());
    }

    public void setDoublePenColor1(int color) {
        pen1Color = color;
    }

    public void setDoublePenColor2(int color) {
        pen2Color = color;
    }

    public void setFingerAsEraserEnabled(boolean enabled) {
        fingerAsEraserEnabled = enabled;
    }

    public void setFistAsEraserEnabled(boolean enabled) {
        fistAsEraserEnabled = enabled;
    }

    public void setDoublePenThreshold(float threshold) {
        doublePenThreshold = threshold;
    }

    public void setFingerAsEraserThreshold(float threshold) {
        fingerAsEraserThreshold = threshold;
    }

    public void setFistAsEraserThreshold(float threshold) {
        fistAsEraserThreshold = threshold;
    }

    public void setDoublePenModeEnabled(boolean enabled) {
        doublePenEnabled = enabled;
    }

    public void clearCanvas() {
        // Clear the canvas
        mCacheBitmap.eraseColor(Color.TRANSPARENT);
        mDrawBitmap.eraseColor(Color.TRANSPARENT);
        requestCacheDraw();
        mAcd.stopAndClearAccelerate();
    }
}