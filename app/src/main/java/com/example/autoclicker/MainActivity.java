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
    private SwitchMaterial autoSwitch;
    private TextView stateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        delayInput = findViewById(R.id.delayInput);
        xInput = findViewById(R.id.xInput);
        yInput = findViewById(R.id.yInput);
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

            if (delay < 50) {
                delay = 50;
            }

            AutoClickController.saveConfig(this, delay, tapX, tapY);
            AutoClickController.setEnabled(this, autoSwitch.isChecked());
            updateState();
        } catch (NumberFormatException ignored) {
            delayInput.setText(String.valueOf(AutoClickController.getDelay(this)));
            xInput.setText(String.valueOf(AutoClickController.getTapX(this)));
            yInput.setText(String.valueOf(AutoClickController.getTapY(this)));
        }
    }

    private void loadSettings() {
        delayInput.setText(String.valueOf(AutoClickController.getDelay(this)));
        xInput.setText(String.valueOf(AutoClickController.getTapX(this)));
        yInput.setText(String.valueOf(AutoClickController.getTapY(this)));
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
