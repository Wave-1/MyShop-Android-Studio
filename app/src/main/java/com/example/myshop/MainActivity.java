package com.example.myshop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.Activities.AccountActivity;
import com.example.myshop.Activities.CartActivity;
import com.example.myshop.Activities.HomeActivity;
import com.example.myshop.Activities.LoginActivity;
import com.example.myshop.Activities.ManageProductsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String userRole = prefs.getString("userRole", "");
        // Mặc định mở HomeActivity
        if (isLoggedIn) {
            if ("admin".equalsIgnoreCase(userRole)) {
                startActivity(new Intent(this, ManageProductsActivity.class));
            } else {
                startActivity(new Intent(this, HomeActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }

            return false;
        });
    }
}
