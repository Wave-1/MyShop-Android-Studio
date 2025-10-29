package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    EditText edtOldPass, edtNewPass, edtConfirmPass;
    Button btnConfirmChange;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        edtOldPass = findViewById(R.id.edtOldPassword);
        edtNewPass = findViewById(R.id.edtNewPassword);
        edtConfirmPass = findViewById(R.id.edtConfirmPassword);
        btnConfirmChange = findViewById(R.id.btnConfirmChange);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        btnConfirmChange.setOnClickListener(v -> {
            String newPass = edtNewPass.getText().toString().trim();
            String confirm = edtConfirmPass.getText().toString().trim();

            if (newPass.isEmpty() || confirm.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirm)){
                Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            user.updatePassword(newPass)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đổi mật khẩu thành công !!!", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Đổi mật khẩu thất bại !!!", Toast.LENGTH_SHORT).show();
                    });
        });

    }
}
