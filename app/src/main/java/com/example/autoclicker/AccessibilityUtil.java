package com.example.autoclicker;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public final class AccessibilityUtil {
    private AccessibilityUtil() {
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) {
            return false;
        }

        List<android.accessibilityservice.AccessibilityServiceInfo> enabledServices =
                am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC);

        String targetService = context.getPackageName() + "/.AutoClickService";
        for (android.accessibilityservice.AccessibilityServiceInfo info : enabledServices) {
            if (targetService.equals(info.getId())) {
                return true;
            }
        }
        return false;
    }

    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(intent);
    }
}
