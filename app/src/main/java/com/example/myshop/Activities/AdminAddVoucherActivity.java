package com.example.myshop.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myshop.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminAddVoucherActivity extends AppCompatActivity {

    // Views
    private EditText etCode, etDiscountValue, etMinOrder, etQuantity;
    private RadioGroup rgDiscountType;
    private Button btnPickDate, btnSaveVoucher;
    private TextView tvSelectedDate;
    private Toolbar toolbar;

    // Variables
    private Calendar expiryCalendar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_voucher);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        expiryCalendar = Calendar.getInstance();

        // Ánh xạ Views
        etCode = findViewById(R.id.etCode);
        etDiscountValue = findViewById(R.id.etDiscountValue);
        etMinOrder = findViewById(R.id.etMinOrder);
        rgDiscountType = findViewById(R.id.rgDiscountType);
        etQuantity = findViewById(R.id.etQuantity);
        btnPickDate = findViewById(R.id.btnPickDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnSaveVoucher = findViewById(R.id.btnSaveVoucher);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());



        // Sự kiện chọn ngày
        btnPickDate.setOnClickListener(v -> showDatePicker());

        // Sự kiện lưu voucher
        btnSaveVoucher.setOnClickListener(v -> saveVoucher());
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            expiryCalendar.set(Calendar.YEAR, year);
            expiryCalendar.set(Calendar.MONTH, month);
            expiryCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Đặt giờ về cuối ngày (23:59:59) để voucher có hiệu lực hết ngày đó
            expiryCalendar.set(Calendar.HOUR_OF_DAY, 23);
            expiryCalendar.set(Calendar.MINUTE, 59);
            expiryCalendar.set(Calendar.SECOND, 59);

            updateDateLabel();
        };

        new DatePickerDialog(
                this,
                dateSetListener,
                expiryCalendar.get(Calendar.YEAR),
                expiryCalendar.get(Calendar.MONTH),
                expiryCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        tvSelectedDate.setText("Hết hạn: " + sdf.format(expiryCalendar.getTime()));
        tvSelectedDate.setTextColor(getResources().getColor(R.color.black)); // Đổi màu chữ khi đã chọn
    }

    private void saveVoucher() {
        String code = etCode.getText().toString().trim().toUpperCase();
        String discountValueStr = etDiscountValue.getText().toString().trim();
        String minOrderStr = etMinOrder.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();

        // 1. Validation (Kiểm tra dữ liệu nhập)
        if (code.isEmpty() || discountValueStr.isEmpty() || minOrderStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tvSelectedDate.getText().toString().contains("Chưa chọn")) {
            Toast.makeText(this, "Vui lòng chọn ngày hết hạn!", Toast.LENGTH_SHORT).show();
            return;
        }

        double discountValue = Double.parseDouble(discountValueStr);
        double minOrderValue = Double.parseDouble(minOrderStr);
        double quantity = Double.parseDouble(quantityStr);
        Date expiryDate = expiryCalendar.getTime();

        // Xác định loại giảm giá
        String type = "amount";
        int selectedId = rgDiscountType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbPercentage) {
            type = "percentage";
        } else if (selectedId == R.id.rbFreeship) {
            type = "freeship";
        }

        // Kiểm tra %
        if (type.equals("percentage") && discountValue > 100) {
            Toast.makeText(this, "Phần trăm giảm giá không được quá 100%", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra số lượng
        if (quantity <= 0 ){
            Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> voucher = new HashMap<>();
        voucher.put("code", code);
        voucher.put("discountType", type);
        voucher.put("discountValue", discountValue);
        voucher.put("minOrderValue", minOrderValue);
        voucher.put("expiryDate", expiryDate);
        voucher.put("quantity", Integer.parseInt(quantityStr));

        // 2. Hiển thị loading khi lưu
        // Tắt nút để tránh bấm nhiều lần
        btnSaveVoucher.setEnabled(false);
        btnSaveVoucher.setText("Đang lưu...");

        // 3. Gửi lên Firestore
        db.collection("vouchers")
                .add(voucher) // Tự động sinh ID document
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thêm Voucher thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình sau khi lưu
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSaveVoucher.setEnabled(true);
                    btnSaveVoucher.setText("Thêm Voucher");
                });
    }
}
