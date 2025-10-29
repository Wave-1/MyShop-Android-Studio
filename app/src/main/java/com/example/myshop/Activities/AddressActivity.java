package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.AddressAdapter;
import com.example.myshop.Models.AddressModel;
import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AddressActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerAddresses;
    private Button btnAddAddress;
    private LinearLayout layoutEmpty;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;
    private AddressAdapter adapter;
    private ArrayList<AddressModel> addressList = new ArrayList<>();
    private ActivityResultLauncher<Intent> addEditAddressLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        toolbar = findViewById(R.id.toolbar);
        recyclerAddresses = findViewById(R.id.recyclerAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupAddEditLauncher();
        setupRecyclerView();
        loadAddresses();

        btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditAddressActivity.class);
            addEditAddressLauncher.launch(intent);
        });


    }

    private void setupAddEditLauncher() {
        addEditAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadAddresses();
                    }
                }

        );
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter(this, addressList, addEditAddressLauncher, addressModel -> {
            Intent intent = new Intent();
            intent.putExtra("SELECTED_ADDRESS", addressModel);
            setResult(RESULT_OK, intent);
            finish();
        });
        recyclerAddresses.setLayoutManager(new LinearLayoutManager(this));
        recyclerAddresses.setAdapter(adapter);
    }

    private void loadAddresses() {
        db.collection("users")
                .document(uid)
                .collection("addresses")
                .orderBy("default", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        AddressModel addressModel = documentSnapshot.toObject(AddressModel.class);
                        addressModel.setId(documentSnapshot.getId());
                        addressList.add(addressModel);
                    }

                    Log.d("Firestore", "Số địa chỉ load được: " + addressList.size());
                    adapter.notifyDataSetChanged();
                    checkIfEmpty();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải địa chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkIfEmpty();
                });
    }

    private void checkIfEmpty() {
        if (addressList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerAddresses.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerAddresses.setVisibility(View.VISIBLE);
        }
    }
}
