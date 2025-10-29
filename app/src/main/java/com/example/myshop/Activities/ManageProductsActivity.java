package com.example.myshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Models.ProductModel;
import com.example.myshop.Adapters.ProductAdminAdapter;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManageProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private EditText edtSearchAdmin;
    private Button btnSearchAdmin;
    private FirebaseFirestore db;
    private List<ProductModel> productModelList;
    private List<String> categoryList;
    private ArrayAdapter<String> categoryAdapter;
    private ProductAdminAdapter adapter;
    private BottomNavigationView bottomNav;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product);

        edtSearchAdmin = findViewById(R.id.edtSearchAdmin);
        recyclerProducts = findViewById(R.id.recyclerProductsAd);
        bottomNav = findViewById(R.id.bottomNav);

        findViewById(R.id.btnAddProductAd).setOnClickListener(v -> showAddDialog());

        db = FirebaseFirestore.getInstance();
        productModelList = new ArrayList<>();
        categoryList = new ArrayList<>();

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);
        String email = prefs.getString("email", "Admin");
        edtSearchAdmin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadProducts();
                } else {
                    searchProducts(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button btnManageCategories = findViewById(R.id.btnManageCategories);
        btnManageCategories.setOnClickListener(v -> {
            // Chuyển sang màn hình quản lý danh mục
            Intent intent = new Intent(ManageProductsActivity.this, ManageCategoriesActivity.class);
            startActivity(intent);
        });


        // --- Bottom Navigation ---
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // đang ở trang này
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, ManageUsersActivity.class));
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, ManageCategoriesActivity.class));
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, ManageOrdersActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });


        adapter = new ProductAdminAdapter(this, productModelList, new ProductAdminAdapter.OnItemClickListener() {
            @Override
            public void onEdit(ProductModel productModel) {
                showEditDialog(productModel);
            }

            @Override
            public void onDelete(ProductModel productModel) {

                new AlertDialog.Builder(ManageProductsActivity.this)
                        .setTitle("Xóa sản phẩm")
                        .setMessage("Bạn có chắc muốn xóa " + productModel.getName() + " không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            db.collection("products").document(productModel.getProductId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(ManageProductsActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                        loadProducts();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(ManageProductsActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onItemClick(ProductModel productModel) {
                Intent intent = new Intent(ManageProductsActivity.this, ProductDetailActivity.class);
                intent.putExtra("productId", productModel.getProductId());
                intent.putExtra("role", "admin"); // để hiện Sửa/Xóa
                startActivity(intent);
            }
        });

        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerProducts.setAdapter(adapter);

        loadProducts();
        loadCategories();
        updateExistingProductsWithId();
    }

    private void updateExistingProductsWithId() {
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                WriteBatch batch = db.batch();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Kiểm tra xem trường "id" đã tồn tại chưa
                    if (!document.contains("productId")) {
                        // Nếu chưa, thêm thao tác cập nhật vào batch
                        DocumentReference docRef = db.collection("products").document(document.getId());
                        batch.update(docRef, "productId", document.getId());
                    }
                }
                // Thực hiện tất cả các thao tác cập nhật cùng một lúc
                batch.commit().addOnSuccessListener(aVoid -> {
                    Log.d("UpdateID", "Cập nhật ID cho các sản phẩm cũ thành công!");
                    // Bạn có thể thêm Toast ở đây nếu muốn
                }).addOnFailureListener(e -> {
                    Log.e("UpdateID", "Lỗi khi cập nhật ID hàng loạt: ", e);
                });
            } else {
                Log.e("UpdateID", "Lỗi khi lấy danh sách sản phẩm: ", task.getException());
            }
        });
    }

    private void loadCategories() {
        db.collection("categories").orderBy("name").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        categoryList.add(doc.getString("name"));
                    }
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh mục: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void searchProducts(String searchQuery) {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productModelList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ProductModel p = doc.toObject(ProductModel.class);
                        p.setProductId(doc.getId());
                        if (p.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                            productModelList.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (productModelList.isEmpty()) {
                        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadProducts() {
        db.collection("products")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productModelList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ProductModel p = doc.toObject(ProductModel.class);
                        p.setProductId(doc.getId());
                        productModelList.add(p);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm sản phẩm");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtPrice = view.findViewById(R.id.edtPrice);
        EditText edtImage = view.findViewById(R.id.edtImage);
        EditText edtDescription = view.findViewById(R.id.edtDescription);
        EditText edtSalePercent = view.findViewById(R.id.edtSalePercent);
//        TextView tvSaleStatus = view.findViewById(R.id.tvSaleStatus);

        AutoCompleteTextView spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerCategory.setAdapter(categoryAdapter);

//        ImageButton btnAddCategory = view.findViewById(R.id.btnAddCategory);
//        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        loadCategories();
        edtDescription.setVisibility(View.VISIBLE);

        edtSalePercent.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int sale = 0;
                try {
                    sale = Integer.parseInt(s.toString());
                } catch (NumberFormatException ignored) {
                }
//                if (sale > 0 && sale <= 100) {
//                    tvSaleStatus.setText("Trạng thái sale: Đang Sale (" + sale + "%)");
//                    tvSaleStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
//                } else {
//                    tvSaleStatus.setText("Trạng thái sale: Chưa Sale");
//                    tvSaleStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
//                }
            }
        });

        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String imageUrl = edtImage.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();
            String saleStr = edtSalePercent.getText().toString().trim();

            if (spinnerCategory.getText() == null) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }
            String selectedCategory = spinnerCategory.getText().toString();

            if (name.isEmpty() || priceStr.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            int salePercent = saleStr.isEmpty() ? 0 : Integer.parseInt(saleStr);

//            String id = UUID.randomUUID().toString();
//            ProductModel productModel = new ProductModel(id, name, price, imageUrl, "Mô tả chi tiết", salePercent);
            Map<String, Object> newProduct = new HashMap<>();
            newProduct.put("name", name);
            newProduct.put("price", price);
            newProduct.put("image", imageUrl);
            newProduct.put("description", description);
            newProduct.put("salePercent", salePercent);
            newProduct.put("category", selectedCategory);
            newProduct.put("createAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

            db.collection("products").add(newProduct)
                    .addOnSuccessListener(documentReference -> {
                        String newProductId = documentReference.getId();
                        db.collection("products").document(newProductId)
                                .update("productId", newProductId)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                    loadProducts(); // Tải lại danh sách
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Thêm thành công nhưng lỗi cập nhật ID", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

//    private void showAddCategoryDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Thêm dang mục mới");
//
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
//        EditText edtNewCategoryName = view.findViewById(R.id.edtNewCategoryName);
//
//        builder.setView(view);
//        builder.setPositiveButton("Thêm", (dialog, which) -> {
//            String categoryName = edtNewCategoryName.getText().toString().trim();
//            if (categoryName.isEmpty()) {
//                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            Map<String, Object> categoryData = new HashMap<>();
//            categoryData.put("name", categoryName);
//
//            db.collection("categories").add(categoryData)
//                    .addOnSuccessListener(documentReference -> {
//                        Toast.makeText(this, "Đã thêm danh mục: " + categoryName, Toast.LENGTH_SHORT).show();
//                        loadCategories();
//                    })
//                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e, Toast.LENGTH_SHORT).show());
//        });
//        builder.setNegativeButton("Hủy", null);
//        builder.show();
//    }

    private void showEditDialog(ProductModel productModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa sản phẩm");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtPrice = view.findViewById(R.id.edtPrice);
        EditText edtImage = view.findViewById(R.id.edtImage);
        EditText edtDescription = view.findViewById(R.id.edtDescription);
        EditText edtSalePercent = view.findViewById(R.id.edtSalePercent);

        AutoCompleteTextView spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerCategory.setAdapter(categoryAdapter);
//        ImageButton btnAddCategory = view.findViewById(R.id.btnAddCategory);
//        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        loadCategories();

        edtName.setText(productModel.getName());
        edtPrice.setText(String.valueOf(productModel.getPrice()));
        edtImage.setText(productModel.getImage());

        edtDescription.setText(productModel.getDescription());
        edtSalePercent.setText(String.valueOf(productModel.getSalePercent()));

        if (productModel.getCategory() != null) {
                spinnerCategory.setText(productModel.getCategory(), false);
        }

        builder.setView(view);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String imageUrl = edtImage.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();
            String saleStr = edtSalePercent.getText().toString().trim();

            String selectedCategory = spinnerCategory.getText().toString();
            if (spinnerCategory.getText() == null) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.isEmpty() || priceStr.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            int salePercent = saleStr.isEmpty() ? 0 : Integer.parseInt(saleStr);

            productModel.setName(name);
            productModel.setPrice(price);
            productModel.setImage(imageUrl);
            productModel.setCategory(selectedCategory);
            productModel.setDescription(description);
            productModel.setSalePercent(salePercent);

            db.collection("products").document(productModel.getProductId())
                    .update(
                            "name", name,
                            "price", price,
                            "image", imageUrl,
                            "description", description,
                            "category", selectedCategory,
                            "salePercent", salePercent,
                            "productId", productModel.getProductId()

                    )
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
