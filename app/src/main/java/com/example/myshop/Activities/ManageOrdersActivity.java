package com.example.myshop.Activities;

import android.app.AlertDialog;
import android.app.ComponentCaller;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.OrderAdapter;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private TextView tvNoOrders;
    private BottomNavigationView bottomNav;

    private Button btnAll, btnProcessing, btnShipping, btnCompleted, btnCancelled;
    private Button selectedButton;
    private List<Button> filterButtons = new ArrayList<>();
    private FirebaseFirestore db;
    private ArrayList<OrderModel> orderList;
    private OrderAdapter orderAdapter;
    private String currentStatus = "Tất cả";
    private static final int UPDATE_ORDER_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();

        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressBar = findViewById(R.id.progressBar);
        tvNoOrders = findViewById(R.id.tvNoOrders);
        bottomNav = findViewById(R.id.bottomNav);

        btnAll = findViewById(R.id.btnAll);
        btnProcessing = findViewById(R.id.btnProcessing);
        btnShipping = findViewById(R.id.btnShipping);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnCancelled = findViewById(R.id.btnCancelled);

//        View rootLayout = findViewById(R.id.rootLayout);
//
//        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
//            // Lấy khoảng trống của các thanh hệ thống (status bar, navigation bar)
//            int systemBarsInsetsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
//            int systemBarsInsetsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
//
//            // Áp dụng padding cho layout gốc
//            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);
//
//            // Trả về insets mặc định để các View con (như AppBarLayout) có thể tự xử lý
//            return WindowInsetsCompat.CONSUMED;
//        });
        applyBottomNavigationPadding();
        setupRecyclerView();
        setupFilterButtons();
        setupBottomNavigation();
        selectButton(btnAll);

        loadOrders(currentStatus);
    }

    private void applyBottomNavigationPadding() {
        bottomNav = findViewById(R.id.bottomNav);
        recyclerOrders = findViewById(R.id.recyclerOrders);

        if (bottomNav == null || recyclerOrders == null) {
            return;
        }

        bottomNav.post(() -> {
            int navHeight = bottomNav.getHeight();

            int paddingLeft = recyclerOrders.getPaddingLeft();
            int paddingTop = recyclerOrders.getPaddingTop();
            int paddingRight = recyclerOrders.getPaddingRight();

            recyclerOrders.setPadding(paddingLeft, paddingTop, paddingRight, navHeight);
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_orders);
        // --- Bottom Navigation ---
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
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    // Bộ lọc theo trạng thái
    private void setupFilterButtons() {
        filterButtons.add(btnAll);
        filterButtons.add(btnProcessing);
        filterButtons.add(btnShipping);
        filterButtons.add(btnCompleted);
        filterButtons.add(btnCancelled);

        btnAll.setOnClickListener(v -> {
            selectButton(btnAll);
            currentStatus = "Tất cả";
            loadOrders(currentStatus);
        });

        btnProcessing.setOnClickListener(v -> {
            selectButton(btnProcessing);
            currentStatus = "Đang xử lý";
            loadOrders(currentStatus);
        });

        btnShipping.setOnClickListener(v -> {
            selectButton(btnShipping);
            currentStatus = "Chờ giao hàng";
            loadOrders(currentStatus);
        });

        btnCompleted.setOnClickListener(v -> {
            selectButton(btnCompleted);
            currentStatus = "Đã giao";
            loadOrders(currentStatus);
        });

        btnCancelled.setOnClickListener(v -> {
            selectButton(btnCancelled);
            currentStatus = "Đã hủy";
            loadOrders(currentStatus);
        });

    }

    private void selectButton(Button btnToSelect) {
        if (selectedButton != null) {
            selectedButton.setSelected(false);
        }
        btnToSelect.setSelected(true);
        selectedButton = btnToSelect;
    }

    private void setupRecyclerView() {
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this,
                orderList,
                order -> {
                    Intent intent = new Intent(this, AdminOrderDetailActivity.class);
                    intent.putExtra("ORDER_OBJECT", order);
                    startActivityForResult(intent, UPDATE_ORDER_REQUEST_CODE);
                }, null
        );
        recyclerOrders.setAdapter(orderAdapter);
    }

    private void loadOrders(String status) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
        tvNoOrders.setVisibility(View.GONE);

        Query query = db.collectionGroup("orders");
        if (!"Tất cả".equals(status)) {
            query = query.whereEqualTo("status", status);
        }
        query = query.orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            orderList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                try {
                    OrderModel order = doc.toObject(OrderModel.class);
                    order.setOrderId(doc.getId());
                    orderList.add(order);
                } catch (Exception e) {
                    Log.e("FirestoreDeserialize", "Lỗi chuyển đổi đơn hàng: " + doc.getId() + ". Nguyên nhân: " + e.getMessage());
                }
//                OrderModel order = doc.toObject(OrderModel.class);
//                String oderId = doc.getId();
//                order.setOrderId(oderId);
//                orderList.add(order);
            }
            progressBar.setVisibility(View.GONE);
            if (orderList.isEmpty()) {
                tvNoOrders.setVisibility(View.VISIBLE);
                recyclerOrders.setVisibility(View.GONE);
            } else {
                tvNoOrders.setVisibility(View.GONE);
                recyclerOrders.setVisibility(View.VISIBLE);
            }
            orderAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Log.e("loadOrders", "Lỗi tải đơn hàng: " + e.getMessage());
        });
    }

    private void showChangeStatusDialog(OrderModel order) {
        if (order.getUserId() == null || order.getOrderId() == null) {
            Toast.makeText(this, "Lỗi đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] statuses = {"Đang xử lý", "Chờ giao hàng", "Đã giao", "Đã hủy"};

        new AlertDialog.Builder(this).setTitle("Cập nhật trạng thái đơn hàng").setItems(statuses, (dialog, which) -> {
            String newStatus = statuses[which];

            db.collection("users").document(order.getUserId()).collection("orders").document(order.getOrderId()).update("status", newStatus).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                order.setStatus(newStatus);
                orderAdapter.notifyDataSetChanged();
                loadOrders(currentStatus);
            }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
        }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);
        if (requestCode == UPDATE_ORDER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Đang làm mới danh sách đơn hàng...", Toast.LENGTH_SHORT).show();
                loadOrders(currentStatus);
            }
        }
    }
}