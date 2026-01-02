package com.example.myshop.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Adapters.ReviewImageAdapter;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Models.ReviewModel;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductReviewActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName, tvRatingStatus, tvUsernameHint;
    private RatingBar ratingBar;
    private EditText etReviewComment;
    private MaterialSwitch switchShowUsername;
    private Button btnSubmitReview;
    private MaterialToolbar toolbar;

    private RecyclerView recyclerReviewImages;
    private MaterialCardView layoutAddImage;

    private CartModel productToReview;
    private String orderId;

    private ReviewImageAdapter imageAdapter;
    private ArrayList<Uri> selectedImageUris = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    // Launcher mới để chọn nhiều ảnh
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                if (!uris.isEmpty()) {
                    // Giới hạn chỉ lấy tối đa 5 ảnh
                    int currentSize = selectedImageUris.size();
                    int remainingSlots = 5 - currentSize;
                    int itemsToAdd = Math.min(uris.size(), remainingSlots);

                    for (int i = 0; i < itemsToAdd; i++) {
                        selectedImageUris.add(uris.get(i));
                    }
                    imageAdapter.notifyDataSetChanged();
                    checkAddImageButtonVisibility();
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });

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

        recyclerReviewImages = findViewById(R.id.recyclerReviewImages);
        layoutAddImage = findViewById(R.id.layoutAddImage);

        initFirebase();

        setupToolbar();
        displayProductInfo();
        setupRatingBar();
        setupUsernameSwitch();
        setupImagePicker();
        setupSubmitButton();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
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

    private void setupImagePicker() {
        // Cấu hình RecyclerView
        recyclerReviewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ReviewImageAdapter(this, selectedImageUris, position -> {
            selectedImageUris.remove(position);
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, selectedImageUris.size());
            checkAddImageButtonVisibility();
        });
        recyclerReviewImages.setAdapter(imageAdapter);

        // Sự kiện click nút thêm ảnh
        layoutAddImage.setOnClickListener(v -> {
            // Mở picker ảnh, chỉ cho phép chọn ảnh (image only)
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        checkAddImageButtonVisibility();
    }

    private void checkAddImageButtonVisibility() {
        // Ẩn nút thêm ảnh nếu đã đủ 5 ảnh
        if (selectedImageUris.size() >= 5) {
            layoutAddImage.setVisibility(View.GONE);
        } else {
            layoutAddImage.setVisibility(View.VISIBLE);
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

        if (selectedImageUris.isEmpty()) {
            postReviewToFirestore(rating, comment, new ArrayList<>());
        } else {
            // Nếu có ảnh, upload ảnh trước
            uploadImagesAndPostReview(rating, comment);
        }
    }

    private void uploadImagesAndPostReview(float rating, String comment) {
        StorageReference storageRef = storage.getReference();
        List<String> imageUrls = new ArrayList<>();
        AtomicInteger uploadCounter = new AtomicInteger(0);

        for (Uri uri : selectedImageUris) {
            // Tạo tên file ngẫu nhiên
            StorageReference imageRef = storageRef.child("review_images/" + UUID.randomUUID().toString());
            imageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                imageUrls.add(downloadUri.toString());
                                // Kiểm tra nếu đã upload xong tất cả
                                if (uploadCounter.incrementAndGet() == selectedImageUris.size()) {
                                    postReviewToFirestore(rating, comment, imageUrls);
                                }
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải lên ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSubmitReview.setEnabled(true);
                        btnSubmitReview.setText("Gửi đánh giá");
                    });
        }
    }

    private void postReviewToFirestore(float rating, String comment, List<String> imageUrls) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String userName = (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty())
                ? currentUser.getDisplayName()
                : currentUser.getEmail();
        db.collection("users").document(userId).get().addOnSuccessListener(userDocument -> {
                    String userAvatarUrl = ""; // Giá trị mặc định
                    if (userDocument.exists() && userDocument.contains("profileImage")) {
                        userAvatarUrl = userDocument.getString("profileImage");
                    }
                    boolean isAnonymous = !switchShowUsername.isChecked();
                    String productId = productToReview.getProductId();

                    ReviewModel review = new ReviewModel(
                            userId,
                            userName,
                            userAvatarUrl,
                            isAnonymous,
                            productId,
                            orderId,
                            rating,
                            comment,
                            new Timestamp(new Date()),
                            imageUrls
                    );

                    db.collection("products")
                            .document(productId)
                            .collection("reviews")
                            .add(review)
                            .addOnSuccessListener(documentReference -> {

                                Toast.makeText(this, "Cảm ơn bạn đã gửi đánh giá!", Toast.LENGTH_LONG).show();
                                db.collection("orders")
                                        .document(orderId)
                                        .collection("products")
                                        .document(productId)
                                        .update("reviewed", true);
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Gửi đánh giá thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                btnSubmitReview.setEnabled(true);
                                btnSubmitReview.setText("Gửi đánh giá");
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lấy thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmitReview.setEnabled(true);
                    btnSubmitReview.setText("Gửi đánh giá");
                });
    }
}
