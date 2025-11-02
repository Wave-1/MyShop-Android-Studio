package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Constants;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.Adapters.OrderAdapter;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderTrackingActivity extends AppCompatActivity {
    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private MaterialToolbar toolbar;
    Button btnProcessing, btnShipping, btnCompleted, btnCancelled;
    private Button selectedButton;
    private FirebaseFirestore db;
    private OrderAdapter orderAdapter;
    private List<OrderModel> orderList;
    private String targetUserId;
    private String currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        toolbar = findViewById(R.id.toolbar);
        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressBar = findViewById(R.id.progressBar);
        emptyLayout = findViewById(R.id.emptyLayout);

        btnProcessing = findViewById(R.id.btnProcessing);
        btnShipping = findViewById(R.id.btnShipping);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnCancelled = findViewById(R.id.btnCancelled);

        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();

        String initialStatus = getIntent().getStringExtra(Constants.INTENT_KEY_ORDER_STATUS);

        if (initialStatus == null) {
            initialStatus = Constants.ORDER_STATUS_PROCESSING;
        }

        if (!determineTargetUser()) {
            return;
        }

        filterOrdersByStatus(initialStatus, true);

        setupToolbar();
        setupRecyclerView();
        setupFilterButtons();
        loadOrders(currentStatus);

    }

    private void filterOrdersByStatus(String status, boolean isInitial) {
        if (!isInitial && status.equals(currentStatus)) {
            return;
        }
        currentStatus = status;
        updateFilterButtons(status);
        loadOrders(status);
    }

    private void updateFilterButtons(String activeStatus) {
        btnProcessing.setSelected(Constants.ORDER_STATUS_PROCESSING.equals(activeStatus));
        btnShipping.setSelected(Constants.ORDER_STATUS_SHIPPING.equals(activeStatus));
        btnCompleted.setSelected(Constants.ORDER_STATUS_COMPLETED.equals(activeStatus));
        btnCancelled.setSelected(Constants.ORDER_STATUS_CANCELLED.equals(activeStatus));
    }

    private boolean determineTargetUser() {
        String userIdAdmin = getIntent().getStringExtra("userId");
        String emailAdmin = getIntent().getStringExtra("userEmail");
        if (userIdAdmin != null) {
            setTitle("Đơn hàng của: " + emailAdmin);
            targetUserId = userIdAdmin;
        } else {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Bạn cần đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
                return false;
            }
            targetUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return true;
    }

    private void setupFilterButtons() {

        btnProcessing.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_PROCESSING, false));
        btnShipping.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_SHIPPING, false));
        btnCompleted.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_COMPLETED, false));
        btnCancelled.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_CANCELLED, false));
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, (ArrayList<OrderModel>) orderList, order -> {
            // Hành động khi người dùng click vào một đơn hàng: Mở trang chi tiết
            Intent intent = new Intent(OrderTrackingActivity.this, OrderDetailActivity.class);
            intent.putExtra("ORDER_DETAIL", order); // Đảm bảo OrderModel là Serializable
            startActivity(intent);
        });
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);

    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadOrders(String status) {
        showLoading(true);
        db.collection("users")
                .document(targetUserId)
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
                    orderAdapter.notifyDataSetChanged();
                    showLoading(false);
                    updateEmptyState();

                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            recyclerOrders.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerOrders.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerOrders.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
