package com.example.autoclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AutoClickService extends AccessibilityService {
    private static final String TAG = "AutoClickService";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tapRunnable;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not required for this simple clicker.
    }

    @Override
    public void onInterrupt() {
        stopLoop();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");
        startLoop();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopLoop();
        return super.onUnbind(intent);
    }

    private void startLoop() {
        stopLoop();
        if (!AutoClickController.isEnabled(this)) {
            return;
        }

        tapRunnable = new Runnable() {
            @Override
            public void run() {
                if (!AutoClickController.isEnabled(AutoClickService.this)) {
                    return;
                }

                dispatchTap(AutoClickController.getTapX(AutoClickService.this),
                        AutoClickController.getTapY(AutoClickService.this));
                handler.postDelayed(this, Math.max(50, AutoClickController.getDelay(AutoClickService.this)));
            }
        };

        handler.postDelayed(tapRunnable, Math.max(50, AutoClickController.getDelay(this)));
    }

    private void stopLoop() {
        if (tapRunnable != null) {
            handler.removeCallbacks(tapRunnable);
            tapRunnable = null;
        }
    }

    private void dispatchTap(int x, int y) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }

        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(
                path, 0, 1);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke)
                .build();

        boolean result = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);

        if (!result) {
            Log.w(TAG, "Gesture not dispatched for tap: x=" + x + ", y=" + y);
        }
    }
}
