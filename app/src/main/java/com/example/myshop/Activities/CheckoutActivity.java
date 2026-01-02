package com.example.myshop.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.CheckoutAdapter;
import com.example.myshop.Models.CheckoutViewModel;
import com.example.myshop.Util.Constants;
import com.example.myshop.Models.AddressModel;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.Models.VoucherModel;
import com.example.myshop.R;
import com.example.myshop.Util.PaymentsUtil;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.button.ButtonOptions;
import com.google.android.gms.wallet.button.PayButton;
import com.google.android.gms.wallet.contract.TaskResultContracts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CheckoutActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private LinearLayout layoutAddressSelection, layoutPaymentMethod, layoutVoucherSelection;
    private View layoutDiscountAmount, layoutShippingDiscount;
    private RelativeLayout layoutRankDiscount;
    private TextView tvUserNameAndPhone, tvUserAddress, tvTotalCheckout, tvSelectedPaymentMethod, tvSelectedVoucher, tvDiscountAmount, tvShippingFee, tvShippingDiscount, tvRankDiscountLabel, tvRankDiscountAmount;
    private Button btnConfirm;
    private RecyclerView recyclerCheckoutItems;
    private FirebaseFirestore db;
    private String uid;
    private ArrayList<CartModel> checkoutList = new ArrayList<>();
    private CheckoutAdapter adapter;
    private AddressModel selectedAddress;
    private CheckoutViewModel model;

    // Biến xử lý Voucher & Tiền
    private VoucherModel selectedFreeship;
    private ArrayList<VoucherModel> selectedDiscounts = new ArrayList<>();
    private ActivityResultLauncher<Intent> addressLauncher, paymentLauncher, voucherLauncher;
    private String selectedPaymentMethod = "Thanh toán khi nhận hàng (COD)";

    private String currentRankName = "THÀNH VIÊN MỚI";
    private double rankDiscountPercent = 0; // % giảm giá của rank
    private double rankDiscountAmount = 0;  // Số tiền giảm thực tế
    private double totalProductAmount = 0; // Tổng tiền hàng
    private double shippingFee = 30000;    // Phí ship mặc định
    private double finalAmount = 0;        // Tổng thanh toán cuối cùng

    // Google Pay Launcher
    private final ActivityResultLauncher<Task<PaymentData>> paymentDataLauncher =
            registerForActivityResult(new TaskResultContracts.GetPaymentDataResult(), result -> {
                int statusCode = result.getStatus().getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.SUCCESS:
                        handlePaymentSuccess(result.getResult());
                        break;
                    //case CommonStatusCodes.CANCELED: The user canceled
                    case CommonStatusCodes.DEVELOPER_ERROR:
                        handleError(statusCode, result.getStatus().getStatusMessage());
                        break;
                    default:
                        handleError(statusCode, "Unexpected non API" +
                                " exception when trying to deliver the task result to an activity!");
                        break;
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Khởi tạo ViewModel của Google Pay
        model = new ViewModelProvider(this).get(CheckoutViewModel.class);
        model.canUseGooglePay.observe(this, this::setGooglePayAvailable);

        initViews();
        setupFirebase();
        setupToolbar();
        setupLaunchers();

        if (!loadProductsFromIntent()) return;

        setupRecyclerView();
        loadDefaultAddress();
        loadUserRankConfig();
        loadShippingFeeFromConfig();
        setupEvents();

        updateCheckoutUI();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTotalCheckout = findViewById(R.id.tvTotalCheckout);
        tvUserNameAndPhone = findViewById(R.id.tvUserNameAndPhone);
        tvUserAddress = findViewById(R.id.tvUserAddress);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvShippingDiscount = findViewById(R.id.tvShippingDiscount);

        layoutShippingDiscount = findViewById(R.id.layoutShippingDiscount);
        layoutAddressSelection = findViewById(R.id.layoutAddressSelection);
        layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
        layoutVoucherSelection = findViewById(R.id.layoutVoucherSelection);

        layoutDiscountAmount = findViewById(R.id.layoutDiscountAmount);
        tvDiscountAmount = findViewById(R.id.tvDiscountAmount);

        layoutRankDiscount = findViewById(R.id.layoutRankDiscount);
        tvRankDiscountLabel = findViewById(R.id.tvRankDiscountLabel);
        tvRankDiscountAmount = findViewById(R.id.tvRankDiscountAmount);

        tvSelectedPaymentMethod = findViewById(R.id.tvSelectedPaymentMethod);
        tvSelectedVoucher = findViewById(R.id.tvSelectedVoucher);

        recyclerCheckoutItems = findViewById(R.id.recyclerCheckoutItems);
        btnConfirm = findViewById(R.id.btnConfirm);

    }

    private boolean isGooglePayAvailable = false;

    private void setGooglePayAvailable(boolean available) {
        this.isGooglePayAvailable = available;
    }

    public void requestPayment(View view) {
        if (selectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ trước khi thanh toán Google Pay", Toast.LENGTH_SHORT).show();
            return;
        }
        if (finalAmount < 0) finalAmount = 0;
        long priceForGooglePay = (long) Math.round(finalAmount);
        // The price provided to the API should include taxes and shipping.
        final Task<PaymentData> task = model.getLoadPaymentDataTask(priceForGooglePay);
        task.addOnCompleteListener(paymentDataLauncher::launch);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupEvents() {
        layoutAddressSelection.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressActivity.class);
            addressLauncher.launch(intent);
        });

        layoutPaymentMethod.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentMethodActivity.class);
            intent.putExtra("CURRENT_METHOD", selectedPaymentMethod);
            paymentLauncher.launch(intent);
        });

        layoutVoucherSelection.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, VoucherActivity.class);
            // Gửi tổng tiền hàng sang để VoucherActivity kiểm tra điều kiện
            intent.putExtra("CURRENT_TOTAL", totalProductAmount);
            if (selectedFreeship != null) {
                intent.putExtra("OLD_SELECTED_FREESHIP", selectedFreeship);
            }
            if (selectedDiscounts != null && !selectedDiscounts.isEmpty()) {
                intent.putExtra("OLD_SELECTED_DISCOUNTS", selectedDiscounts);
            }
            voucherLauncher.launch(intent);
        });

        btnConfirm.setOnClickListener(v -> {
            if ("Google Pay".equals(selectedPaymentMethod)) {
                // Kiểm tra xem máy có hỗ trợ Google Pay không trước khi gọi
                if (isGooglePayAvailable) {
                    requestPayment(v);
                } else {
                    Toast.makeText(this, "Google Pay chưa sẵn sàng hoặc không được hỗ trợ trên thiết bị này", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Các phương thức khác (COD, Banking)
                confirmOrder();
            }
        });
    }

    private void setupLaunchers() {
        // 1. Launcher nhận địa chỉ
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

        // 2. Launcher nhận phương thức thanh toán
        paymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String method = result.getData().getStringExtra("SELECTED_PAYMENT_METHOD");
                        if (method != null) {
                            selectedPaymentMethod = method;
                            if ("Google Pay".equals(method)) {
                                tvSelectedPaymentMethod.setText("Google Pay");
                            } else if ("Banking".equals(method)) {
                                tvSelectedPaymentMethod.setText("Chuyển khoản ngân hàng / QR");
                            } else {
                                tvSelectedPaymentMethod.setText("Thanh toán khi nhận hàng (COD)");
                            }
                            updatePaymentButtonVisibility();
                        }
                    }
                }
        );

        // 3. Launcher nhận Voucher (Freeship + Discount List)
        voucherLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // Lấy Freeship
                        if (data.hasExtra("SELECTED_FREESHIP")) {
                            selectedFreeship = (VoucherModel) data.getSerializableExtra("SELECTED_FREESHIP");
                        } else {
                            selectedFreeship = null; // Người dùng bỏ chọn
                        }

                        // Lấy Discount List
                        if (data.hasExtra("SELECTED_DISCOUNTS_LIST")) {
                            selectedDiscounts = (ArrayList<VoucherModel>) data.getSerializableExtra("SELECTED_DISCOUNTS_LIST");
                        } else {
                            selectedDiscounts.clear(); // Người dùng bỏ chọn hết
                        }

                        // Tính toán lại tiền
                        updateCheckoutUI();
                    }
                }
        );
    }

    private void updatePaymentButtonVisibility() {
        btnConfirm.setVisibility(View.VISIBLE);
        if ("Google Pay".equals(selectedPaymentMethod)) {
            btnConfirm.setText("Thanh toán Google Pay");
        } else if ("Banking".equals(selectedPaymentMethod)) {
            btnConfirm.setText("Tiếp tục thanh toán");
        } else {
            btnConfirm.setText("Đặt hàng");
        }
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

            // Tính tổng tiền hàng gốc ban đầu
            totalProductAmount = 0;
            for (CartModel item : checkoutList) {
                totalProductAmount += item.getPrice() * item.getQuantity();
            }
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
        if (uid == null) return;
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

    // --- GỌI HÀM LOAD RANK ---
    private void loadUserRankConfig() {
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("currentRank"))
                            currentRankName = documentSnapshot.getString("currentRank");
                    }
                    fetchRankDiscountPercent(currentRankName);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi tải rank: ", e));
    }

    private void fetchRankDiscountPercent(String rankName) {
        db.collection("config")
                .document("general")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> ranks = (List<Map<String, Object>>) documentSnapshot.get("ranks");
                        if (ranks != null) {
                            for (Map<String, Object> rank : ranks) {
                                String name = (String) rank.get("name");
                                if (name != null && name.equalsIgnoreCase(rankName)) {
                                    Object discountObj = rank.get("discountPercent");
                                    if (discountObj instanceof Long) {
                                        rankDiscountPercent = ((Long) discountObj).doubleValue();
                                    } else if (discountObj instanceof Double) {
                                        rankDiscountPercent = (Double) discountObj;
                                    }
                                    break;
                                }
                            }
                        }
                        updateCheckoutUI();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi lấy cấu hình Rank: ", e));
    }

    private void loadShippingFeeFromConfig() {
        db.collection("config")
                .document("general")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double fee = documentSnapshot.getDouble("shippingFee");
                        if (fee != null) {
                            shippingFee = fee;
                        } else {
                            shippingFee = 30000;
                        }
                        updateCheckoutUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải phí vận chuyển: ", e);
                    updateCheckoutUI();
                });

    }


    // --- HÀM TÍNH TOÁN GIÁ & HIỂN THỊ VOUCHER ---
    private void updateCheckoutUI() {
        double totalProductDiscount = 0; // Tiền giảm giá sản phẩm
        double totalShippingDiscount = 0; // Tiền giảm giá vận chuyển
        rankDiscountAmount = 0;
        StringBuilder voucherNames = new StringBuilder();

        // --- 1. TÍNH TOÁN FREESHIP (Giảm giá vận chuyển) ---
        if (selectedFreeship != null) {
            voucherNames.append(selectedFreeship.getCode()).append(", ");

            double discount = selectedFreeship.getDiscountValue();

            // Logic: Không giảm quá phí ship thực tế (VD: ship 30k, mã giảm 50k -> chỉ giảm 30k)
            if (discount > shippingFee) {
                discount = shippingFee;
            }
            totalShippingDiscount = discount;
        }

        // --- 2. TÍNH TOÁN DISCOUNT (Giảm giá sản phẩm) ---
        if (selectedDiscounts != null && !selectedDiscounts.isEmpty()) {
            for (VoucherModel v : selectedDiscounts) {
                voucherNames.append(v.getCode()).append(", ");

                double discount = 0;
                if ("percentage".equals(v.getDiscountType())) {
                    discount = totalProductAmount * (v.getDiscountValue() / 100);
                    if (v.getMaxDiscountValue() > 0 && discount > v.getMaxDiscountValue()) {
                        discount = v.getMaxDiscountValue();
                    }
                } else {
                    discount = v.getDiscountValue();
                }
                totalProductDiscount += discount;
            }
        }

        // --- 3.TÍNH TOÁN GIẢM GIÁ TỪ RANK ---
        if (rankDiscountPercent > 0) {
            rankDiscountAmount = totalProductAmount * (rankDiscountPercent / 100);
        }

        // --- 4. CẬP NHẬT GIAO DIỆN ---
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // A. Hiển thị Phí vận chuyển (Cố định hoặc tính toán trước đó)
        tvShippingFee.setText(formatter.format(shippingFee));

        // B. Hiển thị Giảm giá vận chuyển
        if (totalShippingDiscount > 0) {
            layoutShippingDiscount.setVisibility(View.VISIBLE);
            tvShippingDiscount.setText("-" + formatter.format(totalShippingDiscount));
        } else {
            layoutShippingDiscount.setVisibility(View.GONE);
        }

        // C. Hiển thị Giảm giá sản phẩm (Voucher giảm giá)
        if (totalProductDiscount > 0) {
            layoutDiscountAmount.setVisibility(View.VISIBLE);
            tvDiscountAmount.setText("-" + formatter.format(totalProductDiscount));
        } else {
            layoutDiscountAmount.setVisibility(View.GONE);
        }

        // D. Hiển thị tên các mã đã chọn
        if (rankDiscountAmount > 0 && layoutRankDiscount != null) {
            layoutRankDiscount.setVisibility(View.VISIBLE);
            // Hiển thị text: Ưu đãi Vàng (-5%):
            tvRankDiscountLabel.setText("Ưu đãi " + currentRankName + " (-" + (int) rankDiscountPercent + "%):");
            tvRankDiscountAmount.setText("-" + formatter.format(rankDiscountAmount));
        } else if (layoutRankDiscount != null) {
            layoutRankDiscount.setVisibility(View.GONE);
        }

        // E. Hiển thị tên các mã đã chọn
        if (voucherNames.length() > 0) {
            String names = voucherNames.substring(0, voucherNames.length() - 2); // Xóa dấu phẩy cuối
            tvSelectedVoucher.setText(names);
            tvSelectedVoucher.setTextColor(Color.parseColor("#673AB7")); // Màu tím
        } else {
            tvSelectedVoucher.setText("Chọn hoặc nhập mã");
            tvSelectedVoucher.setTextColor(Color.parseColor("#555555"));
        }

        // --- 4. TÍNH TỔNG THANH TOÁN CUỐI CÙNG ---
        // Công thức: (Tiền hàng + Ship) - (Giảm ship + Giảm hàng + Giảm rank)
        finalAmount = (totalProductAmount + shippingFee) - (totalShippingDiscount + totalProductDiscount + rankDiscountAmount);

        if (finalAmount < 0) finalAmount = 0;

        tvTotalCheckout.setText(formatter.format(finalAmount));
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

        // Tạo batch để ghi nhiều thao tác cùng lúc
        WriteBatch batch = db.batch();

        DocumentReference orderRef = db.collection("users")
                .document(uid)
                .collection("orders")
                .document();
        String orderId = orderRef.getId();

        List<String> productIds = checkoutList.stream()
                .map(CartModel::getProductId)
                .collect(Collectors.toList());

        OrderModel newOrder = new OrderModel();
        newOrder.setOrderId(orderId);
        newOrder.setUserId(uid);
        newOrder.setAddress(selectedAddress);
        newOrder.setItems(checkoutList);

        // QUAN TRỌNG: Lưu số tiền cuối cùng (đã trừ voucher)
        newOrder.setTotalAmount(finalAmount);
        newOrder.setTimestamp(null);
        newOrder.setStatus(Constants.ORDER_STATUS_PROCESSING);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", newOrder.getOrderId());
        orderData.put("userId", newOrder.getUserId());
        orderData.put("address", newOrder.getAddress());
        orderData.put("items", newOrder.getItems());
        orderData.put("totalAmount", newOrder.getTotalAmount());
        orderData.put("paymentMethod", selectedPaymentMethod);
        orderData.put("status", newOrder.getStatus());
        orderData.put("productIds", productIds);
        orderData.put("timestamp", FieldValue.serverTimestamp());

        orderData.put("rankApplied", currentRankName);
        orderData.put("rankDiscountAmount", rankDiscountAmount);
        orderData.put("rankDiscountPercent", rankDiscountPercent);

        batch.set(orderRef, orderData);

        if (selectedFreeship != null) {
            DocumentReference freeShipRef = db.collection("vouchers").document(selectedFreeship.getId());
            batch.update(freeShipRef, "quantity", FieldValue.increment(-1));
        }

        if (selectedDiscounts != null && !selectedDiscounts.isEmpty()) {
            for (VoucherModel voucher : selectedDiscounts) {
                DocumentReference discountRef = db.collection("vouchers").document(voucher.getId());
                batch.update(discountRef, "quantity", FieldValue.increment(-1));
            }
        }

        for (CartModel item : checkoutList) {
            DocumentReference productRef = db.collection("products").document(item.getProductId());
            batch.update(productRef, "salesCount", FieldValue.increment(item.getQuantity()));

            DocumentReference cartItemRef = db.collection("users").document(uid).collection("cart").document(item.getProductId());
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

    private void handlePaymentSuccess(PaymentData paymentData) {
        Toast.makeText(this, "Thanh toán Google Pay thành công!", Toast.LENGTH_SHORT).show();
        confirmOrder();
    }

    private void handleError(int statusCode, @Nullable String message) {
        Log.e("loadPaymentData failed",
                String.format(Locale.getDefault(), "Error code: %d, Message: %s", statusCode, message));
    }
}
