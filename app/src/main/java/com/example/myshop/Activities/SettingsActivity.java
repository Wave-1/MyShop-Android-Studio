package com.example.myshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout itemAccountInfo;
    TextView tvLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        itemAccountInfo = findViewById(R.id.itemAccountInfo);
        tvLogout = findViewById(R.id.tvLogout);

        // Khi bấm "Thông tin tài khoản" → mở ProfileActivity
        itemAccountInfo.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Khi bấm "Đăng xuất"
        tvLogout.setOnClickListener(v -> {
            // Xóa thông tin đăng nhập (nếu có lưu trong SharedPreferences)
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Chuyển về màn hình Login
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // đóng SettingsActivity
        });
    }
}
