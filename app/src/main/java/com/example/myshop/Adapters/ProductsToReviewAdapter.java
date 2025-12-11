package com.example.myshop.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Models.CartModel;
import com.example.myshop.R;

import java.util.List;

public class ProductsToReviewAdapter extends RecyclerView.Adapter<ProductsToReviewAdapter.ProductViewHolder> {

    private final List<CartModel> productList;
    private final String orderId;
    private final OnProductReviewClickListener listener;

    public interface OnProductReviewClickListener {
        void onProductReviewClick(CartModel product, String orderId);
    }

    public ProductsToReviewAdapter(List<CartModel> productList, String orderId, OnProductReviewClickListener listener) {
        this.productList = productList;
        this.orderId = orderId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_in_order, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position), orderId, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvAlreadyReviewed;
        Button btnReviewProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            btnReviewProduct = itemView.findViewById(R.id.btnReviewProduct);
            tvAlreadyReviewed = itemView.findViewById(R.id.tvAlreadyReviewed);
        }

        void bind(final CartModel product, final String orderId, final OnProductReviewClickListener listener) {
            tvProductName.setText(product.getName());
            Glide.with(itemView.getContext()).load(product.getImage()).into(ivProductImage);

            if (product.isReviewed()) {
                btnReviewProduct.setVisibility(View.GONE);
                tvAlreadyReviewed.setVisibility(View.VISIBLE);
            } else {
                btnReviewProduct.setVisibility(View.VISIBLE);
                tvAlreadyReviewed.setVisibility(View.GONE);
                btnReviewProduct.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProductReviewClick(product, orderId);
                    }
                });
            }
        }
    }
}
