package com.example.myshop.Activities;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.CartAdapter;
import com.example.myshop.Adapters.OrderDetailAdapter;
import com.example.myshop.Models.AddressModel;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextView tvUserEmail, tvAddress, tvOrderStatus, tvCancellationReason, tvDetailTotalAmount;
    private RecyclerView recyclerProducts;
    private OrderModel currentOrder;
    private OrderDetailAdapter orderDetailAdapter;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        toolbar = findViewById(R.id.toolbar);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvAddress = findViewById(R.id.tvAddress);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvCancellationReason = findViewById(R.id.tvCancellationReason);
        tvDetailTotalAmount = findViewById(R.id.tvDetailTotalAmount);
        recyclerProducts = findViewById(R.id.recyclerProducts);

        db = FirebaseFirestore.getInstance();
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ORDER_DETAIL")) {
            currentOrder = (OrderModel) intent.getSerializableExtra("ORDER_DETAIL");
        }
        if (currentOrder == null) {
            finish();
            return;
        }

        displayOrderDetails();
    }

    private void displayOrderDetails() {
        displayCustomerInfo();
        displayOrderStatus();
        tvDetailTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f ₫", currentOrder.getTotalAmount()));
    }


    private void displayOrderStatus() {
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        orderDetailAdapter = new com.example.myshop.Adapters.OrderDetailAdapter(
                this,
                currentOrder.getItems(),
                currentOrder.getStatus(), // Truyền trạng thái để hiện/ẩn nút đánh giá
                currentOrder.getOrderId() // Truyền ID đơn để biết đánh giá đơn nào
        );
        recyclerProducts.setAdapter(orderDetailAdapter);
        displayCustomerInfo();

        tvOrderStatus.setText("Trạng thái: " + currentOrder.getStatus());
        if ("Đã hủy".equalsIgnoreCase(currentOrder.getStatus()) && currentOrder.getCancellationReason() != null && !currentOrder.getCancellationReason().isEmpty()) {
            tvCancellationReason.setText("Lý do: " + currentOrder.getCancellationReason());
            tvCancellationReason.setVisibility(View.VISIBLE);
        }else {
            tvCancellationReason.setVisibility(View.GONE);
        }
    }

    private void displayCustomerInfo() {
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

}
