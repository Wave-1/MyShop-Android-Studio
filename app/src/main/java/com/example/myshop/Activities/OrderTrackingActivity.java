package com.example.myshop.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.Models.OrderModel;
import com.example.myshop.Adapters.OrderAdapter;
import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderTrackingActivity extends AppCompatActivity {
    ListView listOrders;
    OrderAdapter orderAdapter;
    List<OrderModel> orderList = new ArrayList<>();
    Button btnProcessing, btnShipping, btnCompleted;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private View emptyLayout;
    private String currentStatus = "Đang xử lý";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        listOrders = findViewById(R.id.listOrders);
        emptyLayout = findViewById(R.id.emptyLayout);

        btnProcessing = findViewById(R.id.btnProcessing);
        btnShipping = findViewById(R.id.btnShipping);
        btnCompleted = findViewById(R.id.btnCompleted);

        String userIdAdmin = getIntent().getStringExtra("userId");
        String emailAdmin = getIntent().getStringExtra("userEmail");
        String targetUserId;
        if (userIdAdmin != null) {
            setTitle("Đơn hàng của: " + emailAdmin);
            targetUserId = userIdAdmin;
            loadOrders(targetUserId, currentStatus);
            btnStatus(targetUserId);
        } else {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Bạn cần đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            targetUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadOrders(targetUserId, currentStatus);
            // Bộ lọc theo trạng thái
            btnStatus(targetUserId);
        }
    }

    private void btnStatus(String targetUserId) {
        btnProcessing.setOnClickListener(v -> {
            currentStatus = "Đang xử lý";
            loadOrders(targetUserId,currentStatus);
        });

        btnShipping.setOnClickListener(v -> {
            currentStatus = "Đang giao";
            loadOrders(targetUserId,currentStatus);
        });

        btnCompleted.setOnClickListener(v -> {
            currentStatus = "Hoàn tất";
            loadOrders(targetUserId,currentStatus);
        });
    }

    private void loadOrders(String userId, String status) {
        db.collection("users")
                .document(userId)
                .collection("orders")
                .whereEqualTo("status", status)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderModel order = doc.toObject(OrderModel.class);
                        orderList.add(order);
                    }
                    if (orderList.isEmpty()) {
                        listOrders.setVisibility(View.GONE);
                        emptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        listOrders.setVisibility(View.VISIBLE);
                        emptyLayout.setVisibility(View.GONE);
                        orderAdapter = new OrderAdapter(this, orderList);
                        listOrders.setAdapter(orderAdapter);
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
