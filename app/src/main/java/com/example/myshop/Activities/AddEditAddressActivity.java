package com.example.myshop.Activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myshop.Models.AddressModel;
import com.example.myshop.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddEditAddressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText edtName, edtPhone, edtCity, edtDistrict, edtWard, edtAddressLine;
    private MaterialCheckBox cbSetDefault;
    private Button btnSave, btnDelete;
    private AddressModel existingAddress;
    private boolean isEditMode = false;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_edit_address);
        View rootLayout = findViewById(R.id.rootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            // Lấy khoảng trống của các thanh hệ thống (status bar, navigation bar)
            int systemBarsInsetsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Áp dụng padding cho layout gốc
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            // Trả về insets mặc định để các View con (như AppBarLayout) có thể tự xử lý
            return WindowInsetsCompat.CONSUMED;
        });
        // Ánh xạ
        toolbar = findViewById(R.id.toolbar);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtCity = findViewById(R.id.edtCity);
        edtDistrict = findViewById(R.id.edtDistrict);
        edtWard = findViewById(R.id.edtWard);
        edtAddressLine = findViewById(R.id.edtAddressLine);
        cbSetDefault = findViewById(R.id.cbSetDefault);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        // Cấu hình Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Firebase
        db = FirebaseFirestore.getInstance();
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        if (getIntent().hasExtra("EDIT_ADDRESS")) {
            isEditMode = true;
            existingAddress = (AddressModel) getIntent().getSerializableExtra("EDIT_ADDRESS");
            toolbar.setTitle("Sửa địa chỉ");
            btnDelete.setVisibility(View.VISIBLE);
            populateFields(existingAddress);
        } else {
            isEditMode = false;
            toolbar.setTitle("Địa chỉ mới");
            btnDelete.setVisibility(View.GONE);
        }

        // Sự kiện click
        btnSave.setOnClickListener(v -> saveAddress());
        btnDelete.setOnClickListener(v -> deleteAddress());

    }

    private void deleteAddress() {
        if (existingAddress == null) {
            return;
        }
        if (existingAddress.isDefault()) {
            Toast.makeText(this, "Không thể xóa địa chỉ mặc định", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteAddressFromFirestore();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void deleteAddressFromFirestore() {
        db.collection("users")
                .document(uid)
                .collection("addresses")
                .document(existingAddress.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa địa chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateFields(AddressModel address) {
        edtName.setText(address.getName());
        edtPhone.setText(address.getPhone());
        edtCity.setText(address.getCity());
        edtDistrict.setText(address.getDistrict());
        edtWard.setText(address.getWard());
        edtAddressLine.setText(address.getAddressLine());
        cbSetDefault.setChecked(address.isDefault());
        if (address.isDefault()) {
            cbSetDefault.setEnabled(false);
        }
    }

    private void saveAddress() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String district = edtDistrict.getText().toString().trim();
        String ward = edtWard.getText().toString().trim();
        String addressLine = edtAddressLine.getText().toString().trim();
        boolean isDefault = cbSetDefault.isChecked();

        if (name.isEmpty() || phone.isEmpty() || city.isEmpty() || district.isEmpty() || ward.isEmpty() || addressLine.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> addressData = createAddressMap(name, phone, city, district, ward, addressLine, isDefault);
        if (isDefault) {
            // Nếu người dùng chọn đây là địa chỉ mặc định, chúng ta cần bỏ mặc định ở các địa chỉ khác
            runBatchToSetDefault(addressData);
        } else {
            if (isEditMode) {
                updateExistingAddress(existingAddress.getId(), addressData);
            } else {
                saveNewAddress(addressData);
            }
        }
    }

    private void updateExistingAddress(String docId, Map<String, Object> addressData) {
        db.collection("users")
                .document(uid)
                .collection("addresses")
                .document(docId)
                .update(addressData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Lưu địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    setResultAndFinish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void runBatchToSetDefault(Map<String, Object> saveNewAddress) {
        WriteBatch batch = db.batch();
        // 1. Tìm địa chỉ mặc định hiện tại
        db.collection("users").document(uid).collection("addresses")
                .whereEqualTo("default", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 2. Bỏ trạng thái mặc định của nó
                    for (var doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "default", false);
                    }
                    DocumentReference documentReference;
                    if (isEditMode) {
                        documentReference = db.collection("users")
                                .document(uid)
                                .collection("addresses")
                                .document(existingAddress.getId());
                    } else {
                        documentReference = db.collection("users")
                                .document(uid)
                                .collection("addresses")
                                .document();
                    }
                    batch.set(documentReference, saveNewAddress);
                    commitBatchAndFinish(batch, "Lưu địa chỉ thành công");
                }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void commitBatchAndFinish(WriteBatch batch, String message) {
        batch.commit().addOnSuccessListener(aVoid -> {
            showToast(message);
            setResultAndFinish();
        }).addOnFailureListener(e -> showToast("Lỗi khi lưu địa chỉ: " + e.getMessage()));
    }

    private void setResultAndFinish() {
        setResult(RESULT_OK);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveNewAddress(Map<String, Object> addressData) {
        db.collection("users").document(uid).collection("addresses").add(addressData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Lưu địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    setResultAndFinish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private Map<String, Object> createAddressMap(String name, String phone, String city, String district, String ward, String addressLine, boolean isDefault) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("city", city);
        data.put("district", district);
        data.put("ward", ward);
        data.put("addressLine", addressLine);
        data.put("default", isDefault);
        return data;
    }
}
