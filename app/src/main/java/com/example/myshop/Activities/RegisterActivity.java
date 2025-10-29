package com.example.myshop.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtConfirm;
    Button btnRegister;
    TextView tvBackToLogin;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirm = findViewById(R.id.edtRegisterConfirm);
        btnRegister = findViewById(R.id.btnRegisterSubmit);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            String confirm = edtConfirm.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("id", uid);
                        user.put("email", email);
                        user.put("role", "user"); // mặc định là user, admin set thủ công

                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                                    // Gửi email về LoginActivity
                                    Intent result = new Intent();
                                    result.putExtra("email", email);
                                    setResult(Activity.RESULT_OK, result);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi lưu Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        if (e.getMessage() != null && e.getMessage().contains("already in use")) {
                            Toast.makeText(this, "Email đã tồn tại, vui lòng dùng email khác", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        tvBackToLogin.setOnClickListener(v -> finish());
    }
}
