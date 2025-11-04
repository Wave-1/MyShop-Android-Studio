package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myshop.Constants;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private LinearLayout layoutUser, layoutGuest;
    private ShapeableImageView imgAvatar;
    private TextView tvUsername, tvEditProfile;
    private MaterialButton btnLogin, btnLogout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private static final int REQUEST_LOGIN = 100;
    private static final int REQUEST_REGISTER = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        bottomNav = findViewById(R.id.bottomNav);
        layoutUser = findViewById(R.id.layoutUser);
        layoutGuest = findViewById(R.id.layoutGuest);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogout = findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        View rootLayout = findViewById(R.id.rootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            // Lấy khoảng trống của các thanh hệ thống (status bar, navigation bar)
            int systemBarsInsetsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Áp dụng padding cho layout gốc
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            // Trả về insets mặc định để các View con (như AppBarLayout) có thể tự xử lý
            return WindowInsetsCompat.CONSUMED;
        });

        updateUI();
        setupClickListeners();
        setupBottomNavigation();

    }

    private void setupBottomNavigation() {
        //  Gắn sự kiện Bottom Navigation
        bottomNav.setSelectedItemId(R.id.nav_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.nav_home) {
                startActivity(new Intent(AccountActivity.this, HomeActivity.class));
//                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right);
                return true;
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(AccountActivity.this, ProductActivity.class));
//                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right);
                return true;
            }
            if (intent != null){
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_account) {
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        tvEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(AccountActivity.this, EditProfileActivity.class));
        });

        findViewById(R.id.layoutViewAllOrders).setOnClickListener(v -> {
            Toast.makeText(this, "Xem tất cả đơn hàng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AccountActivity.this, OrderTrackingActivity.class));
        });

        // Đang xử lý
        findViewById(R.id.status_processing).setOnClickListener(v -> {
            openOrderTrackingActivity(Constants.ORDER_STATUS_PROCESSING);

        });

        // Chờ giao hàng
        findViewById(R.id.status_shipping).setOnClickListener(v -> {
            openOrderTrackingActivity(Constants.ORDER_STATUS_SHIPPING);

        });

        // Đã giao
        findViewById(R.id.status_completed).setOnClickListener(v -> {
            openOrderTrackingActivity(Constants.ORDER_STATUS_COMPLETED);

        });

        // Đánh giá
        findViewById(R.id.reviews).setOnClickListener(v -> {
//            openOrderTrackingActivity("Đang xử lý");
            Toast.makeText(this, "Đánh giá", Toast.LENGTH_SHORT).show();
        });


        findViewById(R.id.option_address).setOnClickListener(v -> {
            Toast.makeText(this, "Mở sổ địa chỉ", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.option_support).setOnClickListener(v -> {
            Toast.makeText(this, "Mở trung tâm hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.option_settings).setOnClickListener(v -> {
            Toast.makeText(this, "Mở cài đặt", Toast.LENGTH_SHORT).show();
        });
    }

    private void openOrderTrackingActivity(String status) {
        Intent intent = new Intent(this, OrderTrackingActivity.class);
        intent.putExtra(Constants.INTENT_KEY_ORDER_STATUS, status);
        startActivity(intent);
    }

    private void updateUI() {
        if (currentUser != null) {
            layoutUser.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            layoutGuest.setVisibility(View.GONE);
            loadUserProfile();
        } else {
            layoutUser.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            layoutGuest.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserProfile() {
        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .collection("addresses")
                .whereEqualTo("default", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String nameFromAddress = null;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy document địa chỉ đầu tiên tìm thấy
                        nameFromAddress = queryDocumentSnapshots.getDocuments().get(0).getString("name");
                    }
                    if (nameFromAddress != null && !nameFromAddress.isEmpty()) {
                        tvUsername.setText(nameFromAddress);
                    } else {
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            tvUsername.setText(displayName);
                        } else {
                            tvUsername.setText(currentUser.getEmail());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Xảy ra lỗi khi truy vấn Firestore
                    Toast.makeText(AccountActivity.this, "Lỗi tải thông tin cá nhân: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Hiển thị thông tin dự phòng
                    tvUsername.setText(currentUser.getEmail());
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_account);
    }
}
