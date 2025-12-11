package com.example.myshop.Activities;

import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.RankConfigAdapter;
import com.example.myshop.Models.RankConfigModel;
import com.example.myshop.R;

import com.example.myshop.Util.CurrencyTextWatcher;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageRankConfigActivity extends BaseAdminActivity {

    private EditText edtShippingFee;
    private RecyclerView recyclerRanks;
    private RankConfigAdapter adapter;
    private List<RankConfigModel> rankList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings); // Layout mới

        db = FirebaseFirestore.getInstance();

        // Ánh xạ
        edtShippingFee = findViewById(R.id.edtShippingFee);
        recyclerRanks = findViewById(R.id.recyclerRanks);
        Button btnSaveConfig = findViewById(R.id.btnSaveConfig);
        Button btnAddRank = findViewById(R.id.btnAddRank);

        // Format tiền
        edtShippingFee.addTextChangedListener(new CurrencyTextWatcher(edtShippingFee));

        // Setup RecyclerView
        rankList = new ArrayList<>();
        adapter = new RankConfigAdapter(rankList, new RankConfigAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged() {
                saveConfig();
            }
        });
        recyclerRanks.setLayoutManager(new LinearLayoutManager(this));
        recyclerRanks.setAdapter(adapter);

        // Load dữ liệu
        loadCurrentConfig();

        // Sự kiện thêm rank mới
        btnAddRank.setOnClickListener(v -> {
            rankList.add(new RankConfigModel("Rank Mới", 0, 0, 0));
            adapter.notifyItemInserted(rankList.size() - 1);
        });

        // Sự kiện lưu
        btnSaveConfig.setOnClickListener(v -> saveConfig());


    }

    @Override
    protected int getCurrentMenuId() {
        return R.id.nav_admin_ranks;
    }

    private void loadCurrentConfig() {
        db.collection("config")
                .document("general")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load Ship
                        Double ship = documentSnapshot.getDouble("shippingFee");
                        if (ship != null) {
                            DecimalFormat format = new DecimalFormat("#,###");
                            edtShippingFee.setText(format.format(ship));
                        }

                        // Load Rank List
                        List<Map<String, Object>> ranksData = (List<Map<String, Object>>) documentSnapshot.get("ranks");
                        if (ranksData != null) {
                            rankList.clear();
                            for (Map<String, Object> data : ranksData) {
                                String name = (String) data.get("name");
                                // Xử lý an toàn kiểu số từ Firestore (Long -> int/double)
                                int targetOrder = ((Long) data.get("targetOrders")).intValue();
                                double targetSpent = Double.parseDouble(data.get("targetSpent").toString());
                                int discount = ((Long) data.get("discountPercent")).intValue();

                                rankList.add(new RankConfigModel(name, targetOrder, targetSpent, discount));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải cấu hình", Toast.LENGTH_SHORT).show());
    }

    private void saveConfig() {
        if (rankList.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm hạng!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Toast.makeText(this, "Đang lưu cấu hình...", Toast.LENGTH_SHORT).show();
            double shippingFee = CurrencyTextWatcher.getDoubleValue(edtShippingFee);

            Map<String, Object> config = new HashMap<>();
            config.put("shippingFee", shippingFee);
            config.put("ranks", rankList);

            db.collection("config")
                    .document("general")
                    .set(config)
                    .addOnSuccessListener(unused ->
                    {
                        Toast.makeText(this, "Đã lưu cấu hình thành công!", Toast.LENGTH_SHORT).show();
                        loadCurrentConfig();
                        hideKeyboard();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng kiểm tra các trường số!", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }
}
