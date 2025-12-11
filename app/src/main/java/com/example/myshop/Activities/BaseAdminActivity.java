package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myshop.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseAdminActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Kiểm tra nếu Drawer đang mở thì đóng nó lại
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Nếu Drawer không mở, thực hiện hành vi Back mặc định (đóng Activity)
                    setEnabled(false); // Vô hiệu hóa callback này tạm thời
                    getOnBackPressedDispatcher().onBackPressed(); // Gọi lại nút Back hệ thống
                    setEnabled(true); // Bật lại callback (nếu Activity không bị destroy ngay)
                }
            }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        // 1. Lấy layout gốc là drawer_layout
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base_admin, null);

        // 2. Tìm FrameLayout nơi chứa nội dung
        FrameLayout contentFrame = drawerLayout.findViewById(R.id.content_frame);

        // 3. Bơm layout của trang con (layoutResID) vào FrameLayout
        getLayoutInflater().inflate(layoutResID, contentFrame, true);

        // 4. Set content view là cái drawerLayout đã bao gồm nội dung con
        super.setContentView(drawerLayout);

        // 5. Cấu hình Menu chung
        setupDrawer();
    }

    private void setupDrawer() {
        navigationView = findViewById(R.id.nav_view_admin);

        // Tìm nút Menu (btnMenu) nằm trong layout con (layoutResID)
        // Lưu ý: Các file xml con PHẢI có ImageButton id là btnMenu
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        // Xử lý click menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Kiểm tra để tránh load lại trang hiện tại
            if (id == getCurrentMenuId()) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }

            Intent intent = null;
            if (id == R.id.nav_admin_home) { // Ví dụ đây là trang Sản phẩm
                intent = new Intent(this, ManageProductsActivity.class);
            } else if (id == R.id.nav_admin_voucher) {
                startActivity(new Intent(this, ManageVouchersActivity.class));
                return true;
            } else if (id == R.id.nav_admin_categories) {
                startActivity(new Intent(this, ManageCategoriesActivity.class));
                return true;
            } else if (id == R.id.nav_admin_ranks) {
                startActivity(new Intent(this, ManageRankConfigActivity.class));
                return true;
            } else if (id == R.id.nav_admin_orders) {
                startActivity(new Intent(this, ManageOrdersActivity.class));
                return true;
            } else if (id == R.id.nav_admin_logout) {
                mAuth.signOut();
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                Intent logoutIntent = new Intent(this, LoginActivity.class);
                startActivity(logoutIntent);
                finish();
                return true;
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0); // Tắt hiệu ứng chuyển trang để mượt hơn
                finish(); // Đóng activity hiện tại để không chồng stack
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Highlight item hiện tại
        navigationView.setCheckedItem(getCurrentMenuId());
    }

    // Hàm trừu tượng bắt buộc các trang con phải khai báo ID của menu tương ứng
    protected abstract int getCurrentMenuId();

}
