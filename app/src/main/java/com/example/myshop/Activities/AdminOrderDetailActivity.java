package com.example.myshop.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.CartAdapter;
import com.example.myshop.Models.AddressModel;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderDetailActivity extends AppCompatActivity {
    private TextView tvUserEmail, tvAddress, tvOrderStatus, tvCancellationReason;
    private RecyclerView recyclerProducts;
    private Button btnUpdateStatus;
    private MaterialToolbar toolbar;
    private FirebaseFirestore db;
    private OrderModel currentOrder;
    private CartAdapter cartAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        toolbar = findViewById(R.id.toolbar);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvAddress = findViewById(R.id.tvAddress);
        recyclerProducts = findViewById(R.id.recyclerProducts);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvCancellationReason = findViewById(R.id.tvCancellationReason);

        db = FirebaseFirestore.getInstance();

        toolbar.setNavigationOnClickListener(v -> finish());

        currentOrder = (OrderModel) getIntent().getSerializableExtra("ORDER_OBJECT");
        if (currentOrder == null) {
            Toast.makeText(this, "Không thể lấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        displayOrderDetails();
        setupUpdateButton();
    }

    private void setupUpdateButton() {
        String status = currentOrder.getStatus();
        if ("Đã giao".equals(status) || "Đã hủy".equals(status)) {
            btnUpdateStatus.setText(status.toUpperCase());
            btnUpdateStatus.setEnabled(false);
        } else {
            btnUpdateStatus.setOnClickListener(v -> showStatusDialog());
        }
    }

    private void showStatusDialog() {
        List<String> availableStatuses = getAvailableStatuses(currentOrder.getStatus());
        String[] statusOptions = availableStatuses.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Chọn trạng thái mới")
                .setItems(statusOptions, (dialog, which) -> {
                    String newStatus = statusOptions[which];
                    updateOrderStatus(newStatus);
                })
                .show();
    }

    private List<String> getAvailableStatuses(String currentStatus) {
        List<String> statuses = new ArrayList<>();
        switch (currentStatus) {
            case "Đang xử lý":
                statuses.add("Chờ giao hàng");
                break;
            case "Chờ giao hàng":
                statuses.add("Đã giao");
                break;
        }
        if (!"Đã giao".equals(currentStatus)){
            statuses.add("Đã hủy");
        }
        return statuses;
    }

    private void displayOrderDetails() {
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, currentOrder.getItems(), true, null);
        recyclerProducts.setAdapter(cartAdapter);
        loadUserInfo();

        tvOrderStatus.setText("Trạng thái: " + currentOrder.getStatus());
        if ("Đã hủy".equalsIgnoreCase(currentOrder.getStatus()) && currentOrder.getCancellationReason() != null && !currentOrder.getCancellationReason().isEmpty()) {
            tvCancellationReason.setText("Lý do: " + currentOrder.getCancellationReason());
            tvCancellationReason.setVisibility(View.VISIBLE);
        }else {
            tvCancellationReason.setVisibility(View.GONE);
        }

    }

    private void loadUserInfo() {
        db.collection("users").document(currentOrder.getUserId()).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                tvUserEmail.setText("Email: " + userDoc.getString("email"));
            }
        });
        AddressModel orderAddress = currentOrder.getAddress();
        if (orderAddress != null) {
            String recipientInfo = "Người nhận: " + orderAddress.getName() + "\n" +
                    "SĐT: " + orderAddress.getPhone() + "\n" +
                    "Địa chỉ: " + orderAddress.getAddressLine() + ", " +
                    orderAddress.getWard() + ", " +
                    orderAddress.getDistrict() + ", " +
                    orderAddress.getCity();
            tvAddress.setText(recipientInfo);
        } else {
            tvAddress.setText("Địa chỉ: Không có thông tin");
        }
    }
    private void updateOrderStatus(String newStatus) {
        if (currentOrder.getUserId() == null || currentOrder.getOrderId() == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", newStatus);

        db.collection("users").document(currentOrder.getUserId())
                .collection("orders").document(currentOrder.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    // Cập nhật lại đối tượng và giao diện
                    currentOrder.setStatus(newStatus);
                    setupUpdateButton();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
