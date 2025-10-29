package com.example.myshop.Activities;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.CategoryManageAdapter;
import com.example.myshop.Models.CategoryModel;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageCategoriesActivity extends AppCompatActivity {
    private RecyclerView recyclerCategories;
    private Button btnAddCategory;
    private FirebaseFirestore db;
    private List<CategoryModel> categoryList;
    private CategoryManageAdapter adapter;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        db = FirebaseFirestore.getInstance();
        recyclerCategories = findViewById(R.id.recyclerCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        bottomNav = findViewById(R.id.bottomNav);
        categoryList = new ArrayList<>();
        adapter = new CategoryManageAdapter(this, categoryList, this::confirmDeleteCategory);

        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerCategories.setAdapter(adapter);

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        bottomNav.setSelectedItemId(R.id.nav_categories);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ManageProductsActivity.class));
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, ManageUsersActivity.class));
                return true;
            } else if (id == R.id.nav_categories) {
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
        loadCategories();
    }

    private void loadCategories() {
        db.collection("categories").orderBy("name").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        CategoryModel categoryModel = documentSnapshot.toObject(CategoryModel.class);
                        categoryModel.setId(documentSnapshot.getId());
                        categoryList.add(categoryModel);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh mục.", Toast.LENGTH_SHORT).show());
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm dang mục mới");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText edtNewCategoryName = view.findViewById(R.id.edtNewCategoryName);

        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String categoryName = edtNewCategoryName.getText().toString().trim();
            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("name", categoryName);

            db.collection("categories").add(categoryData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Đã thêm danh mục: " + categoryName, Toast.LENGTH_SHORT).show();
                        loadCategories();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e, Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void confirmDeleteCategory(CategoryModel category) {
        if (category.getName().equals("Chưa phân loại")) {
            Toast.makeText(this, "Không thể xóa danh mục mặc định.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa danh mục '" + category.getName() + "'?\n\nTất cả sản phẩm thuộc danh mục này sẽ được chuyển về 'Chưa phân loại'.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategoryAndUpdateProducts(category))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCategoryAndUpdateProducts(CategoryModel categoryToDelete) {
        WriteBatch batch = db.batch();

        // 1. Lấy tất cả sản phẩm thuộc danh mục sắp xóa
        db.collection("products")
                .whereEqualTo("category", categoryToDelete.getName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // 2. Cập nhật từng sản phẩm sang danh mục "Chưa phân loại"
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.update(doc.getReference(), "category", "Chưa phân loại");
                    }

                    // 3. Xóa document của danh mục đó
                    DocumentReference categoryRef = db.collection("categories").document(categoryToDelete.getId());
                    batch.delete(categoryRef);

                    // 4. Thực thi tất cả các thao tác
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Xóa danh mục thành công.", Toast.LENGTH_SHORT).show();
                                loadCategories(); // Tải lại danh sách
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi xóa.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tìm sản phẩm để cập nhật.", Toast.LENGTH_SHORT).show());
    }
}
