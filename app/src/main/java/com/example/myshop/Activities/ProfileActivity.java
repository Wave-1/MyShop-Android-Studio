package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.ProfileAdapter;
import com.example.myshop.Models.ProfileModel;
import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText edtName, edtAddress, edtPhone, edtEmail;
    private Button btnSave, btnChangePassword;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        edtName = findViewById(R.id.edtName);
        edtAddress =findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        btnSave = findViewById(R.id.btnSave);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user != null){
            edtEmail.setText(user.getEmail());
            docRef = db.collection("users").document(user.getUid());
            loadUserData();
        }

        btnSave.setOnClickListener(v -> saveUserData());
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void saveUserData() {
        String name = edtName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if(name.isEmpty() || address.isEmpty() || phone.isEmpty()){
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("defaultAddress", address);
        updates.put("phone", phone);

        docRef.update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show());
    }

    private void loadUserData() {
        docRef.get().addOnSuccessListener(command -> {
            if(command.exists()){
                edtName.setText(command.getString("name"));
                edtAddress.setText(command.getString("defaultAddress"));
                edtPhone.setText(command.getString("phone"));
            }
        });
    }
}
