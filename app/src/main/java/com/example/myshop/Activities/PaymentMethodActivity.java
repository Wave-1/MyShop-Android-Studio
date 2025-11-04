package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myshop.Adapters.PaymentMethodAdapter;
import com.example.myshop.Models.PaymentMethod;
import com.example.myshop.R;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodActivity extends AppCompatActivity {

    private List<PaymentMethod> listPayment = new ArrayList<>();
    private PaymentMethodAdapter adapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        // 1. Toolbar với nút back
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Phương thức thanh toán");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 2. RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rcv_payment_method);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Dữ liệu
        listPayment.add(new PaymentMethod(1, "COD", "Thanh toán khi nhận hàng"));
        listPayment.add(new PaymentMethod(2, "Thẻ tín dụng", "Visa, MasterCard"));
        listPayment.add(new PaymentMethod(3, "Chuyển khoản", "Ngân hàng Vietcombank"));
        listPayment.add(new PaymentMethod(4, "ZaloPay", "Thanh toán qua ZaloPay"));

        // 4. Adapter
        adapter = new PaymentMethodAdapter(listPayment, method -> {
            for (PaymentMethod m : listPayment) m.setSelected(false);
            method.setSelected(true);
            adapter.notifyDataSetChanged();

            Intent result = new Intent();
            result.putExtra("SELECTED_PAYMENT_METHOD", method.getName());
            setResult(RESULT_OK, result);
            finish();
        });

        recyclerView.setAdapter(adapter);
    }
}
