package com.example.myshop.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView imgAvatar;
    private FrameLayout btnChangeAvatar;
    private ProgressBar progressBar, progressBarAvatar;
    private View mainContent;
    private TextView tvFullName, tvGender, tvBirthday, tvPhone, tvEmail;
    private LinearLayout layoutName, layoutGender, layoutBirthday, layoutPhone, layoutEmail, layoutChangePassword;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private String userId;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImageToFirebase(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        imgAvatar = findViewById(R.id.img_avatar);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);

        progressBar = findViewById(R.id.progressBar);
        mainContent = findViewById(R.id.mainContent);
        progressBarAvatar = findViewById(R.id.progressBarAvatar);

        tvFullName = findViewById(R.id.tv_full_name);
        tvGender = findViewById(R.id.tv_gender);
        tvBirthday = findViewById(R.id.tv_birthday);
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);

        layoutName = findViewById(R.id.layout_name);
        layoutGender = findViewById(R.id.layout_gender);
        layoutBirthday = findViewById(R.id.layout_birthday);
        layoutPhone = findViewById(R.id.layout_phone);
        layoutEmail = findViewById(R.id.layout_email);
        layoutChangePassword = findViewById(R.id.layout_change_password);

        initFirebase();
        setupToolbar();
        setupEvents();

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (mainContent != null) mainContent.setVisibility(View.GONE);

        loadUserProfile();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupEvents() {
        // 1. Đổi Avatar
        btnChangeAvatar.setOnClickListener(v -> openGallery());

        // 2. Sửa Tên
        layoutName.setOnClickListener(v ->
                showEditDialog("Cập nhật Họ tên", tvFullName.getText().toString(), "name", tvFullName));

        // 3. Sửa Giới tính
        layoutGender.setOnClickListener(v -> showGenderDialog());

        // 4. Sửa Ngày sinh
        layoutBirthday.setOnClickListener(v -> showBirthDialog());

        // 5. Sửa SĐT
        layoutPhone.setOnClickListener(v ->
                showEditDialog("Cập nhật SĐT", tvPhone.getText().toString(), "phoneNumber", tvPhone));

        // 6. Đổi Mật khẩu
        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
        // 7. Email
        layoutEmail.setOnClickListener(v ->
                Toast.makeText(this, "Không thể thay đổi Email đăng nhập", Toast.LENGTH_SHORT).show());
    }

    // --- FIREBASE ---
    private void loadUserProfile() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (mainContent != null) mainContent.setVisibility(View.VISIBLE);
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phoneNumber");
                        String email = documentSnapshot.getString("email");
                        String gender = documentSnapshot.getString("gender");
                        String birthday = documentSnapshot.getString("birthday");
                        String avatarUrl = documentSnapshot.getString("profileImage");

                        tvFullName.setText(name != null && !name.isEmpty() ? name : "Chưa cập nhật");
                        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "Chưa cập nhật");
                        tvEmail.setText(email != null && !email.isEmpty() ? email : mAuth.getCurrentUser().getEmail());
                        tvGender.setText(gender != null && !gender.isEmpty() ? gender : "Chưa cập nhật");
                        tvBirthday.setText(birthday != null && !birthday.isEmpty() ? birthday : "Chưa cập nhật");

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this).load(avatarUrl).into(imgAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (mainContent != null) mainContent.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFirestoreField(String field, String value, TextView targetView) {
        db.collection("users").document(userId)
                .update(field, value)
                .addOnSuccessListener(aVoid -> {
                    if (targetView != null) targetView.setText(value);
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- AVATAR UPLOAD ---

    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    private void uploadImageToFirebase(Uri imageUri) {
        progressBarAvatar.setVisibility(View.VISIBLE);
        imgAvatar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        StorageReference fileRef = storageReference.child("profile_images/" + userId + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();

                    // Lưu URL vào Firestore
                    updateFirestoreField("profileImage", downloadUrl, null);

                    // Hiển thị ngay lập tức
                    progressBarAvatar.setVisibility(View.GONE);
                    imgAvatar.setVisibility(View.VISIBLE);
                    Glide.with(ProfileActivity.this).load(downloadUrl).into(imgAvatar);
                }))
                .addOnFailureListener(e -> {
                    progressBarAvatar.setVisibility(View.GONE);
                    imgAvatar.setVisibility(View.VISIBLE);
                    Log.e("ProfileActivity", "Error uploading image", e);
                });
    }

    private void showEditDialog(String title, String currentValue, String fieldKey, TextView targetView) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_text);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        TextView tvTitle = dialog.findViewById(R.id.tv_title);
        EditText edtValue = dialog.findViewById(R.id.edt_value);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        tvTitle.setText(title);
        if (!currentValue.equals("Chưa cập nhật")) {
            edtValue.setText(currentValue);
        }

        if ("phoneNumber".equals(fieldKey)) {
            edtValue.setInputType(InputType.TYPE_CLASS_PHONE);
        } else {
            edtValue.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        btnConfirm.setOnClickListener(v -> {
            String newValue = edtValue.getText().toString().trim();
            if (newValue.isEmpty()) {
                edtValue.setError("Vui lòng nhập thông tin");
                return;
            }

            if ("phoneNumber".equals(fieldKey) && !Patterns.PHONE.matcher(newValue).matches()) {
                edtValue.setError("Số điện thoại không hợp lệ");
                return;
            }

            updateFirestoreField(fieldKey, newValue, targetView);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void showGenderDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_gender);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        RadioGroup rgGender = dialog.findViewById(R.id.rg_gender);
        RadioButton rbMale = dialog.findViewById(R.id.rb_male);
        RadioButton rbFemale = dialog.findViewById(R.id.rb_female);
        RadioButton rbOther = dialog.findViewById(R.id.rb_other);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm_gender);

        String current = tvGender.getText().toString();
        if (current.equals("Nam")) rbMale.setChecked(true);
        else if (current.equals("Nữ")) rbFemale.setChecked(true);
        else if (current.equals("Khác")) rbOther.setChecked(true);

        btnConfirm.setOnClickListener(v -> {
            int selectedId = rgGender.getCheckedRadioButtonId();
            if (selectedId == -1) return;

            RadioButton selected = dialog.findViewById(selectedId);
            String gender = selected.getText().toString();

            updateFirestoreField("gender", gender, tvGender);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showBirthDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_birth); // Đảm bảo bạn có file layout này

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        NumberPicker npYear = dialog.findViewById(R.id.np_year);
        NumberPicker npMonth = dialog.findViewById(R.id.np_month);
        NumberPicker npDay = dialog.findViewById(R.id.np_day);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm_date);

        // Setup DatePicker
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        npYear.setMinValue(1900);
        npYear.setMaxValue(currentYear);
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npDay.setMinValue(1);
        npDay.setMaxValue(31);

        // Set default value
        npYear.setValue(2000);
        npMonth.setValue(1);
        npDay.setValue(1);

        btnConfirm.setOnClickListener(v -> {
            String date = String.format("%02d/%02d/%04d", npDay.getValue(), npMonth.getValue(), npYear.getValue());
            updateFirestoreField("birthday", date, tvBirthday);
            dialog.dismiss();
        });

        dialog.show();
    }

}
