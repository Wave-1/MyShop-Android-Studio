package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.AdminVoucherAdapter;
import com.example.myshop.Models.VoucherModel;
import com.example.myshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageVouchersActivity extends BaseAdminActivity {

    private RecyclerView recyclerVouchers;
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;
    private List<VoucherModel> voucherList;
    private AdminVoucherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vouchers);

        // Khởi tạo
        db = FirebaseFirestore.getInstance();
        voucherList = new ArrayList<>();

        // Ánh xạ
        recyclerVouchers = findViewById(R.id.recyclerVouchers);
        fabAdd = findViewById(R.id.fabAddVoucher);

        // Setup RecyclerView
        adapter = new AdminVoucherAdapter(this, voucherList, this::confirmDeleteVoucher);
        recyclerVouchers.setLayoutManager(new LinearLayoutManager(this));
        recyclerVouchers.setAdapter(adapter);

        // Sự kiện nút thêm: Chuyển sang màn hình nhập liệu
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAddVoucherActivity.class));
        });

        // Tải dữ liệu
        loadVouchers();
    }

    @Override
    protected int getCurrentMenuId() {
        return R.id.nav_admin_voucher;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh sách khi quay lại từ màn hình thêm mới
        loadVouchers();
    }

    private void loadVouchers() {
        db.collection("vouchers")
                .orderBy("expiryDate") // Sắp xếp theo ngày hết hạn
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    voucherList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        VoucherModel voucher = doc.toObject(VoucherModel.class);
                        if (voucher != null) {
                            voucher.setId(doc.getId()); // Lưu ID để xóa
                            voucherList.add(voucher);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải voucher: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void confirmDeleteVoucher(VoucherModel voucher) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa mã voucher '" + voucher.getCode() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteVoucher(voucher))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteVoucher(VoucherModel voucher) {
        db.collection("vouchers").document(voucher.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa voucher.", Toast.LENGTH_SHORT).show();
                    loadVouchers(); // Tải lại list
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Xóa thất bại.", Toast.LENGTH_SHORT).show());
    }

}
