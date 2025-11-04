package com.example.myshop.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvFullName, tvPhone, tvEmail, tvGender, tvBirthday, tvToolbarTitle;
    private ImageView imgEditName, imgEditPhone, imgEditEmail, imgEditGender, imgEditBirthday, imgToolbarBack;
    private FirebaseFirestore db;
    private String userId;
    private String addressId; // 🔹 lưu id của document address mặc định

    private View layoutName, layoutPhone, layoutEmail, layoutGender, layoutBirthday, layoutChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // TextView
        tvFullName = findViewById(R.id.tv_full_name);
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);
        tvGender = findViewById(R.id.tv_gender);
        tvBirthday = findViewById(R.id.tv_birthday);
        imgToolbarBack = findViewById(R.id.img_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);

        // ImageView
        imgEditName = findViewById(R.id.img_edit_name);
        imgEditPhone = findViewById(R.id.img_edit_phone);
        imgEditEmail = findViewById(R.id.img_edit_email);
        imgEditGender = findViewById(R.id.img_edit_gender);
        imgEditBirthday = findViewById(R.id.img_edit_birthday);

        // Layout
        layoutName = findViewById(R.id.layout_name);
        layoutPhone = findViewById(R.id.layout_phone);
        layoutEmail = findViewById(R.id.layout_email);
        layoutGender = findViewById(R.id.layout_gender);
        layoutBirthday = findViewById(R.id.layout_birthday);
        layoutChangePassword = findViewById(R.id.layout_change_password);

        tvToolbarTitle.setText(getString(R.string.account_info));

        imgToolbarBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        });

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) loadUserInfo();

        // Gán sự kiện chỉnh sửa
        View.OnClickListener nameClick = v ->
                showEditDialog("Cập nhật tên đầy đủ", tvFullName.getText().toString(), "name", tvFullName);
        imgEditName.setOnClickListener(nameClick);
        layoutName.setOnClickListener(nameClick);

        View.OnClickListener phoneClick = v ->
                showEditDialog("Cập nhật số điện thoại", tvPhone.getText().toString(), "phone", tvPhone);
        imgEditPhone.setOnClickListener(phoneClick);
        layoutPhone.setOnClickListener(phoneClick);

        View.OnClickListener emailClick = v ->
                showEditDialog("Cập nhật email", tvEmail.getText().toString(), "email", tvEmail);
        imgEditEmail.setOnClickListener(emailClick);
        layoutEmail.setOnClickListener(emailClick);

        View.OnClickListener genderClick = v -> showGenderDialog();
        imgEditGender.setOnClickListener(genderClick);
        layoutGender.setOnClickListener(genderClick);

        View.OnClickListener birthdayClick = v -> showBirthDialog();
        imgEditBirthday.setOnClickListener(birthdayClick);
        layoutBirthday.setOnClickListener(birthdayClick);

        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    // 🔹 Lấy thông tin từ address mặc định
    private void loadUserInfo() {
        db.collection("users").document(userId)
                .collection("addresses")
                .whereEqualTo("default", true)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        addressId = doc.getId();

                        tvFullName.setText(getOrDefault(doc.getString("name")));
                        tvPhone.setText(getOrDefault(doc.getString("phone")));
                        tvEmail.setText(getOrDefault(doc.getString("email"))); // nếu có
                        tvGender.setText(getOrDefault(doc.getString("gender"))); // nếu có
                        tvBirthday.setText(getOrDefault(doc.getString("birthday"))); // nếu có
                    } else {
                        Toast.makeText(this, "Không tìm thấy địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getOrDefault(String value) {
        return (value != null && !value.isEmpty()) ? value : "Chưa thiết lập";
    }

    private void showEditDialog(String title, String currentValue, String field, TextView targetView) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_text);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tv_title);
        EditText edtValue = dialog.findViewById(R.id.edt_value);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        tvTitle.setText(title);
        edtValue.setText(currentValue);

        btnConfirm.setOnClickListener(v -> {
            String newValue = edtValue.getText().toString().trim();
            if (newValue.isEmpty()) {
                edtValue.setError("Vui lòng nhập thông tin");
                return;
            }

            if (addressId == null) {
                Toast.makeText(this, "Không tìm thấy địa chỉ để cập nhật", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(userId)
                    .collection("addresses").document(addressId)
                    .update(field, newValue)
                    .addOnSuccessListener(aVoid -> {
                        targetView.setText(newValue);
                        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

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

        String currentGender = tvGender.getText().toString().trim();
        if (currentGender.equalsIgnoreCase("Nam")) rbMale.setChecked(true);
        else if (currentGender.equalsIgnoreCase("Nữ")) rbFemale.setChecked(true);
        else if (currentGender.equalsIgnoreCase("Khác")) rbOther.setChecked(true);

        btnConfirm.setOnClickListener(v -> {
            int selectedId = rgGender.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selected = dialog.findViewById(selectedId);
            String gender = selected.getText().toString();

            if (addressId == null) {
                Toast.makeText(this, "Không tìm thấy địa chỉ để cập nhật", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(userId)
                    .collection("addresses").document(addressId)
                    .update("gender", gender)
                    .addOnSuccessListener(aVoid -> {
                        tvGender.setText(gender);
                        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showBirthDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_birth);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        NumberPicker npYear = dialog.findViewById(R.id.np_year);
        NumberPicker npMonth = dialog.findViewById(R.id.np_month);
        NumberPicker npDay = dialog.findViewById(R.id.np_day);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm_date);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        npYear.setMinValue(1950);
        npYear.setMaxValue(currentYear);
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npDay.setMinValue(1);
        npDay.setMaxValue(31);

        String currentBirth = tvBirthday.getText().toString().trim();
        if (currentBirth.matches("\\d{2}/\\d{2}/\\d{4}")) {
            try {
                String[] parts = currentBirth.split("/");
                npDay.setValue(Integer.parseInt(parts[0]));
                npMonth.setValue(Integer.parseInt(parts[1]));
                npYear.setValue(Integer.parseInt(parts[2]));
            } catch (Exception e) {
                npYear.setValue(2000);
                npMonth.setValue(1);
                npDay.setValue(1);
            }
        } else {
            npYear.setValue(2000);
            npMonth.setValue(1);
            npDay.setValue(1);
        }

        btnConfirm.setOnClickListener(v -> {
            int day = npDay.getValue();
            int month = npMonth.getValue();
            int year = npYear.getValue();

            String date = String.format("%02d/%02d/%04d", day, month, year);

            if (addressId == null) {
                Toast.makeText(this, "Không tìm thấy địa chỉ để cập nhật", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(userId)
                    .collection("addresses").document(addressId)
                    .update("birthday", date)
                    .addOnSuccessListener(aVoid -> {
                        tvBirthday.setText(date);
                        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            dialog.dismiss();
        });

        dialog.show();
    }
}
