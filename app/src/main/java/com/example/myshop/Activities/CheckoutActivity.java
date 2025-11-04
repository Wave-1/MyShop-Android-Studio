package com.example.myshop.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.CheckoutAdapter;
import com.example.myshop.Constants;
import com.example.myshop.Models.AddressModel;
import com.example.myshop.Models.CartModel;
import com.example.myshop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;
import java.util.stream.Collectors;

public class CheckoutActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private LinearLayout layoutAddressSelection, layoutPaymentMethod;
    private TextView tvUserNameAndPhone, tvUserAddress, tvTotalCheckout, tvSelectedPaymentMethod;
    private RecyclerView recyclerCheckoutItems;
    private Button btnConfirm;
    private FirebaseFirestore db;
    private String uid;
    private ArrayList<CartModel> checkoutList = new ArrayList<>();
    private CheckoutAdapter adapter;
    private AddressModel selectedAddress;
    private ActivityResultLauncher<Intent> addressLauncher, paymentLauncher;
    private String selectedPaymentMethod = "Thanh toán khi nhận hàng (COD)";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        toolbar = findViewById(R.id.toolbar);
        tvTotalCheckout = findViewById(R.id.tvTotalCheckout);
        tvUserNameAndPhone = findViewById(R.id.tvUserNameAndPhone);
        tvUserAddress = findViewById(R.id.tvUserAddress);
        layoutAddressSelection = findViewById(R.id.layoutAddressSelection);
        layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
        tvSelectedPaymentMethod = findViewById(R.id.tvSelectedPaymentMethod);
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
        setupLaunchers();
        loadDefaultAddress();

        layoutAddressSelection.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressActivity.class);
            addressLauncher.launch(intent);
        });

        layoutPaymentMethod.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentMethodActivity.class);
            paymentLauncher.launch(intent);
        });

        btnConfirm.setOnClickListener(v -> confirmOrder());
    }

    private void setupLaunchers() {
        addressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        AddressModel addressModel = (AddressModel) result.getData().getSerializableExtra("SELECTED_ADDRESS");
                        if (addressModel != null) {
                            selectedAddress = addressModel;
                            updateAddressUI(addressModel);
                        }
                    }
                }
        );

        paymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String method = result.getData().getStringExtra("SELECTED_PAYMENT_METHOD");
                        if (method != null) {
                            selectedPaymentMethod = method;
                            tvSelectedPaymentMethod.setText(method);
                        }
                    }
                }
        );
    }

    private boolean loadProductsFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedProducts")) {
            checkoutList = (ArrayList<CartModel>) intent.getSerializableExtra("selectedProducts");
            if (checkoutList == null || checkoutList.isEmpty()) {
                Toast.makeText(this, "Chọn ít nhất một sản phẩm để thanh toán!", Toast.LENGTH_SHORT).show();
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

    private void setupRecyclerView() {
        adapter = new CheckoutAdapter(this, checkoutList);
        recyclerCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerCheckoutItems.setAdapter(adapter);
    }

    private void loadDefaultAddress() {
        db.collection("users")
                .document(uid)
                .collection("addresses")
                .whereEqualTo("default", true)
                .limit(1)
                .get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) {
                        DocumentSnapshot doc = q.getDocuments().get(0);
                        selectedAddress = doc.toObject(AddressModel.class);
                        if (selectedAddress != null) {
                            selectedAddress.setId(doc.getId());
                            updateAddressUI(selectedAddress);
                        }
                    } else {
                        tvUserNameAndPhone.setText("Chưa có địa chỉ");
                        tvUserAddress.setText("Vui lòng chọn hoặc thêm địa chỉ mới");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi tải địa chỉ mặc định: ", e));
    }

    private void updateAddressUI(AddressModel addressModel) {
        tvUserNameAndPhone.setText(String.format("%s | %s", addressModel.getName(), addressModel.getPhone()));
        tvUserAddress.setText(String.format("%s, %s, %s, %s",
                addressModel.getAddressLine(),
                addressModel.getWard(),
                addressModel.getDistrict(),
                addressModel.getCity()));
    }

    private void updateTotalUI() {
        double total = 0;
        for (CartModel item : checkoutList) total += item.getPrice() * item.getQuantity();
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        tvTotalCheckout.setText(nf.format(total));
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

        double totalAmount = 0;
        for (CartModel item : checkoutList) totalAmount += item.getPrice() * item.getQuantity();

        // Tạo batch để ghi nhiều thao tác cùng lúc
        WriteBatch batch = db.batch();

        DocumentReference orderRef = db.collection("users")
                .document(uid)
                .collection("orders")
                .document();
        String orderId = orderRef.getId();

        List<String> productIds = checkoutList.stream()
                .map(CartModel::getId)
                .collect(Collectors.toList());

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", uid);
        orderData.put("customerName", selectedAddress.getName());
        orderData.put("address", tvUserAddress.getText().toString());
        orderData.put("phone", selectedAddress.getPhone());
        orderData.put("items", checkoutList);
        orderData.put("totalAmount", totalAmount);
        orderData.put("paymentMethod", selectedPaymentMethod);
        orderData.put("timestamp", FieldValue.serverTimestamp());
        orderData.put("status", Constants.ORDER_STATUS_PROCESSING);
        orderData.put("productIds", productIds);

        batch.set(orderRef, orderData);

        for (CartModel item : checkoutList) {
            DocumentReference productRef = db.collection("products").document(item.getId());
            batch.update(productRef, "salesCount", FieldValue.increment(item.getQuantity()));

            DocumentReference cartItemRef = db.collection("users").document(uid).collection("cart").document(item.getId());
            batch.delete(cartItemRef);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, OrderTrackingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi thực hiện batch commit: ", e);
                    Toast.makeText(this, "Đặt hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
