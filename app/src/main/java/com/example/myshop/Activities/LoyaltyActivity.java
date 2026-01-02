package com.example.myshop.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myshop.Models.RankConfigModel;
import com.example.myshop.R;
import com.example.myshop.Util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoyaltyActivity extends AppCompatActivity {

    private TextView tvUserName, tvRankTitle, tvTotalOrders, tvTotalSpent, btnViewHistory, tvDetailRankName, tvDetailBenefits, tvDetailCondition;
    private ProgressBar progressOrder, progressSpent, progressBarLoading;
    private View scrollMain;
    private ConstraintLayout layoutRankBackground;

    private AppCompatButton btnRankMember, btnRankSilver, btnRankGold, btnRankDiamond;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String uid;
    private FirebaseAuth mAuth;
    private List<RankConfigModel> rankConfigList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loyalty);

        initViews();

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            loadSystemConfig();
        }

        btnViewHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng lịch sử đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvUserName = findViewById(R.id.tvUserName);
        tvRankTitle = findViewById(R.id.tvRankTitle);

        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        btnViewHistory = findViewById(R.id.btnViewHistory);

        progressOrder = findViewById(R.id.progressOrder);
        progressSpent = findViewById(R.id.progressSpent);

        layoutRankBackground = findViewById(R.id.layoutRankBackground);

        tvDetailRankName = findViewById(R.id.tvDetailRankName);
        tvDetailBenefits = findViewById(R.id.tvDetailBenefits);
        tvDetailCondition = findViewById(R.id.tvDetailCondition);

        btnRankMember = findViewById(R.id.btnRankMember);
        btnRankSilver = findViewById(R.id.btnRankSilver);
        btnRankGold = findViewById(R.id.btnRankGold);
        btnRankDiamond = findViewById(R.id.btnRankDiamond);

        progressBarLoading = findViewById(R.id.progressBarLoading);
        scrollMain = findViewById(R.id.scrollMain);
    }

    private void loadSystemConfig() {
        progressBarLoading.setVisibility(View.VISIBLE);
        scrollMain.setVisibility(View.GONE);
        db.collection("config").document("general").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> ranksData = (List<Map<String, Object>>) documentSnapshot.get("ranks");

                        if (ranksData != null) {
                            rankConfigList.clear();
                            for (Map<String, Object> data : ranksData) {
                                String name = (String) data.get("name");
                                // Parse an toàn để tránh lỗi kiểu dữ liệu
                                int targetOrder = ((Long) data.get("targetOrders")).intValue();
                                double targetSpent = Double.parseDouble(data.get("targetSpent").toString());
                                int discount = ((Long) data.get("discountPercent")).intValue();

                                rankConfigList.add(new RankConfigModel(name, targetOrder, targetSpent, discount));
                            }

                            // Sắp xếp Rank từ thấp đến cao (theo chi tiêu hoặc số đơn) để dễ tính toán
                            Collections.sort(rankConfigList, (r1, r2) -> Double.compare(r1.getTargetSpent(), r2.getTargetSpent()));
                        }
                    }
                    // Sau khi có config thì mới tải thông tin user để tính toán
                    setupRankButtons();
                    loadUserData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải cấu hình rank: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Vẫn load user dù lỗi config (sẽ hiển thị mặc định)
                    loadUserData();
                });
    }

    private void setupRankButtons() {
        btnRankMember.setOnClickListener(v -> {
            updateButtonState(btnRankMember);
            showRankDetailsByName("Thành viên mới");
        });

        btnRankSilver.setOnClickListener(v -> {
            updateButtonState(btnRankSilver);
            showRankDetailsByName(Constants.RANK_NAME_SILVER);
        });

        btnRankGold.setOnClickListener(v -> {
            updateButtonState(btnRankGold);
            showRankDetailsByName(Constants.RANK_NAME_GOLD);
        });

        btnRankDiamond.setOnClickListener(v -> {
            updateButtonState(btnRankDiamond);
            showRankDetailsByName(Constants.RANK_NAME_DIAMOND);
        });

        btnRankMember.performClick();
    }

    private void updateButtonState(AppCompatButton selectedBtn) {
        setButtonStyle(btnRankMember, false);
        setButtonStyle(btnRankSilver, false);
        setButtonStyle(btnRankGold, false);
        setButtonStyle(btnRankDiamond, false);

        // Set Active cho nút được chọn
        setButtonStyle(selectedBtn, true);
    }

    private void showRankDetailsByName(String rankName) {
        tvDetailRankName.setText("Tên hạng: " + rankName);
        String benefits = "";
        String condition = "";
        String nameCheck = rankName.toUpperCase();

        if (nameCheck.contains("KIM CƯƠNG") || nameCheck.contains("DIAMOND")) {
            benefits = "• 1 mã miễn phí vận chuyển hàng tháng.\n" +
                    "• Ưu đãi ngày hội thành viên vào lúc 9h sáng ngày 10 và 20 hàng tháng.\n" +
                    "• Ưu đãi độc quyền từ thương hiệu và đối tác.\n" +
                    "• Ưu đãi độc quyền từ thương hiệu và người bán trên Shopee.\n" +
                    "• Voucher HOT từ shop.\n" +
                    "• Voucher thăng hạng.\n" +
                    "• Voucher sinh nhật.\n" +
                    "• Voucher duy trì thứ hạng Kim Cương.";
            condition = "Điều kiện: 100 đơn hàng HOẶC chi tiêu 50.000.000đ"; // Bạn có thể sửa số cứng này
        } else if (nameCheck.contains("VÀNG") || nameCheck.contains("GOLD")) {
            benefits = "• 1 mã miễn phí vận chuyển hàng tháng.\n" +
                    "• Ưu đãi ngày hội thành viên vào lúc 9h sáng ngày 10 và 20 hàng tháng.\n" +
                    "• Ưu đãi độc quyền từ thương hiệu và đối tác.\n" +
                    "• Ưu đãi độc quyền từ thương hiệu và người bán trên Shopee.\n" +
                    "• Voucher HOT từ shop.\n" +
                    "• Voucher thăng hạng.\n" +
                    "• Voucher sinh nhật.";
            condition = "Điều kiện: 50 đơn hàng HOẶC chi tiêu 10.000.000đ";
        } else if (nameCheck.contains("BẠC") || nameCheck.contains("SILVER")) {
            benefits = "• 1 mã miễn phí vận chuyển hàng tháng.\n" +
                    "• Ưu đãi ngày hội thành viên vào lúc 9h sáng ngày 10 và 20 hàng tháng.\n" +
                    "• Ưu đãi độc quyền từ thương hiệu và đối tác.\n" +
                    "• Ưu đãi độc quyền từ thương hiệu và người bán trên Shopee.\n" +
                    "• Voucher HOT từ shop.\n" +
                    "• Shopee Rewards.";
            condition = "Điều kiện:  đơn hàng HOẶC chi tiêu 2.000.000đ";
        } else {
            // Mặc định là Thành viên
            benefits = "• 1 mã miễn phí vận chuyển hàng tháng.\n" +
                    "• Ưu đãi ngày hội thành viên vào lúc 9h sáng ngày 10 và 20 hàng tháng.";
            condition = "Điều kiện: Đăng ký tài khoản thành công";
        }

        tvDetailBenefits.setText(benefits);
        tvDetailCondition.setText(condition);
    }

    private void setButtonStyle(AppCompatButton btn, boolean isActive) {
        if (isActive) {
            btn.setBackgroundResource(R.drawable.filter_selected);
            btn.setTextColor(Color.parseColor("#FF4081"));
        } else {
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setTextColor(Color.parseColor("#1C2A44"));
        }
    }

    private void loadUserData() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Nếu người dùng chưa đăng nhập, không thực hiện bất kỳ hành động nào
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            // Có thể chuyển về màn hình đăng nhập
            // startActivity(new Intent(this, LoginActivity.class));
            // finish();
            return;
        }
        // Gán lại uid để đảm bảo nó không null
        uid = currentUser.getUid();

        progressBarLoading.setVisibility(View.VISIBLE);
        scrollMain.setVisibility(View.GONE);

        // ✅ 2. THAY ĐỔI TRUY VẤN: Lấy thẳng từ document user thay vì sub-collection
        // Truy vấn này hiệu quả hơn và lấy được tên gốc từ document user
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDocument -> {
                    if (userDocument.exists()) {
                        String name = userDocument.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvUserName.setText(name);
                        } else {
                            // Tên dự phòng là DisplayName từ tài khoản Google/Firebase
                            String displayName = currentUser.getDisplayName();
                            if (displayName != null && !displayName.isEmpty()) {
                                tvUserName.setText(displayName);
                            } else {
                                // Tên dự phòng cuối cùng là Email
                                tvUserName.setText(currentUser.getEmail());
                            }
                        }
                    } else {
                        // Trường hợp không tìm thấy document user
                        tvUserName.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "Người dùng");
                    }

                    // ✅ 3. SAU KHI LẤY TÊN THÀNH CÔNG, TIẾP TỤC TẢI CÁC ĐƠN HÀNG
                    loadUserOrdersAndCalculateRank();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBarLoading.setVisibility(View.GONE);
                });
