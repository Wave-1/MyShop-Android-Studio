package com.example.myshop.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Activities.ReviewActivity; // Đảm bảo import đúng Activity đánh giá của bạn
import com.example.myshop.Models.CartModel;
import com.example.myshop.R;
import com.example.myshop.Util.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private Context context;
    private ArrayList<CartModel> itemList;
    private String orderStatus;
    private String orderId;

    public OrderDetailAdapter(Context context, ArrayList<CartModel> itemList, String orderStatus, String orderId) {
        this.context = context;
        this.itemList = itemList;
        this.orderStatus = orderStatus;
        this.orderId = orderId;
    }

    @NonNull
    @Override
    public OrderDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_in_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailAdapter.ViewHolder holder, int position) {
        CartModel item = itemList.get(position);

        holder.tvProductName.setText(item.getName());
        holder.tvProductPrice.setText(String.format(Locale.getDefault(), "%,.0f ₫", item.getPrice()));
        holder.tvProductQuantity.setText("x " + item.getQuantity());
        Glide.with(context).load(item.getImage()).into(holder.ivProductImage);

        // Kiểm tra trạng thái "Hoàn thành"
        boolean isCompleted = Constants.ORDER_STATUS_COMPLETED.equalsIgnoreCase(orderStatus);

        if (isCompleted) {
            holder.btnReviewProduct.setVisibility(View.VISIBLE);
            holder.tvAlreadyReviewed.setVisibility(View.GONE);

            if (item.isReviewed()) {
                // Đã đánh giá -> Hiện text "Đã đánh giá", ẩn nút
                holder.btnReviewProduct.setVisibility(View.GONE);
                holder.tvAlreadyReviewed.setVisibility(View.VISIBLE);
            } else {
                // Chưa đánh giá -> Hiện nút "Đánh giá", ẩn text
                holder.btnReviewProduct.setVisibility(View.VISIBLE);
                holder.tvAlreadyReviewed.setVisibility(View.GONE);

                holder.btnReviewProduct.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ReviewActivity.class);
                    intent.putExtra("productId", item.getProductId());
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("productName", item.getName());
                    intent.putExtra("productImage", item.getImage());
                    context.startActivity(intent);
                });
            }

        } else {
            // Đơn chưa hoàn thành -> Ẩn nút đánh giá
            holder.btnReviewProduct.setVisibility(View.GONE);
            holder.tvAlreadyReviewed.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductQuantity;
        Button btnReviewProduct;
        TextView tvAlreadyReviewed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);

            // Ánh xạ View mới
            btnReviewProduct = itemView.findViewById(R.id.btnReviewProduct);
            tvAlreadyReviewed = itemView.findViewById(R.id.tvAlreadyReviewed);
        }
    }
}
