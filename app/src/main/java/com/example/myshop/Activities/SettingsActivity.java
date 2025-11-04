package com.example.myshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout itemAccountInfo;
    TextView tvLogout;
    ImageView imgToolbarBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        itemAccountInfo = findViewById(R.id.itemAccountInfo);
        tvLogout = findViewById(R.id.itemLogout);
        imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);

        tvToolbarTitle.setText(getString(R.string.btn_settings));

        // Khi nhấn nút quay lại (mũi tên trên thanh toolbar)
        imgToolbarBack.setOnClickListener(v -> {
            // Quay về trang AccountActivity
            Intent intent = new Intent(SettingsActivity.this, AccountActivity.class);
            startActivity(intent);
            finish(); // đóng SettingsActivity
        });

        // Khi bấm "Thông tin tài khoản" → mở ProfileActivity
        itemAccountInfo.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Khi bấm "Đăng xuất"
        tvLogout.setOnClickListener(v -> {
            // Đăng xuất khỏi Firebase
            FirebaseAuth.getInstance().signOut();

            // Xóa thông tin đăng nhập trong SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Quay lại màn hình đăng nhập
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
