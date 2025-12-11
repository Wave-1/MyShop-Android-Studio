package com.example.myshop.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Models.ReviewModel;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class ProductReviewActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName, tvRatingStatus, tvUsernameHint;
    private RatingBar ratingBar;
    private EditText etReviewComment;
    private MaterialSwitch switchShowUsername;
    private Button btnSubmitReview;
    private MaterialToolbar toolbar;

    private CartModel productToReview;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review);

        productToReview = (CartModel) getIntent().getSerializableExtra("PRODUCT_TO_REVIEW");
        orderId = getIntent().getStringExtra("ORDER_ID");

        if (productToReview == null || orderId == null) {
            Toast.makeText(this, "Lỗi: Không có thông tin sản phẩm để đánh giá.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Ánh xạ Views
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvRatingStatus = findViewById(R.id.tvRatingStatus);
        ratingBar = findViewById(R.id.ratingBar);
        etReviewComment = findViewById(R.id.etReviewComment);
        switchShowUsername = findViewById(R.id.switchShowUsername);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        toolbar = findViewById(R.id.toolbar);
        tvUsernameHint = findViewById(R.id.tvUsernameHint);

        setupToolbar();
        displayProductInfo();
        setupRatingBar();
        setupUsernameSwitch();
        setupSubmitButton();
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayProductInfo() {
        tvProductName.setText(productToReview.getName());
        Glide.with(this).load(productToReview.getImage()).into(ivProductImage);
    }

    private void setupRatingBar() {
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            updateRatingStatus(rating);
        });
        updateRatingStatus(ratingBar.getRating());
    }

    private void updateRatingStatus(float rating) {
        String status;
        if (rating <= 1) status = "Rất tệ";
        else if (rating <= 2) status = "Tệ";
        else if (rating <= 3) status = "Bình thường";
        else if (rating <= 4) status = "Tốt";
        else status = "Tuyệt vời";
        tvRatingStatus.setText(status);
    }

    private void setupUsernameSwitch() {
        switchShowUsername.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUsernameDisplay(isChecked);
        });
        updateUsernameDisplay(switchShowUsername.isChecked());
    }

    private void updateUsernameDisplay(boolean showFullName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String displayName = "Người dùng";

        if (currentUser != null && currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            displayName = currentUser.getDisplayName();
        } else if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
            displayName = currentUser.getEmail();
        }

        if (showFullName) {

            tvUsernameHint.setText("Tên của bạn: " + displayName);
        } else {

            if (displayName.length() > 2) {
                char firstChar = displayName.charAt(0);
                char lastChar = displayName.charAt(displayName.length() - 1);
                String maskedName = firstChar + "*****" + lastChar;
                tvUsernameHint.setText("Tên của bạn: " + maskedName);
            } else {
                // Nếu tên quá ngắn, hiển thị như cũ hoặc chỉ hiển thị dấu *
                tvUsernameHint.setText("Tên của bạn: " + displayName.charAt(0) + "*");
            }
        }
    }


    private void setupSubmitButton() {
        btnSubmitReview.setOnClickListener(v -> {
            submitReview();
        });
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = etReviewComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao để đánh giá chất lượng sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng chia sẻ một vài nhận xét về sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Bạn cần đăng nhập để thực hiện hành động này.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReview.setEnabled(false);
        btnSubmitReview.setText("Đang gửi...");

        String userId = currentUser.getUid();
        String userName = (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty())
                ? currentUser.getDisplayName()
                : currentUser.getEmail();
        String userAvatar = (currentUser.getPhotoUrl() != null) ? currentUser.getPhotoUrl().toString() : null;
        boolean isAnonymous = !switchShowUsername.isChecked();
        String productId = productToReview.getProductId();
        ReviewModel review = new ReviewModel(
                userId,
                userName,
                userAvatar,
                isAnonymous,
                productId,
                orderId,
                rating,
                comment,
                new Timestamp(new Date())
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .document(productId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(this, "Cảm ơn bạn đã gửi đánh giá!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gửi đánh giá thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmitReview.setEnabled(true);
                    btnSubmitReview.setText("Gửi đánh giá");
                });
    }
}
