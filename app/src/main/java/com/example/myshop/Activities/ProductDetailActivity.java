package com.example.myshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.system.Os;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Adapters.ProductAdapter;
import com.example.myshop.Adapters.ReviewAdapter;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Models.ProductModel;
import com.example.myshop.Models.ReviewModel;
import com.example.myshop.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct;
    private TextView tvProductName, tvProductPrice, tvProductOriginalPrice, tvSalePercent, tvNoReviews;
    private Button btnAddToCart, btnBuyNow, btnSubmitReview;
    private RatingBar ratingBar;
    private EditText edtComment;
    private LinearLayout reviewInputLayout, descriptionContainer;
    private RecyclerView recyclerReviews, recyclerRelatedProducts;
    private String role, productId, userId;
    private ProductAdapter relatedProductsAdapter;
    private ArrayList<ProductModel> relatedProductsList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);

        role = getIntent().getStringExtra("role");
        productId = getIntent().getStringExtra("productId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        imgProduct = findViewById(R.id.imgProduct);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);

        descriptionContainer = findViewById(R.id.layout_description_container);
        tvSalePercent = findViewById(R.id.tvSalePercent);
        tvProductOriginalPrice = findViewById(R.id.tvProductOriginalPrice);

        // Reviews
        reviewInputLayout = findViewById(R.id.reviewInputLayout);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        recyclerReviews = findViewById(R.id.recyclerReviews);
        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        // Sản phẩm liên quan
        recyclerRelatedProducts = findViewById(R.id.recyclerRelatedProducts);
        relatedProductsList = new ArrayList<>();

        relatedProductsAdapter = new ProductAdapter(this, relatedProductsList);
        recyclerRelatedProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerRelatedProducts.setAdapter(relatedProductsAdapter);


        reviewInputLayout.setVisibility(View.GONE);

        if ("admin".equals(role)) {
            btnAddToCart.setVisibility(View.GONE);
            btnBuyNow.setVisibility(View.GONE);

        } else {
            btnAddToCart.setVisibility(View.VISIBLE);
            btnBuyNow.setVisibility(View.VISIBLE);
            checkUserCanReview();
        }

        btnSubmitReview.setOnClickListener(v -> {
            submitReview();
        });

        if (productId != null) {
            db.collection("products").document(productId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            ProductModel productModel = document.toObject(ProductModel.class);
                            if (productModel != null) {
                                productModel.setProductId(document.getId()); // gán id từ Firestore

                                if (productModel.getImage() != null && !productModel.getImage().isEmpty()) {
                                    Glide.with(this)
                                            .load(productModel.getImage()) // URL từ Firestore
                                            .placeholder(R.drawable.bg_image_placeholder) // ảnh tạm khi đang load
                                            .error(R.drawable.store)       // ảnh nếu load lỗi
                                            .into(imgProduct);
                                }
                                tvProductName.setText(productModel.getName());
                                tvProductPrice.setText(String.format("%,.0f ₫", productModel.getPrice()));
//                                tvProductDesc.setText(productModel.getDescription() +
//                                        " - Đây là mô tả chi tiết của " + productModel.getName());
                                renderDescription(productModel.getDescription());
                                showProductPrice(productModel);
                                loadReviews();

                                loadRelatedProducts(productModel.getCategory(), productModel.getProductId());

                                // TODO: gán sự kiện btnAddToCart, btnBuyNow
                                btnAddToCart.setOnClickListener(v -> {
                                    String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                                            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                            : null;

                                    if (uid == null) {
                                        Toast.makeText(this, "Bạn cần đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    double finalPrice = productModel.isOnSale() ? productModel.getSalePrice() : productModel.getPrice();

                                    db.collection("users")
                                            .document(uid)
                                            .collection("cart")
                                            .document(productModel.getProductId())
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    Long oldQuantity = documentSnapshot.getLong("quantity");
                                                    if (oldQuantity == null)
                                                        oldQuantity = 1L;
                                                    long newQuantity = oldQuantity + 1;
                                                    db.collection("users")
                                                            .document(uid)
                                                            .collection("cart")
                                                            .document(productModel.getProductId())
                                                            .update("quantity", newQuantity)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(this, "Lỗi thêm giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                } else {
                                                    CartModel cartModel = new CartModel(productModel.getProductId(), productModel.getName(), finalPrice, productModel.getImage(), 1);
                                                    db.collection("users")
                                                            .document(uid)
                                                            .collection("cart")
                                                            .document(productModel.getProductId())
                                                            .set(cartModel)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(this, "Lỗi thêm giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                }

                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Lỗi thêm giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                });

                                btnBuyNow.setOnClickListener(v -> {
                                    String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                                            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                            : null;

                                    if (uid == null) {
                                        Toast.makeText(this, "Bạn cần đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    double finalPrice = productModel.isOnSale() ? productModel.getSalePrice() : productModel.getPrice();

                                    db.collection("users")
                                            .document(uid)
                                            .collection("cart")
                                            .document(productModel.getProductId())
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    Long oldQuantity = documentSnapshot.getLong("quantity");
                                                    if (oldQuantity == null)
                                                        oldQuantity = 1L;
                                                    long newQuantity = oldQuantity + 1;
                                                    db.collection("users")
                                                            .document(uid)
                                                            .collection("cart")
                                                            .document(productModel.getProductId())
                                                            .update("quantity", newQuantity)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Intent intent = new Intent(this, CartActivity.class);
                                                                startActivity(intent);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(this, "Lỗi thêm giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                } else {
                                                    CartModel cartModel = new CartModel(productModel.getProductId(), productModel.getName(), finalPrice, productModel.getImage(), 1);
                                                    db.collection("users")
                                                            .document(uid)
                                                            .collection("cart")
                                                            .document(productModel.getProductId())
                                                            .set(cartModel)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Intent intent = new Intent(this, CartActivity.class);
                                                                startActivity(intent);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(this, "Lỗi thêm giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                }

                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Lỗi thêm giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }

    }

    private void loadRelatedProducts(String category, String currentProductId) {
        if (category == null || category.isEmpty()) {
            findViewById(R.id.tvRelatedProductsTitle).setVisibility(View.GONE);
            recyclerRelatedProducts.setVisibility(View.GONE);
            return;
        }

        db.collection("products")
                .whereEqualTo("category", category)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    relatedProductsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ProductModel product = document.toObject(ProductModel.class);
                        product.setProductId(document.getId());
                        if (!product.getProductId().equals(currentProductId)){
                            relatedProductsList.add(product);
                        }
                    }
                    relatedProductsAdapter.notifyDataSetChanged();

                    // Ẩn/hiện khu vực sản phẩm liên quan nếu không có dữ liệu
                    View titleView = findViewById(R.id.tvRelatedProductsTitle);
                    if (relatedProductsList.isEmpty()) {
                        titleView.setVisibility(View.GONE);
                        recyclerRelatedProducts.setVisibility(View.GONE);
                    } else {
                        titleView.setVisibility(View.VISIBLE);
                        recyclerRelatedProducts.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Có thể ẩn đi nếu có lỗi để không làm phiền người dùng
                    findViewById(R.id.tvRelatedProductsTitle).setVisibility(View.GONE);
                    recyclerRelatedProducts.setVisibility(View.GONE);
                });
    }

    private void renderDescription(String description) {
        if (description == null || description.isEmpty()) {
            descriptionContainer.setVisibility(View.GONE);
            return;
        }

        descriptionContainer.removeAllViews();

        String imageRegex = "\\[\\[(.*?)\\]\\]";
        Pattern pattern = Pattern.compile(imageRegex);

        String[] parts = description.split(imageRegex);
        Matcher imageMatcher = pattern.matcher(description);

        int partIndex = 0;
        while (partIndex < parts.length) {

            // Thêm phần văn bản vào container
            String textSegment = parts[partIndex].trim();

            if (!textSegment.isEmpty()) {
                addFormattedText(textSegment);
            }
            partIndex++;

            // Nếu còn Os.link ảnh để xử lý, thêm nó vào
            if (imageMatcher.find()) {
                String imageUrl = imageMatcher.group(1); // Lấy URL từ group 1 của regex

                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                imageParams.setMargins(0, 8, 0, 24);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                descriptionContainer.addView(imageView, imageParams);

                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.store)
                        .into(imageView);
            }

        }
    }

    private void addFormattedText(String textSegment) {
        String boldRegex = "\\*\\*(.*?)\\*\\*";
        SpannableStringBuilder builder = new SpannableStringBuilder(textSegment);

        Pattern boldPattern = Pattern.compile(boldRegex);
        Matcher boldMatcher = boldPattern.matcher(textSegment);

        while (boldMatcher.find()) {
            builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    boldMatcher.start(), boldMatcher.end(), 0);
        }

        TextView textView = new TextView(this);
        textView.setText(builder);
        textView.setTextSize(16f);
        textView.setLineSpacing(0f, 1.2f);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(0, 0, 0, 16);
        descriptionContainer.addView(textView, textParams);
    }

    // ====================== KIỂM TRA QUYỀN ĐÁNH GIÁ ==========================
    private void checkUserCanReview() {
        if (userId == null || productId == null) return;

        db.collection("users")
                .document(userId)
                .collection("orders")
                .whereEqualTo("status", "Hoàn tất") // ✅ chỉ lấy đơn hàng đã hoàn tất
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean canReview = false;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // ✅ Lấy danh sách productIds từ đơn hàng
                        List<String> productIds = (List<String>) document.get("productIds");
                        if (productIds != null && productIds.contains(productId)) {
                            canReview = true;
                            break;
                        }
                    }

                    // ✅ Nếu người dùng đã mua sản phẩm và đơn hàng hoàn tất -> cho phép đánh giá
                    if (canReview) {
                        reviewInputLayout.setVisibility(View.VISIBLE);
                        btnSubmitReview.setVisibility(View.VISIBLE);
                    } else {
                        reviewInputLayout.setVisibility(View.GONE);
                        btnSubmitReview.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // ====================== GỬI ĐÁNH GIÁ ==========================
    private void submitReview() {
        String comment = edtComment.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> review = new HashMap<>();
        review.put("userId", userId);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", FieldValue.serverTimestamp());

        db.collection("products")
                .document(productId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    edtComment.setText("");
                    ratingBar.setRating(0);
                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private void loadReviews() {
        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ReviewModel> reviewModelList = new ArrayList<>();
        ReviewAdapter reviewAdapter = new ReviewAdapter(this, reviewModelList);
        recyclerReviews.setAdapter(reviewAdapter);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));

        db.collection("products")
                .document(productId)
                .collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviewModelList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ReviewModel reviewModel = documentSnapshot.toObject(ReviewModel.class);
                        reviewModelList.add(reviewModel);
                    }

                    if (reviewModelList.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        recyclerReviews.setVisibility(View.GONE);
                    } else {
                        tvNoReviews.setVisibility(View.GONE);
                        recyclerReviews.setVisibility(View.VISIBLE);
                        reviewAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(command -> {
                    Toast.makeText(this, "Lỗi tải danh sách đánh giá", Toast.LENGTH_SHORT).show();
                });
    }

    private void showProductPrice(ProductModel productModel) {
        double originalPrice = productModel.getPrice();

        // Nếu có giảm giá
        if (productModel.getSalePercent() > 0) {
            double salePrice = originalPrice * (1 - productModel.getSalePercent() / 100.0);

            // Hiển thị giá gốc (có gạch ngang)
            tvProductOriginalPrice.setVisibility(View.VISIBLE);
            tvProductOriginalPrice.setText(String.format("%,.0f ₫", originalPrice));
            tvProductOriginalPrice.setPaintFlags(
                    tvProductOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );

            // Hiển thị giá sale
            tvProductPrice.setText(String.format("%,.0f ₫", salePrice));

            // Hiển thị % giảm giá
            tvSalePercent.setVisibility(View.VISIBLE);
            tvSalePercent.setText(String.format("-%d%%", productModel.getSalePercent()));

        } else {
            // Không có giảm giá → chỉ hiển thị giá thường
            tvProductOriginalPrice.setVisibility(View.GONE);
            tvSalePercent.setVisibility(View.GONE);

            tvProductPrice.setText(String.format("%,.0f ₫", originalPrice));
        }
    }
}
