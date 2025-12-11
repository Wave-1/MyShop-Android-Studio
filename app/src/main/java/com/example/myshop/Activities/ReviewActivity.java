package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.*;
import com.example.myshop.Adapters.ProductsToReviewAdapter;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.Util.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReviewActivity extends AppCompatActivity implements ProductsToReviewAdapter.OnProductReviewClickListener {

    private static final int REVIEW_REQUEST_CODE = 101;

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerReviews;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private TextView tvEmptyMessage;
    private ProductsToReviewAdapter adapter;
    private List<CartModel> currentList;
    private List<CartModel> pendingList;
    private List<CartModel> completedList;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerReviews = findViewById(R.id.recyclerReviews);
        progressBar = findViewById(R.id.progressBar);
        emptyLayout = findViewById(R.id.emptyLayout);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem đánh giá", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        setSupportActionBar(toolbar);
        // Sửa lại để tránh NullPointerException
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());


        currentList = new ArrayList<>();
        pendingList = new ArrayList<>();
        completedList = new ArrayList<>();

        setupRecyclerView();
        // Chuyển setupTabLayout() lên trước để nó sẵn sàng nhận dữ liệu
        setupTabLayout();
        loadAllPurchasedProducts();
    }


    private void setupRecyclerView() {
        adapter = new ProductsToReviewAdapter(currentList, null, this);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterList(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void filterList(int tabPosition) {
        currentList.clear();
        boolean isPendingTab = (tabPosition == 0);

        if (isPendingTab) {
            currentList.addAll(pendingList);
        } else {
            currentList.addAll(completedList);
        }
        // ✅ BƯỚC 1: Gọi hàm updateEmptyState tại đây
        updateEmptyState(currentList.isEmpty(), isPendingTab);
        adapter.notifyDataSetChanged();
    }

    private void loadAllPurchasedProducts() {
        showLoading(true);

        db.collectionGroup("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> reviewedProductIds = new HashSet<>();
                    for (QueryDocumentSnapshot reviewDoc : queryDocumentSnapshots) {
                        reviewedProductIds.add(reviewDoc.getString("productId"));
                    }
                    fetchCompletedOrders(reviewedProductIds);
                }).addOnFailureListener(e -> {
                    Log.e("ReviewActivity", "Lỗi tải danh sách sản phẩm", e);
                    fetchCompletedOrders(new HashSet<>());
                });
    }

    private void fetchCompletedOrders(Set<String> reviewedProductIds) {
        db.collection("users")
                .document(userId)
                .collection("orders")
                .whereEqualTo("status", Constants.ORDER_STATUS_COMPLETED)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    Map<String, CartModel> allProducts = new HashMap<>();
                    for (QueryDocumentSnapshot orderDoc : orderSnapshots) {
                        OrderModel order = orderDoc.toObject(OrderModel.class);
                        if (order.getItems() != null) {
                            for (CartModel item : order.getItems()) {
                                item.setOrderId(orderDoc.getId());
                                allProducts.put(item.getProductId(), item);
                            }
                        }
                    }

                    pendingList.clear();
                    completedList.clear();

                    for (CartModel product : allProducts.values()) {
                        if (reviewedProductIds.contains(product.getProductId())) {
                            product.setReviewed(true);
                            completedList.add(product);
                        } else {
                            product.setReviewed(false);
                            pendingList.add(product);
                        }
                    }
                    showLoading(false);
                    // Cập nhật lại giao diện dựa trên tab đang được chọn
                    filterList(tabLayout.getSelectedTabPosition());
                })
                .addOnFailureListener(e -> {
                    Log.e("ReviewActivity", "Lỗi khi tải đơn hàng đã hoàn thành.", e);
                    showLoading(false);
                    Toast.makeText(this, "Không thể tải dữ liệu.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerReviews.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (isLoading) {
            emptyLayout.setVisibility(View.GONE);
        }
    }

    // ✅ BƯỚC 3: Sửa lại logic của updateEmptyState
    private void updateEmptyState(boolean isListEmpty, boolean isPendingTab) {
        if (isListEmpty) {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerReviews.setVisibility(View.GONE);
            if (isPendingTab) {
                // Nếu tab "Chưa đánh giá" trống, kiểm tra xem có sản phẩm nào đã mua chưa
                if (pendingList.isEmpty() && completedList.isEmpty()) {
                    tvEmptyMessage.setText("Bạn chưa mua sản phẩm nào.");
                } else {
                    tvEmptyMessage.setText("Tất cả sản phẩm đã được đánh giá!");
                }
            } else {
                tvEmptyMessage.setText("Chưa có sản phẩm nào được đánh giá.");
            }
        } else {
            emptyLayout.setVisibility(View.GONE);
            recyclerReviews.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onProductReviewClick(CartModel product, String orderId) {
        Intent intent = new Intent(this, ProductReviewActivity.class);
        intent.putExtra("PRODUCT_TO_REVIEW", product);
        // ✅ BƯỚC 2: Dùng getOrderId() để lấy ID đơn hàng đã lưu
        intent.putExtra("ORDER_ID", product.getOrderId());
        startActivityForResult(intent, REVIEW_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REVIEW_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Đang cập nhật...", Toast.LENGTH_SHORT).show();
            loadAllPurchasedProducts();
        }
    }
}
