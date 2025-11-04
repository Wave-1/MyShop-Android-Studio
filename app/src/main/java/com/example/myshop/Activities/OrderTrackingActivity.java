package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Constants;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.Adapters.OrderAdapter;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderTrackingActivity extends AppCompatActivity implements OrderAdapter.OnOrderCancelListener {
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
        orderAdapter = new OrderAdapter(this,
                (ArrayList<OrderModel>) orderList,
                order -> {
                    Intent intent = new Intent(OrderTrackingActivity.this, OrderDetailActivity.class);
                    intent.putExtra("ORDER_DETAIL", order);
                    startActivity(intent);
                }, this
        );
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);

    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            String fromActivity = getIntent().getStringExtra("FROM_ACTIVITY");
            if ("ACCOUNT".equals(fromActivity)) {
                finish();
            } else {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
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
                        try {
                            // Cố gắng chuyển đổi document thành đối tượng OrderModel
                            OrderModel order = doc.toObject(OrderModel.class);
                            // Rất quan trọng: Gán ID của document vào đối tượng OrderModel
                            // vì ID không được tự động map.
                            order.setOrderId(doc.getId());
                            orderList.add(order);
                        } catch (Exception e) {
                            // Nếu chuyển đổi thất bại, ghi lại lỗi và bỏ qua đơn hàng này
                            // Điều này ngăn ứng dụng bị crash và giúp bạn tìm ra đơn hàng lỗi
                            android.util.Log.e("FirestoreDeserialize",
                                    "Lỗi chuyển đổi đơn hàng trong OrderTrackingActivity: " + doc.getId() + ". Nguyên nhân: " + e.getMessage());
                        }
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

    @Override
    public void onCancelClick(OrderModel order) {
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_cancel_order);

        RadioGroup rgReasons = dialog.findViewById(R.id.rgReasons);
        EditText etOtherReason = dialog.findViewById(R.id.etOtherReason);
        Button btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnDialogConfirm = dialog.findViewById(R.id.btnDialogConfirm);
        RadioButton rbReasonOther = dialog.findViewById(R.id.rbReasonOther);

        rgReasons.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbReasonOther){
                etOtherReason.setVisibility(View.VISIBLE);
            } else {
                etOtherReason.setVisibility(View.GONE);
            }
        });
        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());

        btnDialogConfirm.setOnClickListener(v -> {
            String reason = "";
            int selectedId = rgReasons.getCheckedRadioButtonId();

            if (selectedId == -1){
                Toast.makeText(this, "Vui lòng chọn một lý do", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedId == R.id.rbReasonOther){
                reason = etOtherReason.getText().toString().trim();
                if (reason.isEmpty()){
                    Toast.makeText(this, "Vui lòng nhập lý do của bạn", Toast.LENGTH_SHORT).show();
                    return;
                }
            }else {
                RadioButton selectedRadioButton = dialog.findViewById(selectedId);
                reason = selectedRadioButton.getText().toString();
            }
            cancelOrderInFirestore(order, reason);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void cancelOrderInFirestore(OrderModel orderCancel, String reason) {
        showLoading(true);
        db.collection("users")
                .document(targetUserId)
                .collection("orders")
                .document(orderCancel.getOrderId())
                .update("status", Constants.ORDER_STATUS_CANCELLED,
                        "cancellationReason", reason)
                .addOnSuccessListener(aVoid -> {
                   Toast.makeText(this, "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                   loadOrders(currentStatus);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi khi hủy đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}
