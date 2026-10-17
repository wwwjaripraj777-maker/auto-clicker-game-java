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
        // Preferred robust check using Settings.Secure ENABLED_ACCESSIBILITY_SERVICES
        try {
            String enabledServices = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabledServices != null) {
                String fullName = context.getPackageName() + "/" + AutoClickService.class.getName();
                String shortName = context.getPackageName() + "/.AutoClickService";
                if (enabledServices.contains(fullName) || enabledServices.contains(shortName)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // Fallback to AccessibilityManager-based check
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
        }

        return false;
    }

    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        // If called from a non-activity context, ensure this starts a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
