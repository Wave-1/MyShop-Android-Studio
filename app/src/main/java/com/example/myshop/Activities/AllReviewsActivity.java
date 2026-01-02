package com.example.myshop.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.ReviewAdapter;
import com.example.myshop.Models.ReviewModel;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerAllReviews;
    private TextView tvNoReviewsMessage;
    private MaterialToolbar toolbar;

    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList;
    private FirebaseFirestore db;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        // Lấy productId được truyền từ ProductDetailActivity
        productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initFirebase();
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadAllReviews();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerAllReviews = findViewById(R.id.recyclerAllReviews);
        tvNoReviewsMessage = findViewById(R.id.tvNoReviewsMessage);
    }

    private void setupToolbar() {
        // Bắt sự kiện click nút Back trên toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        recyclerAllReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerAllReviews.setAdapter(reviewAdapter);
    }

    private void loadAllReviews() {
        db.collection("products").document(productId)
                .collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sắp xếp theo ngày mới nhất
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviewList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReviewModel review = document.toObject(ReviewModel.class);
                        reviewList.add(review);
                    }

                    if (reviewList.isEmpty()) {
                        tvNoReviewsMessage.setVisibility(View.VISIBLE);
                        recyclerAllReviews.setVisibility(View.GONE);
                    } else {
                        tvNoReviewsMessage.setVisibility(View.GONE);
                        recyclerAllReviews.setVisibility(View.VISIBLE);
                        reviewAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải danh sách đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvNoReviewsMessage.setVisibility(View.VISIBLE);
                });
    }
}
