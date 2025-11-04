package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.EmailAuthProvider;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextView tvToolbarTitle;
    private ImageView imgToolbarBack;
    private EditText edtOldPassword, edtNewPassword, edtConfirmPassword;
    private Button btnChangePassword;

    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Ánh xạ View
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack = findViewById(R.id.img_toolbar_back);
        edtOldPassword = findViewById(R.id.edt_old_password);
        edtNewPassword = findViewById(R.id.edt_new_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);

        // Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Thiết lập tiêu đề Toolbar
        tvToolbarTitle.setText(getString(R.string.account_change_password));

        // Nút quay lại
        imgToolbarBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePasswordActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        });

        // Xử lý khi nhấn nút "Thay đổi mật khẩu"
        btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String oldPass = edtOldPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(oldPass)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu cũ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPass)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Không xác định được người dùng hiện tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xác thực lại người dùng bằng mật khẩu cũ trước khi đổi mật khẩu
        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPass))
                .addOnSuccessListener(aVoid -> {
                    // Nếu xác thực lại thành công → cập nhật mật khẩu mới
                    user.updatePassword(newPass)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Đổi mật khẩu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
                });
    }
}
