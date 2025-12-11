package com.example.myshop.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.ProductAdapter;
import com.example.myshop.Models.ProductModel;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductActivity extends AppCompatActivity {

    private RecyclerView recyclerSearchResults;
    private EditText edtSearch;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private List<ProductModel> productModelList;
    private boolean isSearching = false;
    private int sortState = 0;
    private List<ProductModel> originalList = new ArrayList<>();
    private List<String> categoryNameList = new ArrayList<>();
    Button btnAll, btnNew, btnHot, btnSortPrice, btnCategory;
    List<Button> filterButtons = new ArrayList<>();
    BottomNavigationView bottomNav;
    private ImageView ivCart;
    private FrameLayout cartLayout;
    private TextView tvCartBadge;
    private ProgressBar progressBar;
    private NestedScrollView scrollMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        ivCart = findViewById(R.id.ivCart);
        cartLayout = findViewById(R.id.cartLayout);
        tvCartBadge = findViewById(R.id.tvCartBadge);

//        progressBar = findViewById(R.id.progressBar);
        scrollMain = findViewById(R.id.scrollMain);

        btnAll = findViewById(R.id.btnFilterAll);
        btnNew = findViewById(R.id.btnFilterNew);
        btnHot = findViewById(R.id.btnFilterHot);
        btnSortPrice = findViewById(R.id.btnSortPrice);
        btnCategory = findViewById(R.id.btnCategory);
        bottomNav = findViewById(R.id.bottomNav);

        // Thêm tất cả vào danh sách để dễ reset
        filterButtons.add(btnAll);
        filterButtons.add(btnNew);
        filterButtons.add(btnHot);
        filterButtons.add(btnSortPrice);
        filterButtons.add(btnCategory);

        // Gán sự kiện click
        btnAll.setOnClickListener(v -> selectButton(btnAll));
        btnNew.setOnClickListener(v -> selectButton(btnNew));
        btnHot.setOnClickListener(v -> selectButton(btnHot));
        btnSortPrice.setOnClickListener(v -> selectButton(btnSortPrice));
        btnCategory.setOnClickListener(v -> selectButton(btnCategory));

        // Mặc định chọn “Tất cả”
        selectButton(btnAll);

        // Ánh xạ view
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        edtSearch = findViewById(R.id.edtSearch);

        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        db = FirebaseFirestore.getInstance();
        productModelList = new ArrayList<>();

        adapter = new ProductAdapter(this, new ArrayList<>(productModelList));
        recyclerSearchResults.setAdapter(adapter);

        btnAll.setBackgroundTintList(null);
        btnNew.setBackgroundTintList(null);
        btnHot.setBackgroundTintList(null);
        btnSortPrice.setBackgroundTintList(null);
        btnCategory.setBackgroundTintList(null);

        // Căn lề status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBarInsets.top, 0, 0);
            return insets;
        });

        cartLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        loadCategoryNames();

        if (getIntent().hasExtra("SEARCH_KEYWORD")){
            String keyword = getIntent().getStringExtra("SEARCH_KEYWORD");
            edtSearch.setText(keyword);
            searchProducts(keyword);
        }else {
            loadProducts();
        }

        // ✅ Gắn sự kiện Bottom Navigation
        bottomNav.setSelectedItemId(R.id.nav_products); // Chọn tab Home mặc định
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));

                return true;
            } else if (id == R.id.nav_products) {
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
            }

            return false;
        });

        btnSortPrice.setOnClickListener(v -> {
            selectButton(btnSortPrice);
            switch (sortState) {
                case 0:
                    // Sắp xếp tăng dần
                    sortProductsAscending();
                    btnSortPrice.setText("Giá ↑");
                    Toast.makeText(this, "Sắp xếp theo giá tăng dần", Toast.LENGTH_SHORT).show();
                    sortState = 1;
                    break;

                case 1:
                    // Sắp xếp giảm dần
                    sortProductsDescending();
                    btnSortPrice.setText("Giá ↓");
                    Toast.makeText(this, "Sắp xếp theo giá giảm dần", Toast.LENGTH_SHORT).show();
                    sortState = 2;
                    break;

                case 2:
                    // Trở về danh sách mặc định
                    resetProductList();
                    btnSortPrice.setText("Giá ▼");
                    Toast.makeText(this, "Trở về mặc định", Toast.LENGTH_SHORT).show();
                    sortState = 0;
                    break;
            }
        });

        // Theo dõi thay đổi trong ô tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            private long lastChange = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lastChange = System.currentTimeMillis();
                edtSearch.postDelayed(() -> {
                    if (System.currentTimeMillis() - lastChange >= 400) { // debounce 400ms
                        String keyword = s.toString().trim();
                        if (keyword.isEmpty()) {
                            loadProducts();
                        } else {
                            searchProducts(keyword);
                        }
                    }
                }, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnAll.setOnClickListener(v -> {
            selectButton(btnAll);
            resetProductList();
            btnCategory.setText("Danh mục ▼");
        });

        btnNew.setOnClickListener(v -> {
            selectButton(btnNew);
            resetProductList();
            loadNewProducts();
        });

        btnHot.setOnClickListener(v -> {
            selectButton(btnHot);
            resetProductList();
            loadHotProducts();
        });

        btnCategory.setOnClickListener(v -> {
            selectButton(btnCategory);
            showCategoryDialog();
            resetProductList();
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_products);
        setupCartBadgeListener();
    }

    private void setupCartBadgeListener() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null){
            tvCartBadge.setVisibility(View.GONE);
            return;
        }
        String uid = firebaseUser.getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null){
                        tvCartBadge.setVisibility(View.GONE);
                        return;
                    }
                    if (value != null && !value.isEmpty()){
                        int totalItems = 0;
                        for (DocumentSnapshot doc : value){
                            if (doc.contains("quantity")){
                                totalItems += doc.getLong("quantity").intValue();
                                tvCartBadge.setText(String.valueOf(totalItems));
                                tvCartBadge.setVisibility(View.VISIBLE);
                            }
                        }
                    }else {
                        tvCartBadge.setVisibility(View.GONE);
                    }
                });
    }

    private void loadCategoryNames() {
        db.collection("categories")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryNameList.clear();
                    categoryNameList.add("Tất cả sản phẩm");
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String categoryName = documentSnapshot.getString("name");
                        if (categoryName != null) {
                            categoryNameList.add(categoryName);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh mục: " + e, Toast.LENGTH_SHORT).show());
    }

    private void showCategoryDialog() {
        if (categoryNameList.isEmpty()) {
            Toast.makeText(this, "Đang tải danh mục, vui lòng thử lại sau giây lát.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categories = categoryNameList.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn danh mục sản phẩm")
                .setItems(categories, (dialog, which) -> {
                    String selectedCategory = categories[which];

                    btnCategory.setText(selectedCategory);

                    if (selectedCategory.equals("Tất cả sản phẩm")) {
                        btnCategory.setText("Danh mục ▼");
                        selectButton(btnAll);
                        resetProductList();
                        loadProducts();
                    } else {
                        loadProductsByCategory(selectedCategory);
                    }

                });
        builder.create().show();
    }

    private void loadProductsByCategory(String category) {
        db.collection("products")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productModelList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ProductModel p = documentSnapshot.toObject(ProductModel.class);
                        p.setProductId(documentSnapshot.getId());
                        productModelList.add(p);
                    }
                    adapter.setProducts(new ArrayList<>(productModelList));
                    adapter.notifyDataSetChanged();
                    if (productModelList.isEmpty()) {
                        Toast.makeText(this, "Không có sản phẩm nào trong danh mục này.", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lọc sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void selectButton(Button selectedButton) {
        for (Button btn : filterButtons) {
            if (btn == selectedButton) {
                btn.setBackgroundResource(R.drawable.filter_selected);
                btn.setTextColor(Color.parseColor("#FF4081"));
            } else {
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextColor(Color.parseColor("#1C2A44"));
            }
        }
        if (selectedButton != btnSortPrice){
            sortState = 0;
            btnSortPrice.setText("Giá ▼");
        }
    }

    private void resetProductList() {
        productModelList.clear();
        productModelList.addAll(originalList);
        adapter.setProducts(new ArrayList<>(productModelList));
        adapter.notifyDataSetChanged();
        loadProducts();
    }

    private void sortProductsDescending() {
        Collections.sort(productModelList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        adapter.setProducts(new ArrayList<>(productModelList));
        adapter.notifyDataSetChanged();
    }

    private void sortProductsAscending() {
        Collections.sort(productModelList, Comparator.comparingDouble(ProductModel::getPrice));
        adapter.setProducts(new ArrayList<>(productModelList));
        adapter.notifyDataSetChanged();
    }

    private void searchProducts(String searchQuery) {
        if (isSearching) return;
        isSearching = true;

        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productModelList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ProductModel p = doc.toObject(ProductModel.class);
                        p.setProductId(doc.getId());
                        if (p.getName() != null &&
                                p.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                            productModelList.add(p);
                        }
                    }

                    adapter.setProducts(new ArrayList<>(productModelList)); // 🔥 cập nhật adapter chính xác
                    adapter.notifyDataSetChanged();

                    if (productModelList.isEmpty()) {
                        Toast.makeText(this, "Không tìm thấy sản phẩm phù hợp!", Toast.LENGTH_SHORT).show();
                    }
                    isSearching = false;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isSearching = false;
                });
    }

    private void loadProducts() {
//        showLoading(true);
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productModelList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ProductModel p = doc.toObject(ProductModel.class);
                        p.setProductId(doc.getId());
                        productModelList.add(p);
                    }

                    adapter.setProducts(new ArrayList<>(productModelList));
//                    showLoading(false);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
//                    showLoading(false);
                    Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

//    private void showLoading(boolean isLoading) {
//        if (isLoading) {
//            progressBar.setVisibility(View.VISIBLE);
//            scrollMain.setVisibility(View.GONE);
//        } else {
//            scrollMain.setVisibility(View.VISIBLE);
//            progressBar.setVisibility(View.GONE);
//        }
//    }

    private void loadNewProducts() {
        db.collection("products")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productModelList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ProductModel p = documentSnapshot.toObject(ProductModel.class);
                        p.setProductId(documentSnapshot.getId());
                        productModelList.add(p);
                    }

                    adapter.setProducts(new ArrayList<>(productModelList));
                    adapter.notifyDataSetChanged();
                    if (productModelList.isEmpty()) {
                        Toast.makeText(this, "Không có sản phẩm mới nào.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải sản phẩm mới nào." + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadHotProducts() {
        db.collection("products")
                .orderBy("salesCount", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productModelList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ProductModel p = documentSnapshot.toObject(ProductModel.class);
                        p.setProductId(documentSnapshot.getId());
                        productModelList.add(p);
                    }

                    adapter.setProducts(new ArrayList<>(productModelList));
                    adapter.notifyDataSetChanged();
                    if (productModelList.isEmpty()) {
                        Toast.makeText(this, "Chưa có sản phẩm bán chạy nào.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải sản phẩm bán chạy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
