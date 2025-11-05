package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminSettingActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_setting);

        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ManageProductsActivity.class));
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, ManageUsersActivity.class));
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, ManageCategoriesActivity.class));
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, ManageOrdersActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                return true; // Đã ở trang Cài đặt rồi
            }
            return false;
        });
    }
}
