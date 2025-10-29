package com.example.myshop.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.*;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Models.AddressModel;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Adapters.CheckoutAdapter;
import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.*;
import java.util.stream.Collectors;

public class CheckoutActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private LinearLayout layoutAddressSelection;
    private TextView tvUserNameAndPhone, tvUserAddress, tvTotalCheckout;
    private RecyclerView recyclerCheckoutItems;
    private Button btnConfirm;
    private FirebaseFirestore db;
    private String uid;
    private ArrayList<CartModel> checkoutList = new ArrayList<>();
    private CheckoutAdapter adapter;
    private AddressModel selectedAddress;
    private ActivityResultLauncher<Intent> addressLauncher;

    private ArrayList<String> addressList = new ArrayList<>();
    private ArrayAdapter<String> addressAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        toolbar = findViewById(R.id.toolbar);
        tvTotalCheckout = findViewById(R.id.tvTotalCheckout);
        tvUserNameAndPhone = findViewById(R.id.tvUserNameAndPhone);
        tvUserAddress = findViewById(R.id.tvUserAddress);
        layoutAddressSelection = findViewById(R.id.layoutAddressSelection);
        recyclerCheckoutItems = findViewById(R.id.recyclerCheckoutItems);
        btnConfirm = findViewById(R.id.btnConfirm);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!loadProductsFromIntent()) return;
        setupRecyclerView();
        setupAddressLauncher();
        loadDefaultAddress();

        layoutAddressSelection.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressActivity.class);
            addressLauncher.launch(intent);
        });


        btnConfirm.setOnClickListener(v -> confirmOrder());
    }

    private boolean loadProductsFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedProducts")) {
            checkoutList = (ArrayList<CartModel>) intent.getSerializableExtra("selectedProducts");
            if (checkoutList == null || checkoutList.isEmpty()) {
                Toast.makeText(this, "Chọn ít nhất một sản phẩm để thanh toán !!!", Toast.LENGTH_SHORT).show();
                finish();
                return false;
            }
            updateTotalUI();
            return true;
        } else {
            Toast.makeText(this, "Lỗi: Không nhận được danh sách sản phẩm.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
    }


    private void setupAddressLauncher() {
        addressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        AddressModel addressModel = (AddressModel) result.getData().getSerializableExtra("SELECTED_ADDRESS");
                        if (addressModel != null) {
                            this.selectedAddress = addressModel;
                            updateAddressUI(addressModel);
                        }
                    }
                }
        );
    }

    private void updateAddressUI(AddressModel addressModel) {
//        tvUserNameAndPhone.setText(addressModel.getName() + " | " + addressModel.getPhone());
//        tvUserAddress.setText(addressModel.getAddressLine() + ", " + addressModel.getWard() + ", " + addressModel.getDistrict() + ", " + addressModel.getCity());
        tvUserNameAndPhone.setText(String.format("%s | %s", addressModel.getName(), addressModel.getPhone()));
        tvUserAddress.setText(String.format("%s, %s, %s, %s",
                addressModel.getAddressLine(),
                addressModel.getWard(),
                addressModel.getDistrict(),
                addressModel.getCity()));
    }

    private void setupRecyclerView() {
        adapter = new CheckoutAdapter(this, checkoutList);
        recyclerCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerCheckoutItems.setAdapter(adapter);
    }

