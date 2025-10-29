package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.myshop.Models.ProductModel;
import com.example.myshop.Adapters.ProductAdapter;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;

public class HomeActivity extends AppCompatActivity {

    EditText edtSearch;
    Button btnSearch;
    RecyclerView recyclerFlashSale, recyclerSuggestion, recyclerNewProducts, recyclerBestSeller;
    BottomNavigationView bottomNav;

    ArrayList<ProductModel> flashSaleList, recommendList, newProductsList, bestSellerList;
    ProductAdapter flashSaleAdapter, recommendAdapter, newProductsAdapter, bestSellerAdapter;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private final long AUTO_SCROLL_DELAY = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerFlashSale = findViewById(R.id.recyclerFlashSale);
        recyclerSuggestion = findViewById(R.id.recyclerSuggestion);
        recyclerNewProducts = findViewById(R.id.recyclerNewProducts);
        recyclerBestSeller = findViewById(R.id.recyclerBestSeller);


        bottomNav = findViewById(R.id.bottomNav);
        NestedScrollView scrollMain = findViewById(R.id.scrollMain);

        // Né tai thỏ
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBarInsets.top, 0, 0);
            return insets;
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductActivity.class);
            startActivity(intent);
        });
        // Flash Sale: hiển thị ngang
        flashSaleList = new ArrayList<>();
        flashSaleAdapter = new ProductAdapter(this, flashSaleList);
        recyclerFlashSale.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerFlashSale.setHasFixedSize(true);
        recyclerFlashSale.setAdapter(flashSaleAdapter);

        // Recommend: hiển thị dạng lưới 2 cột
        recommendList = new ArrayList<>();
        recommendAdapter = new ProductAdapter(this, recommendList);
        recyclerSuggestion.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerSuggestion.setAdapter(recommendAdapter);
        recyclerSuggestion.setNestedScrollingEnabled(false);

        // Sản phẩm mới
        newProductsList = new ArrayList<>();
        newProductsAdapter = new ProductAdapter(this, newProductsList);
        recyclerNewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerNewProducts.setAdapter(newProductsAdapter);
        recyclerNewProducts.setNestedScrollingEnabled(false);

        // Sản phẩm bán chạy
        bestSellerList = new ArrayList<>();
        bestSellerAdapter = new ProductAdapter(this, bestSellerList);
        recyclerBestSeller.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerBestSeller.setAdapter(bestSellerAdapter);
        recyclerBestSeller.setNestedScrollingEnabled(false);

        // Lấy danh sách Flash Sale (sản phẩm có salePercent > 0)
        db.collection("products").whereGreaterThan("salePercent", 0).get().addOnSuccessListener(querySnapshot -> {
            flashSaleList.clear();
            for (DocumentSnapshot doc : querySnapshot) {
                ProductModel p = doc.toObject(ProductModel.class);
                if (p != null) {
                    p.setProductId(doc.getId()); // gán id Firestore
                    flashSaleList.add(p);
                }
            }
            flashSaleAdapter.notifyDataSetChanged();
            if (flashSaleList.size() > 1) {
                stopAutoScroll();
                startAutoScroll();
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Lỗi load Flash Sale", e));

        // Lấy danh sách gợi ý
        db.collection("products").get().addOnSuccessListener(querySnapshot -> {
            if (querySnapshot.isEmpty()) {
                Log.d("Firestore", "Không có sản phẩm");
                return;
            }

            ArrayList<ProductModel> allProducts = new ArrayList<>();
            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                ProductModel p = documentSnapshot.toObject(ProductModel.class);
                if (p != null){
                    p.setProductId(documentSnapshot.getId());
                    allProducts.add(p);
                }
            }
            Collections.shuffle(allProducts);
            recommendList.clear();
            int numberOfItemsToShow = 10;
            for (int i = 0; i < Math.min(numberOfItemsToShow, allProducts.size()); i++) {
                recommendList.add(allProducts.get(i));
            }
            recommendAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Log.e("Firestore", "Lỗi load Recommend", e));

        // Lấy sản phẩm mới
        db.collection("products")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    newProductsList.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ProductModel p = documentSnapshot.toObject(ProductModel.class);
                        if (p != null) {
                            p.setProductId(documentSnapshot.getId());
                            newProductsList.add(p);
                        }
                    }
                    newProductsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải sản phẩm mới: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Lấy sản phẩm bán chạy
        db.collection("products")
                .orderBy("salesCount", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bestSellerList.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ProductModel p = documentSnapshot.toObject(ProductModel.class);
                        if (p != null) {
                            p.setProductId(documentSnapshot.getId());
                            bestSellerList.add(p);
                        }
                    }
                    bestSellerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải sản phẩm mới: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


        // ✅ Gắn sự kiện Bottom Navigation
        bottomNav.setSelectedItemId(R.id.nav_home); // Chọn tab Home mặc định
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true; // đang ở Home rồi
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(HomeActivity.this, ProductActivity.class));
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(HomeActivity.this, AccountActivity.class));
                return true;
            }

            return false;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
    }

    private void startAutoScroll() {
        autoScrollHandler = new Handler(Looper.getMainLooper());

        // Tạo một Runnable để thực hiện việc cuộn
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                // Lấy vị trí hiện tại
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerFlashSale.getLayoutManager();
                if (layoutManager == null) return;

                int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    currentPosition = layoutManager.findFirstVisibleItemPosition();
                }

                // Tính vị trí tiếp theo
                int nextPosition = currentPosition + 1;
                // Nếu đã đến cuối danh sách, quay lại vị trí đầu tiên
                if (nextPosition >= flashSaleAdapter.getItemCount()) {
                    nextPosition = 0;
                }

                // Cuộn mượt đến vị trí tiếp theo
                recyclerFlashSale.smoothScrollToPosition(nextPosition);

                // Lặp lại hành động này sau một khoảng thời gian
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        };

        // Bắt đầu vòng lặp
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    private void stopAutoScroll() {
        // Dừng vòng lặp nếu nó đang chạy
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }


    private void searchProducts(String keyword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        recommendList.clear();

        db.collection("products").get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot doc : querySnapshot) {
                ProductModel p = doc.toObject(ProductModel.class);
                if (p != null && p.getName() != null && p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    p.setProductId(doc.getId());
                    recommendList.add(p);
                }
            }
            recommendAdapter.notifyDataSetChanged();

            if (recommendList.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy sản phẩm phù hợp!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
