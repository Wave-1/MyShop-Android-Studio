package com.example.myshop.Activities;

import android.app.AlertDialog;
import android.app.ComponentCaller;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.OrderAdapter;
import com.example.myshop.Util.Constants;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends BaseAdminActivity {

    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private TextView tvNoOrders;

    private Button btnAll, btnProcessing, btnShipping, btnDelivered, btnCompleted, btnCancelled;
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

        btnAll = findViewById(R.id.btnAll);
        btnProcessing = findViewById(R.id.btnProcessing);
        btnShipping = findViewById(R.id.btnShipping);
        btnDelivered = findViewById(R.id.btnDelivered);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnCancelled = findViewById(R.id.btnCancelled);

        String initialStatus = getIntent().getStringExtra(Constants.INTENT_KEY_ORDER_STATUS);

        if (initialStatus == null) {
            initialStatus = Constants.ORDER_STATUS_ALL;
        }

        setupRecyclerView();
        setupFilterButtons();
        filterOrdersByStatus(initialStatus, true);;
        loadOrders(currentStatus);
    }

    @Override
    protected int getCurrentMenuId() {
        return R.id.nav_admin_orders;
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
        btnAll.setSelected(Constants.ORDER_STATUS_ALL.equals(activeStatus));
        btnProcessing.setSelected(Constants.ORDER_STATUS_PROCESSING.equals(activeStatus));
        btnShipping.setSelected(Constants.ORDER_STATUS_SHIPPING.equals(activeStatus));
        btnDelivered.setSelected(Constants.ORDER_STATUS_DELIVERED.equals(activeStatus));
        btnCompleted.setSelected(Constants.ORDER_STATUS_COMPLETED.equals(activeStatus));
        btnCancelled.setSelected(Constants.ORDER_STATUS_CANCELLED.equals(activeStatus));
    }

    // Bộ lọc theo trạng thái
    private void setupFilterButtons() {
        btnAll.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_ALL, false));
        btnProcessing.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_PROCESSING, false));
        btnShipping.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_SHIPPING, false));
        btnDelivered.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_DELIVERED, false));
        btnCompleted.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_COMPLETED, false));
        btnCancelled.setOnClickListener(v -> filterOrdersByStatus(Constants.ORDER_STATUS_CANCELLED, false));
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
                }, null,
                null,
                null,
                null
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