//    private void loadUserInfo() {
//        db.collection("users").document(uid).get()
//                .addOnSuccessListener(doc -> {
//                    if (doc.exists()) {
//                        String name = doc.getString("name");
//                        String phone = doc.getString("phone");
//                        String address = doc.getString("defaultAddress");
//
//                        if (name != null) edtName.setText(name);
//                        if (phone != null) edtPhone.setText(phone);
//                        if (address != null) edtAddress.setText(address);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Lỗi tải thông tin người dùng: ", e);
//                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    private void loadDefaultAddress() {
        db.collection("users")
                .document(uid)
                .collection("addresses")
                .whereEqualTo("default", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        selectedAddress = doc.toObject(AddressModel.class);
                        if (selectedAddress != null) {
                            selectedAddress.setId(doc.getId());
                            updateAddressUI(selectedAddress);
                        }
                    } else {
                        tvUserNameAndPhone.setText("Chưa có địa chỉ");
                        tvUserAddress.setText("Vui lòng chọn hoặc thêm địa chỉ mới");
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải địa chỉ mặc định: ", e);
                    tvUserAddress.setText("Lỗi tải địa chỉ");
                });
    }
    private void updateTotalUI() {
        double total = 0;
        for (CartModel item : checkoutList) {
            total += item.getPrice() * item.getQuantity();
        }
        // Định dạng tiền tệ cho chuyên nghiệp
        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        tvTotalCheckout.setText(currencyFormat.format(total));
    }
    private void loadCartItems() {
        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    checkoutList.clear();
                    double total = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        CartModel item = doc.toObject(CartModel.class);
                        if (item != null) {
                            checkoutList.add(item);
                            total += item.getPrice() * item.getQuantity();
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvTotalCheckout.setText(String.format("%,.0f ₫", total));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải giỏ hàng: ", e);
                    Toast.makeText(this, "Lỗi tải giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmOrder() {
        if (selectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkoutList.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bắt đầu một WriteBatch để thực hiện nhiều thao tác một cách an toàn
        WriteBatch batch = db.batch();

        // --- 1. Chuẩn bị dữ liệu cho đơn hàng ---
        // Tạo một tham chiếu đến document mới trong collection "orders" CẤP CAO NHẤT
        DocumentReference orderRef = db.collection("users")
                .document(uid)
                .collection("orders")
                .document();
        String orderId = orderRef.getId(); // Lấy ID tự động

        double totalAmount = 0;
        for (CartModel item : checkoutList) {
            totalAmount += item.getPrice() * item.getQuantity();
        }

        // Tạo danh sách chỉ chứa ID sản phẩm để sau này truy vấn nhanh
        List<String> productIds = checkoutList.stream()
                .map(CartModel::getId)
                .collect(Collectors.toList());

        // Tạo đối tượng dữ liệu cho đơn hàng
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", uid);
        orderData.put("customerName", selectedAddress.getName());
        orderData.put("address", tvUserAddress.getText().toString());
        orderData.put("phone", selectedAddress.getPhone());
        orderData.put("items", checkoutList);
        orderData.put("totalAmount", totalAmount);
        orderData.put("timestamp", FieldValue.serverTimestamp()); // Dùng thời gian của server
        orderData.put("status", "Đang xử lý");
        orderData.put("productIds", productIds);

        // THAO TÁC 1: Thêm việc TẠO ĐƠN HÀNG vào batch
        batch.set(orderRef, orderData);


        // --- 2. Cập nhật số lượng bán ("salesCount") cho từng sản phẩm ---
        for (CartModel item : checkoutList) {
            DocumentReference productRef = db.collection("products").document(item.getId());
            // THAO TÁC 2: Thêm việc CẬP NHẬT SỐ LƯỢNG BÁN vào batch
            // Dùng FieldValue.increment để tăng giá trị một cách an toàn
            batch.update(productRef, "salesCount", FieldValue.increment(item.getQuantity()));
        }

        // --- 3. Xóa các sản phẩm trong giỏ hàng ---
        for (CartModel item : checkoutList) {
            DocumentReference cartItemRef = db.collection("users").document(uid).collection("cart").document(item.getId());
            // THAO TÁC 3: Thêm việc XÓA GIỎ HÀNG vào batch
            batch.delete(cartItemRef);
        }

        // --- 4. Thực thi tất cả các thao tác trong batch ---
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Chỉ khi batch commit thành công, tất cả các thao tác trên mới thực sự được áp dụng
                    Log.d(TAG, "Đặt hàng và cập nhật thành công với orderId: " + orderId);
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn hình chính hoặc màn hình theo dõi đơn hàng
                    Intent intent = new Intent(this, OrderTrackingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Đóng màn hình checkout
                })
                .addOnFailureListener(e -> {
                    // Nếu có bất kỳ lỗi nào xảy ra, không có thao tác nào được thực hiện
                    Log.e(TAG, "Lỗi khi thực hiện batch commit: ", e);
                    Toast.makeText(this, "Đặt hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


//    private void showAddressDialog() {
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_address_list, null);
//        ListView lvAddresses = view.findViewById(R.id.lvAddresses);
//        Button btnAddAddress = view.findViewById(R.id.btnAddAddress);
//
//        addressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addressList);
//        lvAddresses.setAdapter(addressAdapter);
//
//        db.collection("users").document(uid).collection("addresses")
//                .get()
//                .addOnSuccessListener(q -> {
//                    addressList.clear();
//                    for (DocumentSnapshot doc : q) {
//                        String addr = doc.getString("addressLine");
//                        if (addr != null) addressList.add(addr);
//                    }
//                    addressAdapter.notifyDataSetChanged();
//                });
//
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle("Chọn địa chỉ giao hàng")
//                .setView(view)
//                .create();
//
//        lvAddresses.setOnItemClickListener((parent, v, pos, id) -> {
//            edtAddress.setText(addressList.get(pos));
//            dialog.dismiss();
//        });
//
//        btnAddAddress.setOnClickListener(v -> showAddAddressDialog(dialog));
//        dialog.show();
//    }

//    private void showAddAddressDialog(AlertDialog parentDialog) {
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);
//        EditText edtNewAddress = view.findViewById(R.id.edtNewAddress);
//
//        new AlertDialog.Builder(this)
//                .setTitle("Thêm địa chỉ mới")
//                .setView(view)
//                .setPositiveButton("Lưu", (dialog, which) -> {
//                    String newAddr = edtNewAddress.getText().toString().trim();
//                    if (newAddr.isEmpty()) {
//                        Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    Map<String, Object> data = new HashMap<>();
//                    data.put("addressLine", newAddr);
//
//                    db.collection("users").document(uid)
//                            .collection("addresses").add(data)
//                            .addOnSuccessListener(d -> {
//                                addressList.add(newAddr);
//                                addressAdapter.notifyDataSetChanged();
//                                edtAddress.setText(newAddr);
//                                parentDialog.dismiss();
//                            });
//                })
//                .setNegativeButton("Hủy", null)
//                .show();
//    }
}
