package com.example.myshop.Activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.OrderDetailAdapter;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        TextView tvDetailTotalAmount = findViewById(R.id.tvDetailTotalAmount);

        OrderModel order = (OrderModel) getIntent().getSerializableExtra("ORDER_DETAIL");
        if (order != null) {
            tvDetailTotalAmount.setText(String.format(Locale.getDefault(), "%,.0f ₫", order.getTotalAmount()));
            recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
            OrderDetailAdapter adapter = new OrderDetailAdapter(this, order.getItems());
            recyclerOrderItems.setAdapter(adapter);
        }
    }
}
