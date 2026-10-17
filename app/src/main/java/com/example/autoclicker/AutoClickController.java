package com.example.autoclicker;

import android.content.Context;
import android.content.SharedPreferences;

public final class AutoClickController {
    private static final String PREFS_NAME = "auto_click_config";
    private static final String KEY_DELAY = "delay_ms";
    private static final String KEY_TAP_X = "tap_x";
    private static final String KEY_TAP_Y = "tap_y";
    private static final String KEY_TAP_COUNT = "tap_count";
    private static final String KEY_RANDOMIZE = "randomize";
    private static final String KEY_ENABLED = "enabled";

    private AutoClickController() {
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveConfig(Context context, int delayMs, int tapX, int tapY, int tapCount, boolean randomize) {
        getPrefs(context).edit()
                .putInt(KEY_DELAY, delayMs)
                .putInt(KEY_TAP_X, tapX)
                .putInt(KEY_TAP_Y, tapY)
                .putInt(KEY_TAP_COUNT, tapCount)
                .putBoolean(KEY_RANDOMIZE, randomize)
                .apply();
    }

    public static int getDelay(Context context) {
        return getPrefs(context).getInt(KEY_DELAY, 1000);
    }

    public static int getTapX(Context context) {
        return getPrefs(context).getInt(KEY_TAP_X, 540);
    }

    public static int getTapY(Context context) {
        return getPrefs(context).getInt(KEY_TAP_Y, 980);
    }

    public static int getTapCount(Context context) {
        return getPrefs(context).getInt(KEY_TAP_COUNT, 0);
    }

    public static boolean shouldRandomize(Context context) {
        return getPrefs(context).getBoolean(KEY_RANDOMIZE, false);
    }

    public static boolean isEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
}
