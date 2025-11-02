package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Models.CartModel;
import com.example.myshop.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final Context context;
    private final ArrayList<CartModel> cartList;
    private FirebaseFirestore db;
    private String uid;
    private final boolean isReadOnly;

    public interface OnTotalPriceUpdateListener {
        void onUpdate();
    }

    private final OnTotalPriceUpdateListener totalPriceUpdateListener;

    public CartAdapter(Context context, ArrayList<CartModel> cartList, boolean isReadOnly, OnTotalPriceUpdateListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.isReadOnly = isReadOnly;
        this.totalPriceUpdateListener = listener;
        if (!isReadOnly) {
            this.db = FirebaseFirestore.getInstance();
            this.uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        }
    }

    public CartAdapter(Context context, ArrayList<CartModel> cartList, OnTotalPriceUpdateListener listener) {
        this(context, cartList, false, listener);
    }

    @NonNull
    @Override
    public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        CartModel item = cartList.get(position);
        if (item == null) return;
        holder.tvProductName.setText(item.getName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvProductPrice.setText(currencyFormat.format(item.getPrice()));

        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.store)
                .into(holder.ivProductImage);

        if (isReadOnly) {
            holder.cbSelectProduct.setVisibility(View.GONE);
            holder.btnIncrease.setVisibility(View.GONE);
            holder.btnDecrease.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.tvQuantity.setText("x " + item.getQuantity());
        } else {
            holder.cbSelectProduct.setVisibility(View.VISIBLE);
            holder.btnIncrease.setVisibility(View.VISIBLE);
            holder.btnDecrease.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.tvQuantity.setText("x " + item.getQuantity());
            holder.cbSelectProduct.setChecked(item.isSelected());
            holder.cbSelectProduct.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (totalPriceUpdateListener != null) {
                    totalPriceUpdateListener.onUpdate();
                }
            });
            holder.btnIncrease.setOnClickListener(v -> {
                int currentQuantity = item.getQuantity();
                currentQuantity++;
                updateQuantityInFirestore(item, currentQuantity, holder);
            });

            holder.btnDecrease.setOnClickListener(v -> {
                int currentQuantity = item.getQuantity();
                if (currentQuantity > 1) {
                    currentQuantity--;
                    updateQuantityInFirestore(item, currentQuantity, holder);
                } else {
                    showDeleteConfirmationDialog(item, holder.getAdapterPosition());
                }
            });
            holder.btnDelete.setOnClickListener(v -> {
                showDeleteConfirmationDialog(item, holder.getAdapterPosition());
            });
        }
    }

    private void showDeleteConfirmationDialog(CartModel item, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteItemFromFirestore(item, position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteItemFromFirestore(CartModel item, int position) {
        if (uid == null || position < 0 || position >= cartList.size()) return;
        db.collection("users")
                .document(uid)
                .collection("cart")
                .document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (position < cartList.size()) {
                        cartList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, cartList.size());
                    }
                    Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    if (totalPriceUpdateListener != null) {
                        totalPriceUpdateListener.onUpdate();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateQuantityInFirestore(CartModel item, int newQuantity, CartViewHolder holder) {
        if (uid == null) return;
        db.collection("users")
                .document(uid)
                .collection("cart")
                .document(item.getId())
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    item.setQuantity(newQuantity);
                    holder.tvQuantity.setText(String.valueOf(newQuantity));
                    if (totalPriceUpdateListener != null) {
                        totalPriceUpdateListener.onUpdate();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi cập nhật số lương", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvQuantity;
        ImageButton btnIncrease, btnDecrease, btnDelete;
        CheckBox cbSelectProduct;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cbSelectProduct = itemView.findViewById(R.id.cbSelectProduct);
        }
    }
}
