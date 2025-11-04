package com.example.myshop.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.ProductAdapter;
import com.example.myshop.Models.ProductModel;
import com.example.myshop.R;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "FavoritesActivity";
    private RecyclerView recyclerFavorites;
    private ProductAdapter productAdapter;
    private ArrayList<ProductModel> favoriteProductsList;
    private TextView tvNoFavorites;
    private MaterialToolbar toolbar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        toolbar = findViewById(R.id.toolbar);
        recyclerFavorites = findViewById(R.id.recyclerFavorites);
        tvNoFavorites = findViewById(R.id.tvNoFavorites);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        favoriteProductsList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, favoriteProductsList);

        recyclerFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerFavorites.setAdapter(productAdapter);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (currentUser != null) {
            loadFavoriteProducts();
        } else {
            tvNoFavorites.setText("Vui lòng đăng nhập để xem sản phẩm đã thích.");
            tvNoFavorites.setVisibility(View.VISIBLE);
            recyclerFavorites.setVisibility(View.GONE);
        }
    }

    private void loadFavoriteProducts() {
        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoFavorites.setVisibility(View.VISIBLE);
                        recyclerFavorites.setVisibility(View.GONE);
                        return;
                    }
                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot favoriteDoc : queryDocumentSnapshots) {
                        String productId = favoriteDoc.getId();
                        Task<DocumentSnapshot> productTask = db.collection("products")
                                .document(productId)
                                .get();
                        tasks.add(productTask);
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
                        favoriteProductsList.clear();
                        for (Object result : objects) {
                            DocumentSnapshot productDoc = (DocumentSnapshot) result;
                            if (productDoc.exists()) {
                                ProductModel productModel = productDoc.toObject(ProductModel.class);
                                if (productModel != null) {
                                    favoriteProductsList.add(productModel);
                                }
                            }
                        }

                        productAdapter.notifyDataSetChanged();
                        if (favoriteProductsList.isEmpty()) {
                            tvNoFavorites.setVisibility(View.VISIBLE);
                        } else {
                            tvNoFavorites.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi tải sản phẩm yêu thích: ", e);
                        Toast.makeText(FavoritesActivity.this, "Không thể tải chi tiết sản phẩm.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải danh sách yêu thích: ", e);
                    Toast.makeText(FavoritesActivity.this, "Không thể tải danh sách yêu thích.", Toast.LENGTH_SHORT).show();
                });
    }

}