//        progressBarLoading.setVisibility(View.VISIBLE);
//        scrollMain.setVisibility(View.GONE);
//        db.collection("users")
//                .document(uid)
//                .collection("addresses")
//                .whereEqualTo("default", true)
//                .limit(1)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    String nameFromAddress = null;
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        // Lấy document địa chỉ đầu tiên tìm thấy
//                        nameFromAddress = queryDocumentSnapshots.getDocuments().get(0).getString("name");
//                    }
//                    if (nameFromAddress != null && !nameFromAddress.isEmpty()) {
//                        tvUserName.setText(nameFromAddress);
//                    } else {
//                        String displayName = currentUser.getDisplayName();
//                        if (displayName != null && !displayName.isEmpty()) {
//                            tvUserName.setText(displayName);
//                        } else {
//                            tvUserName.setText("Người dùng");
//                        }
//                    }
//                });


    }

    private void loadUserOrdersAndCalculateRank() {
        db.collection("users")
                .document(uid)
                .collection("orders")
                .whereEqualTo("status", "Hoàn thành")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalOrders = 0;
                    double totalSpent = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        totalOrders++;
                        if (doc.contains("totalAmount")) {
                            totalSpent += doc.getDouble("totalAmount");
                        }
                    }
                    calculateAndShowRank(totalOrders, totalSpent);
                })
                .addOnFailureListener(e -> {
                    Log.e("Loyalty", "Lỗi tải đơn hàng" + e);
                    progressBarLoading.setVisibility(View.GONE);
                    scrollMain.setVisibility(View.GONE);
                });

    }

    private void calculateAndShowRank(int userOrders, double userSpent) {
        progressBarLoading.setVisibility(View.GONE);
        scrollMain.setVisibility(View.VISIBLE);

        if (rankConfigList.isEmpty()) {
            tvRankTitle.setText("Chưa có cấu hình hạng");
            return;
        }

        RankConfigModel currentRank = null;
        RankConfigModel nextRank = null;
        int currentDiscount = 0;
        // 1. Tìm Rank hiện tại (Duyệt từ cao xuống thấp sẽ dễ tìm rank cao nhất đạt được)
        // Nhưng ở đây ta duyệt từ thấp lên cao để tìm rank hiện tại và rank kế tiếp

        // Mặc định là rank thấp nhất (hoặc chưa có rank)
        String currentRankName = "THÀNH VIÊN MỚI";

        for (int i = 0; i < rankConfigList.size(); i++) {
            RankConfigModel rank = rankConfigList.get(i);
            boolean isReached = userOrders >= rank.getTargetOrders() && userSpent >= rank.getTargetSpent();
            // Kiểm tra xem user có đạt rank này không
            if (isReached) {
                currentRank = rank;
                currentRankName = rank.getName();
                currentDiscount = rank.getDiscountPercent();
            } else {
                nextRank = rank;
                break;
            }
        }

        saveUserRank(currentRankName, currentDiscount);

        // 2. Xác định mục tiêu hiển thị lên Progress Bar
        int targetOrdersDisplay;
        double targetSpentDisplay;

        if (nextRank != null) {
            // Chưa max cấp -> Mục tiêu là cấp tiếp theo
            targetOrdersDisplay = nextRank.getTargetOrders();
            targetSpentDisplay = nextRank.getTargetSpent();
        } else {
            // Đã max cấp (không tìm thấy nextRank) -> Mục tiêu là chính nó (full cây)
            targetOrdersDisplay = userOrders;
            targetSpentDisplay = userSpent;
            if (currentRank != null) {
                // Có thể set text đặc biệt cho max cấp
                currentRankName += " (MAX)";
            }
        }

        // 3. Cập nhật UI
        if (currentRankName.equalsIgnoreCase("THÀNH VIÊN MỚI")) {
            // Nếu chưa có rank (mặc định) -> Chỉ hiện "THÀNH VIÊN MỚI"
            tvRankTitle.setText("THÀNH VIÊN MỚI");
        } else {
            // Nếu đã có rank -> Hiện "THÀNH VIÊN + [TÊN RANK]"
            tvRankTitle.setText("THÀNH VIÊN " + currentRankName.toUpperCase());
        }

        // Logic đổi màu/background dựa trên tên rank (bạn có thể tùy chỉnh thêm)
        updateRankStyle(currentRankName);

        // 4. Update Progress Bar
        updateProgressSection(tvTotalOrders, progressOrder, userOrders, targetOrdersDisplay, "đơn");
        updateProgressSectionMoney(tvTotalSpent, progressSpent, userSpent, targetSpentDisplay);
    }

    private void saveUserRank(String rankName, int rankDiscount) {
        if (uid == null) return;
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("currentRank", rankName);
        updateData.put("rankDiscount", rankDiscount);

        db.collection("users")
                .document(uid)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Loyalty", "Đã cập nhật rank" + rankName);
                })
                .addOnFailureListener(e -> {
                    Log.e("Loyalty", "Lỗi cập nhật rank", e);
                });
    }

    // Hàm phụ để đổi màu nền
    private void updateRankStyle(String rankName) {
        String nameUpper = rankName.toUpperCase();
        if (nameUpper.contains(Constants.RANK_NAME_DIAMOND)) {
            tvRankTitle.setTextColor(Color.parseColor("#006064"));
            layoutRankBackground.setBackgroundResource(R.drawable.bg_rank_diamond);
        } else if (nameUpper.contains(Constants.RANK_NAME_GOLD)) {
            tvRankTitle.setTextColor(Color.parseColor("#795548"));
            layoutRankBackground.setBackgroundResource(R.drawable.bg_rank_gold);
        } else if (nameUpper.contains(Constants.RANK_NAME_SILVER)) {
            tvRankTitle.setTextColor(Color.parseColor("#616161"));
            layoutRankBackground.setBackgroundResource(R.drawable.bg_rank_silver);
        } else {
            // Mặc định
            tvRankTitle.setTextColor(Color.BLACK);
            layoutRankBackground.setBackgroundResource(R.drawable.bg_no_rank); // Hoặc ảnh mặc định
        }
    }

    private void updateProgressSection(TextView tv, ProgressBar pb, int current, int target, String unit) {
        if (current >= target && target > 0) {
            tv.setText(current + " " + unit);
            pb.setProgress(100);
        } else {
            String currentStr = String.valueOf(current);
            String targetStr = " / " + target;
            SpannableString span = new SpannableString(currentStr + targetStr);
            int color = (current >= target) ? Color.GREEN : Color.RED;

            span.setSpan(new ForegroundColorSpan(color), 0, currentStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.BLACK), currentStr.length(), span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(span);
            int percent = (target == 0) ? 0 : (int) ((float) current / target * 100);
            pb.setProgress(percent);
        }
    }

    private void updateProgressSectionMoney(TextView tv, ProgressBar pb, double current, double target) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        if (Math.abs(current - target) < 1 && target > 0) {
            tv.setText(fmt.format(current));
            pb.setProgress(100);
        } else {
            String currentStr = formatShortMoney(current);
            String targetStr = " / " + formatShortMoney(target);
            SpannableString span = new SpannableString(currentStr + targetStr);
            int color = (current >= target) ? Color.GREEN : Color.RED;
            span.setSpan(new ForegroundColorSpan(color), 0, currentStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.BLACK), currentStr.length(), span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(span);
            int percent = (target == 0) ? 0 : (int) ((float) current / target * 100);
            pb.setProgress(percent);
        }
    }

    private String formatShortMoney(double amount) {
        if (amount >= 1000000000) return String.format(Locale.US, "%.1f tỷ", amount / 1000000000);
        if (amount >= 1000000) {
            if (amount % 1000000 == 0) return String.format(Locale.US, "%.0f tr", amount / 1000000);
            return String.format(Locale.US, "%.1f tr", amount / 1000000);
        }
        return String.format(Locale.US, "%.0f k", amount / 1000);
    }
}
