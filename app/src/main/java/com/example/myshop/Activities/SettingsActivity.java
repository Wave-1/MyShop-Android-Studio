package com.example.myshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private LinearLayout itemAccountInfo, itemPaymentSettings, itemSecurity;
    private LinearLayout itemNotifications, itemDarkMode, itemLanguage;
    private LinearLayout itemPolicy, itemFeedback;
    private MaterialSwitch switchNotification, switchDarkMode;

    private static final String PREF_NAME = "MyShopSettings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATION = "notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);

        itemAccountInfo = findViewById(R.id.itemAccountInfo);
        itemPaymentSettings = findViewById(R.id.itemPaymentSettings);
        itemSecurity = findViewById(R.id.itemSecurity);

        itemNotifications = findViewById(R.id.itemNotifications);
        itemDarkMode = findViewById(R.id.itemDarkMode);
        itemLanguage = findViewById(R.id.itemLanguage);
        switchNotification = findViewById(R.id.switchNotification);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        itemPolicy = findViewById(R.id.itemPolicy);
        itemFeedback = findViewById(R.id.itemFeedback);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        setupEvents();
        loadSettings();
    }

    private void setupEvents() {
        itemAccountInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        itemPaymentSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Quản lý thanh toán", Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBooleanSetting(KEY_DARK_MODE, isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            }
        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBooleanSetting(KEY_NOTIFICATION, isChecked);
//            if (isChecked) {
//                Toast.makeText(this, "Đã bật thông báo", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
//            }
        });
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        boolean isNotifications = sharedPreferences.getBoolean(KEY_NOTIFICATION, true);

        switchDarkMode.setChecked(isDarkMode);
        switchNotification.setChecked(isNotifications);
    }


    private void saveBooleanSetting(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
