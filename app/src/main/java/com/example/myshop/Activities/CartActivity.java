package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Adapters.CartAdapter;
import com.example.myshop.Models.CartModel;
import com.example.myshop.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerCart;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private LinearLayout layoutEmptyCart;
    private ArrayList<CartModel> cartList;
    private CartAdapter cartAdapter;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        if (!checkUserAuthentication()){
            return;
        }

        toolbar = findViewById(R.id.toolbarCart);
        recyclerCart = findViewById(R.id.recyclerCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();


        setupRecyclerView();

        loadCartFromFirestore();

        btnCheckout.setOnClickListener(v -> {
            ArrayList<CartModel> selectedItems = new ArrayList<>();
            for (CartModel item : cartList){
                if (item.isSelected()){
                    selectedItems.add(item);
                }
            }
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống !!!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Intent intent = new Intent(this, CheckoutActivity.class);
                intent.putExtra("selectedProducts", selectedItems);
                startActivity(intent);
            }

        });
    }

    private void setupRecyclerView() {
        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList, this::updateTotalPrice);
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(cartAdapter);
    }

    private boolean checkUserAuthentication() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            return true;
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
    }

    private void loadCartFromFirestore() {
        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cartList.clear();
                    if (querySnapshot.isEmpty()) {
                        showEmptyCart(true);
                    }else {
                        showEmptyCart(false);
                        for (QueryDocumentSnapshot doc : querySnapshot){
                            CartModel item = doc.toObject(CartModel.class);
                            item.setId(doc.getId());
                            cartList.add(item);
                        }
                    }


                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showEmptyCart(boolean show) {
        if (show){
            layoutEmptyCart.setVisibility(View.VISIBLE);
            recyclerCart.setVisibility(View.GONE);
        }else {
            layoutEmptyCart.setVisibility(View.GONE);
            recyclerCart.setVisibility(View.VISIBLE);
        }
    }

    public void updateTotalPrice(){
        double total = 0;
        for (CartModel item : cartList){
            if (item.isSelected()){
                total += item.getPrice() * item.getQuantity();
            }
        }
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(numberFormat.format(total));
    }


}
