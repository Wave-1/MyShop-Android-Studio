package com.example.myshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myshop.Adapters.ChannelAdapter;
import com.example.myshop.Models.ChannelModel;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {

    Button btnLogin, btnRegister;
    GridView gridChannels;
    ArrayList<ChannelModel> channelList;
    ChannelAdapter adapter;
    ImageButton btnSettings;
    ImageView imgAvatar;
    TextView tvUsername;
    LinearLayout layoutGuest, layoutUser, layoutPendingOrders;
    BottomNavigationView bottomNav;

    private static final int REQUEST_LOGIN = 100;
    private static final int REQUEST_REGISTER = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        gridChannels = findViewById(R.id.gridChannels);
        btnSettings = findViewById(R.id.btnSettings);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        layoutGuest = findViewById(R.id.layoutGuest);
        layoutUser = findViewById(R.id.layoutUser);
        layoutPendingOrders = findViewById(R.id.layoutPendingOrders);
        bottomNav = findViewById(R.id.bottomNav);
        layoutGuest.setVisibility(View.VISIBLE);
        layoutUser.setVisibility(View.GONE);

        // Né tai thỏ
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayoutAc), (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBarInsets.top, 0, 0);
            return insets;
        });

        // Kiểm tra SharedPreferences (nếu đã đăng nhập trước đó)
        SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", null);
        if (savedEmail != null) {
            layoutGuest.setVisibility(View.GONE);
            layoutUser.setVisibility(View.VISIBLE);
            tvUsername.setText("Xin chào, " + savedEmail);
            imgAvatar.setImageResource(R.drawable.ic_account);
        }

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REQUEST_REGISTER);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        layoutPendingOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            startActivity(intent);
        });

        // Danh sách kênh mẫu
        channelList = new ArrayList<>();
        channelList.add(new ChannelModel(R.drawable.ic_lazmall, "LazMall Sale"));
        channelList.add(new ChannelModel(R.drawable.ic_coupon, "Tiết kiệm"));
        channelList.add(new ChannelModel(R.drawable.ic_affiliate, "Affiliates"));
        channelList.add(new ChannelModel(R.drawable.ic_live, "LazLive"));
        channelList.add(new ChannelModel(R.drawable.ic_beauty, "LazMall"));
        channelList.add(new ChannelModel(R.drawable.ic_partner, "Đối Tác"));

        adapter = new ChannelAdapter(this, channelList);
        gridChannels.setAdapter(adapter);
        setGridViewHeightBasedOnChildren(gridChannels, 3);

        // ✅ Gắn sự kiện Bottom Navigation
        bottomNav.setSelectedItemId(R.id.nav_account); // Chọn tab Home mặc định
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(AccountActivity.this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(AccountActivity.this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        if (requestCode == REQUEST_REGISTER && resultCode == RESULT_OK) {
            // Nhận email từ RegisterActivity → mở LoginActivity
            String email = data.getStringExtra("email");
            if (email != null) {
                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                intent.putExtra("email", email); // tự điền email
                startActivityForResult(intent, REQUEST_LOGIN);
            }
        }

        if (requestCode == REQUEST_LOGIN && resultCode == RESULT_OK) {
            String username = data.getStringExtra("username");
            if (username != null) {
                // Lưu email vào SharedPreferences
                SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", username);
                editor.apply();

                layoutGuest.setVisibility(View.GONE);
                layoutUser.setVisibility(View.VISIBLE);
                tvUsername.setText("Xin chào, " + username);
                imgAvatar.setImageResource(R.drawable.ic_account);
            }
        }
    }

    public static void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        android.widget.ListAdapter adapter = gridView.getAdapter();
        if (adapter == null) return;
        int totalHeight = 0;
        int items = adapter.getCount();
        int rows = (int) Math.ceil((double) items / columns);

        for (int i = 0; i < rows; i++) {
            View listItem = adapter.getView(i, null, gridView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        android.view.ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight + (gridView.getVerticalSpacing() * (rows - 1));
        gridView.setLayoutParams(params);
    }
}
