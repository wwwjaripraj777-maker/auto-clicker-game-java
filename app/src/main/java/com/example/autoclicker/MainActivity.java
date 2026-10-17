package com.example.autoclicker;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {
    private EditText delayInput;
    private EditText xInput;
    private EditText yInput;
    private EditText tapCountInput;
    private SwitchMaterial randomizeSwitch;
    private SwitchMaterial autoSwitch;
    private TextView stateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        delayInput = findViewById(R.id.delayInput);
        xInput = findViewById(R.id.xInput);
        yInput = findViewById(R.id.yInput);
        tapCountInput = findViewById(R.id.tapCountInput);
        randomizeSwitch = findViewById(R.id.randomizeSwitch);
        autoSwitch = findViewById(R.id.autoSwitch);
        stateText = findViewById(R.id.stateText);

        Button saveButton = findViewById(R.id.saveButton);
        Button accessibilityButton = findViewById(R.id.accessibilityButton);
        Button overlayButton = findViewById(R.id.overlayButton);

        loadSettings();

        saveButton.setOnClickListener(v -> saveSettings());
        accessibilityButton.setOnClickListener(v -> AccessibilityUtil.openAccessibilitySettings(this));
        overlayButton.setOnClickListener(v -> requestOverlayPermissionAndStart());

        autoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AutoClickController.setEnabled(MainActivity.this, isChecked);
            if (isChecked) {
                // Ensure accessibility service is enabled before attempting to start tapping
                if (!AccessibilityUtil.isAccessibilityServiceEnabled(MainActivity.this)) {
                    // Revert toggle and prompt user to enable accessibility
                    AutoClickController.setEnabled(MainActivity.this, false);
                    autoSwitch.setChecked(false);
                    AccessibilityUtil.openAccessibilitySettings(MainActivity.this);
                } else {
                    startService(new Intent(MainActivity.this, AutoClickService.class)
                            .setAction("com.example.autoclicker.START_AUTO_CLICK"));
                }
            } else {
                stopService(new Intent(MainActivity.this, AutoClickService.class));
            }
            updateState();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateState();
    }

    private void saveSettings() {
        try {
            int delay = Integer.parseInt(delayInput.getText().toString().trim());
            int tapX = Integer.parseInt(xInput.getText().toString().trim());
            int tapY = Integer.parseInt(yInput.getText().toString().trim());
            int tapCount = Integer.parseInt(tapCountInput.getText().toString().trim());

            if (delay < 50) {
                delay = 50;
            }
            if (tapCount < 0) {
                tapCount = 0;
            }

            AutoClickController.saveConfig(this, delay, tapX, tapY, tapCount, randomizeSwitch.isChecked());
            AutoClickController.setEnabled(this, autoSwitch.isChecked());
            updateState();
        } catch (NumberFormatException ignored) {
            delayInput.setText(String.valueOf(AutoClickController.getDelay(this)));
            xInput.setText(String.valueOf(AutoClickController.getTapX(this)));
            yInput.setText(String.valueOf(AutoClickController.getTapY(this)));
            tapCountInput.setText(String.valueOf(AutoClickController.getTapCount(this)));
        }
    }

    private void loadSettings() {
        delayInput.setText(String.valueOf(AutoClickController.getDelay(this)));
        xInput.setText(String.valueOf(AutoClickController.getTapX(this)));
        yInput.setText(String.valueOf(AutoClickController.getTapY(this)));
        tapCountInput.setText(String.valueOf(AutoClickController.getTapCount(this)));
        randomizeSwitch.setChecked(AutoClickController.shouldRandomize(this));
        autoSwitch.setChecked(AutoClickController.isEnabled(this));
    }

    private void requestOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        startService(new Intent(this, OverlayService.class));
    }

    private void updateState() {
        if (AccessibilityUtil.isAccessibilityServiceEnabled(this)) {
            stateText.setText(R.string.state_enabled);
            stateText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            stateText.setText(R.string.state_disabled);
            stateText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }
}
