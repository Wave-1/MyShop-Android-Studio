package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.VoucherAdapter;
import com.example.myshop.Models.VoucherModel;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class VoucherActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText etVoucherCode;
    private Button btnApplyVoucher, btnConfirmSelection;
    private ProgressBar progressBar;

    private RecyclerView recyclerFreeship, recyclerDiscount;
    private TextView tvSeeMoreFreeship, tvSeeMoreDiscount;
    private TextView tvEmptyFreeship, tvEmptyDiscount;

    private FirebaseFirestore db;

    // Adapter và List riêng cho từng loại
    private VoucherAdapter adapterFreeship, adapterDiscount;
    private List<VoucherModel> listFreeship = new ArrayList<>();
    private List<VoucherModel> listDiscount = new ArrayList<>();

    private VoucherModel selectedFreeshipVoucher = null;
    private List<VoucherModel> selectedDiscountVouchers = new ArrayList<>();

    private double currentTotalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);

        if (getIntent() != null && getIntent().hasExtra("CURRENT_TOTAL")) {
            currentTotalAmount = getIntent().getDoubleExtra("CURRENT_TOTAL", 0);
        }

        initViews();
        setupFirebase();
        setupRecyclerViews();
        loadVouchersFromFirestore();
        setupEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etVoucherCode = findViewById(R.id.etVoucherCode);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        progressBar = findViewById(R.id.progressBar);

        recyclerFreeship = findViewById(R.id.recyclerFreeship);
        recyclerDiscount = findViewById(R.id.recyclerDiscount);

        tvSeeMoreFreeship = findViewById(R.id.tvSeeMoreFreeship);
        tvSeeMoreDiscount = findViewById(R.id.tvSeeMoreDiscount);

        btnConfirmSelection = findViewById(R.id.btnConfirmSelection);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        recyclerFreeship.setLayoutManager(new LinearLayoutManager(this));
        recyclerFreeship.setNestedScrollingEnabled(false);

        adapterFreeship = new VoucherAdapter(this, listFreeship, voucher -> {
            if (voucher.isSelected()) {
                for (int i = 0; i < listFreeship.size(); i++) {
                    VoucherModel v = listFreeship.get(i);
                    if (v != voucher && v.isSelected()) {
                        v.setSelected(false);
                        adapterFreeship.notifyItemChanged(i);
                    }
                }
                selectedFreeshipVoucher = voucher;
            } else {
                if (selectedFreeshipVoucher != null && selectedFreeshipVoucher.getId().equals(voucher.getId())) {
                    selectedFreeshipVoucher = null;
                }
            }
        });
        recyclerFreeship.setAdapter(adapterFreeship);

        recyclerDiscount.setLayoutManager(new LinearLayoutManager(this));
        recyclerDiscount.setNestedScrollingEnabled(false);

        adapterDiscount = new VoucherAdapter(this, listDiscount, voucher -> {

            if (voucher.isSelected()) {
                selectedDiscountVouchers.add(voucher);
            } else {
                selectedDiscountVouchers.removeIf(v -> v.getId().equals(voucher.getId()));
            }
        });
        recyclerDiscount.setAdapter(adapterDiscount);
    }

    private void setupEvents() {
        btnApplyVoucher.setOnClickListener(v -> applyVoucherFromInput());

        tvSeeMoreFreeship.setOnClickListener(v -> {
            boolean isCurrentlyExpanded = adapterFreeship.isExpanded();

            if (isCurrentlyExpanded) {
                adapterFreeship.setExpanded(false);
                tvSeeMoreFreeship.setText("Xem thêm");
            } else {
                adapterFreeship.setExpanded(true);
                tvSeeMoreFreeship.setText("Thu gọn");
            }
        });

        tvSeeMoreDiscount.setOnClickListener(v -> {
            boolean isCurrentlyExpanded = adapterDiscount.isExpanded();

            if (isCurrentlyExpanded) {
                adapterDiscount.setExpanded(false);
                tvSeeMoreDiscount.setText("Xem thêm");
            } else {
                // Đang đóng -> Mở rộng ra
                adapterDiscount.setExpanded(true);
                tvSeeMoreDiscount.setText("Thu gọn");
            }
        });

        btnConfirmSelection.setOnClickListener(v -> {
            Intent resultIntent = new Intent();

            // 1. Trả về Freeship
            if (selectedFreeshipVoucher != null) {
                if (currentTotalAmount < selectedFreeshipVoucher.getMinOrderValue()) {
                    Toast.makeText(this, "Đơn chưa đủ điều kiện dùng mã FreeShip: " + selectedFreeshipVoucher.getCode(), Toast.LENGTH_SHORT).show();
                    return;
                }
                resultIntent.putExtra("SELECTED_FREESHIP", selectedFreeshipVoucher);
            }

            if (!selectedDiscountVouchers.isEmpty()) {
                for (VoucherModel voucherModel : selectedDiscountVouchers) {
                    if (currentTotalAmount < voucherModel.getMinOrderValue()) {
                        Toast.makeText(this, "Đơn chưa đủ điều kiện dùng mã: " + voucherModel.getCode(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                resultIntent.putExtra("SELECTED_DISCOUNTS_LIST", (ArrayList<VoucherModel>) selectedDiscountVouchers);
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void loadVouchersFromFirestore() {
        showLoading(true);

        db.collection("vouchers")
                .whereGreaterThan("expiryDate", Calendar.getInstance().getTime())
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        listFreeship.clear();
                        listDiscount.clear();

                        // 1. Lấy dữ liệu cũ từ Intent (nếu có)
                        VoucherModel oldFreeship = null;
                        ArrayList<VoucherModel> oldDiscounts = new ArrayList<>();

                        if (getIntent() != null) {
                            if (getIntent().hasExtra("OLD_SELECTED_FREESHIP")) {
                                oldFreeship = (VoucherModel) getIntent().getSerializableExtra("OLD_SELECTED_FREESHIP");
                                selectedFreeshipVoucher = oldFreeship;
                            }
                            if (getIntent().hasExtra("OLD_SELECTED_DISCOUNTS")) {
                                oldDiscounts = (ArrayList<VoucherModel>) getIntent().getSerializableExtra("OLD_SELECTED_DISCOUNTS");
                                selectedDiscountVouchers = new ArrayList<>(oldDiscounts);
                            }
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            VoucherModel voucher = document.toObject(VoucherModel.class);
                            voucher.setId(document.getId());
                            voucher.setSelected(false);

                            if (oldFreeship != null && voucher.getId().equals(oldFreeship.getId())) {
                                voucher.setSelected(true);
                                selectedFreeshipVoucher = voucher;
                            }

//                            if (voucher.getQuantity() <= 0){
//                                continue;
//                            }
                            voucher.setId(document.getId());
                            for (VoucherModel oldV : oldDiscounts) {
                                if (oldV.getId().equals(voucher.getId())) {
                                    voucher.setSelected(true);
                                    break;
                                }
                            }

                            // Phân loại vào 2 danh sách
                            if ("freeship".equals(voucher.getDiscountType())) {
                                listFreeship.add(voucher);
                            } else {
                                listDiscount.add(voucher);
                            }
                        }

                        // Sắp xếp voucher ngon nhất lên đầu
                        sortVouchers(listFreeship);
                        sortVouchers(listDiscount);

                        adapterFreeship.notifyDataSetChanged();
                        adapterDiscount.notifyDataSetChanged();

                        // Kiểm tra hiển thị nút "Xem thêm"
                        tvSeeMoreFreeship.setVisibility(listFreeship.size() > 1 ? View.VISIBLE : View.GONE);
                        tvSeeMoreDiscount.setVisibility(listDiscount.size() > 1 ? View.VISIBLE : View.GONE);

                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Hàm sắp xếp: Ưu tiên giảm giá nhiều nhất
    private void sortVouchers(List<VoucherModel> list) {
        Collections.sort(list, (v1, v2) -> Double.compare(v2.getDiscountValue(), v1.getDiscountValue()));
    }

    private void applyVoucherFromInput() {
        String code = etVoucherCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Kiểm tra xem mã có nằm trong danh sách Freeship đã tải không
        for (int i = 0; i < listFreeship.size(); i++) {
            VoucherModel v = listFreeship.get(i);
            if (v.getCode().equalsIgnoreCase(code)) {
                // Logic chọn Freeship (Chỉ chọn 1)
                for (VoucherModel item : listFreeship) item.setSelected(false); // Bỏ chọn hết cái cũ
                v.setSelected(true);
                selectedFreeshipVoucher = v;

                adapterFreeship.notifyDataSetChanged(); // Cập nhật giao diện
                Toast.makeText(this, "Đã áp dụng mã Freeship: " + code, Toast.LENGTH_SHORT).show();
                return; // Kết thúc hàm
            }
        }

        // 2. Kiểm tra xem mã có nằm trong danh sách Discount đã tải không
        for (int i = 0; i < listDiscount.size(); i++) {
            VoucherModel v = listDiscount.get(i);
            if (v.getCode().equalsIgnoreCase(code)) {
                // Logic chọn Discount (Chọn nhiều)
                if (!v.isSelected()) {
                    v.setSelected(true);
                    selectedDiscountVouchers.add(v);
                    adapterDiscount.notifyItemChanged(i); // Cập nhật đúng dòng đó
                    Toast.makeText(this, "Đã thêm mã giảm giá: " + code, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Mã này đã được chọn rồi", Toast.LENGTH_SHORT).show();
                }
                return; // Kết thúc hàm
            }
        }


        showLoading(true);
        db.collection("vouchers")
                .whereEqualTo("code", code)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        VoucherModel voucher = document.toObject(VoucherModel.class);

                        if (voucher != null) {
                            voucher.setId(document.getId());

                            if (voucher.getExpiryDate() != null && voucher.getExpiryDate().before(Calendar.getInstance().getTime())) {
                                Toast.makeText(this, "Mã này đã hết hạn", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if ("freeship".equals(voucher.getDiscountType())) {
                                // Xử lý thêm vào listFreeship nếu chưa có (Tuỳ chọn)
                                selectedFreeshipVoucher = voucher;
                                Toast.makeText(this, "Đã chọn mã FreeShip: " + voucher.getCode(), Toast.LENGTH_SHORT).show();
                            } else {
                                // --- SỬA LỖI Ở ĐÂY ---
                                // Thêm vào danh sách thay vì gán đè
                                boolean exists = false;
                                for(VoucherModel v : selectedDiscountVouchers) {
                                    if(v.getId().equals(voucher.getId())) exists = true;
                                }

                                if(!exists) {
                                    voucher.setSelected(true);
                                    selectedDiscountVouchers.add(voucher); // Thêm vào list
                                    Toast.makeText(this, "Đã chọn mã Giảm giá: " + voucher.getCode(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Mã này đã được chọn", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Mã không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
