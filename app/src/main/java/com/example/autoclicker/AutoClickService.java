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

import java.util.Random;

public class AutoClickService extends AccessibilityService {
    private static final String TAG = "AutoClickService";
    private static final String ACTION_START = "com.example.autoclicker.START_AUTO_CLICK";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tapRunnable;
    private int tapCountRemaining = 0;
    private final Random random = new Random();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not required for this simple clicker.
    }

    @Override
    public void onInterrupt() {
        stopLoop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLoop();
        return START_STICKY;
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

        tapCountRemaining = AutoClickController.getTapCount(this);
        if (tapCountRemaining < 0) {
            tapCountRemaining = 0;
        }

        tapRunnable = new Runnable() {
            @Override
            public void run() {
                if (!AutoClickController.isEnabled(AutoClickService.this)) {
                    return;
                }

                if (tapCountRemaining > 0) {
                    tapCountRemaining--;
                    if (tapCountRemaining <= 0) {
                        AutoClickController.setEnabled(AutoClickService.this, false);
                        return;
                    }
                }

                int baseX = AutoClickController.getTapX(AutoClickService.this);
                int baseY = AutoClickController.getTapY(AutoClickService.this);

                if (AutoClickController.shouldRandomize(AutoClickService.this)) {
                    int jitterX = random.nextInt(35) - 17;
                    int jitterY = random.nextInt(35) - 17;
                    baseX = Math.max(0, baseX + jitterX);
                    baseY = Math.max(0, baseY + jitterY);
                }

                dispatchTap(baseX, baseY);
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
