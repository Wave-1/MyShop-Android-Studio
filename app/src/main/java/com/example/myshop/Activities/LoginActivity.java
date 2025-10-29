package com.example.myshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView tvRegister;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Nếu nhận email từ RegisterActivity → tự điền
        String preEmail = getIntent().getStringExtra("email");
        if (preEmail != null) {
            edtEmail.setText(preEmail);
        }

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check đăng nhập Firebase
            auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(document -> {
                                    if (document.exists()) {
                                        String role = document.getString("role");

                                        // 🔹 Lưu email vào SharedPreferences
                                        SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("userRole", role);
                                        editor.putBoolean("isLoggedIn", true);
                                        editor.putString("email", email);
                                        editor.apply();

                                        if ("admin".equals(role)) {
                                            Toast.makeText(this, "Xin chào Admin!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, ManageProductsActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Xin chào: " + email, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this, HomeActivity.class);
                                            intent.putExtra("email", email); // gửi email sang HomeActivity
                                            startActivity(intent);
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Không tìm thấy thông tin user trong Firestore", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("Firestore", "Lỗi Firestore: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu !!! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
