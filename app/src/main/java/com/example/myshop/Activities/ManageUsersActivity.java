package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Models.UserModel;
import com.example.myshop.R;
import com.example.myshop.Adapters.UserAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    UserAdapter adapter;
    List<UserModel> userList;
    FirebaseFirestore db;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        recyclerView = findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomNav = findViewById(R.id.bottomNav);
        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        bottomNav.setSelectedItemId(R.id.nav_users);
        // --- Bottom Navigation ---
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ManageProductsActivity.class));
                return true; // đang ở trang này
            } else if (id == R.id.nav_users) {
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

        loadUsers();
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String email = doc.getString("email");
                        String role = doc.getString("role");
                        userList.add(new UserModel(id, email, role));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}
