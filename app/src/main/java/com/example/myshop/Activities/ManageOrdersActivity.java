package com.example.myshop.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.Adapters.OrderAdapter;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {

    private ListView listOrders;
    private Button btnProcessing, btnShipping, btnCompleted;

    private FirebaseFirestore db;
    private List<OrderModel> orderList;
    private List<String> orderNames;
    private OrderAdapter orderAdapter;
    private BottomNavigationView bottomNav;

    private String currentStatus = "Đang xử lý";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        db = FirebaseFirestore.getInstance();
        listOrders = findViewById(R.id.listOrders);
        btnProcessing = findViewById(R.id.btnProcessing);
        btnShipping = findViewById(R.id.btnShipping);
        btnCompleted = findViewById(R.id.btnCompleted);
        bottomNav = findViewById(R.id.bottomNav);
        orderList = new ArrayList<>();

        // Load mặc định
        loadOrders(currentStatus);

        // Bộ lọc theo trạng thái
        btnProcessing.setOnClickListener(v -> {
            currentStatus = "Đang xử lý";
            loadOrders(currentStatus);
        });

        btnShipping.setOnClickListener(v -> {
            currentStatus = "Đang giao";
            loadOrders(currentStatus);
        });

        btnCompleted.setOnClickListener(v -> {
            currentStatus = "Hoàn tất";
            loadOrders(currentStatus);
        });

        // Khi bấm vào 1 đơn hàng
        listOrders.setOnItemClickListener((parent, view, position, id) -> {
            OrderModel order = orderList.get(position);
            showChangeStatusDialog(order);
        });

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

    private void loadOrders(String status) {
        db.collectionGroup("orders")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderModel order = doc.toObject(OrderModel.class);
                        orderList.add(order);
                    }

                    if (orderList.isEmpty()) {
                        Toast.makeText(this, "Không có đơn hàng ", Toast.LENGTH_SHORT).show();
                    }

                    orderAdapter = new OrderAdapter(this, orderList);
                    listOrders.setAdapter(orderAdapter);
                })
                .addOnFailureListener(e ->
                        Log.e("loadOrders", "Lỗi tải đơn hàng: " + e.getMessage()));
//                        Toast.makeText(this, "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showChangeStatusDialog(OrderModel order) {
        String[] statuses = {"Đang xử lý", "Đang giao", "Hoàn tất"};

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật trạng thái đơn hàng")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];

                    db.collection("users")
                            .document(order.getUserId())
                            .collection("orders")
                            .document(order.getOrderId())
                            .update("status", newStatus)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                loadOrders(currentStatus); // Reload lại list hiện tại
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
                })
                .show();
    }
}