package com.example.myshop.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Activities.AdminOrderDetailActivity;
import com.example.myshop.Activities.OrderDetailActivity;
import com.example.myshop.Constants;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<OrderModel> orderList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(OrderModel order);
    }

    public OrderAdapter(Context context, ArrayList<OrderModel> orderList, OnItemClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.bind(order, listener);

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvOrderId, tvProductName, tvProductPrice, tvProductQuantity, tvItemCount, tvOrderDate, tvTotalAmount, tvOrderStatus;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
        }

        public void bind(final OrderModel order, final OnItemClickListener listener) {
            if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
                itemView.setVisibility(View.GONE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);
            String status = order.getStatus();

            String displayOrderId = order.getOrderId();
            if (displayOrderId != null && displayOrderId.length() > 8) {
                displayOrderId = displayOrderId.substring(0, 8);
            }
            tvOrderId.setText("Mã ĐH: " + displayOrderId);
            tvTotalAmount.setText(String.format("%,.0f ₫", order.getTotalAmount()));
            tvOrderStatus.setText(status);

            if (order.getTimestamp() != null) {
//                Timestamp timestamp = order.getTimestamp();
//                Date date = timestamp.toDate();
//                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//                tvOrderDate.setText(sdf.format(date));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvOrderDate.setText(sdf.format(order.getTimestamp().toDate()));
            }

            int backgroundColor = context.getColor(R.color.holo_orange_light);
            if (Constants.ORDER_STATUS_SHIPPING.equals(status)) {
                backgroundColor = context.getColor(R.color.button_secondary); // Màu xanh dương
            } else if (Constants.ORDER_STATUS_COMPLETED.equals(status)) {
                backgroundColor = context.getColor(R.color.button_primary); // Màu xanh lá
            } else if (Constants.ORDER_STATUS_CANCELLED.equals(status)) {
                backgroundColor = context.getColor(R.color.holo_red_dark); // Màu xám
            }
            GradientDrawable background = (GradientDrawable) tvOrderStatus.getBackground();
            background.setColor(backgroundColor);

            CartModel firstItem = order.getItems().get(0);
            tvProductName.setText(firstItem.getName());
            tvProductPrice.setText(String.format("%,.0f ₫", firstItem.getPrice()));
            tvProductQuantity.setText("x " + firstItem.getQuantity());
            Glide.with(context)
                    .load(firstItem.getImage())
                    .into(ivProductImage);

            int totalItems = order.getItems().size();
            if (totalItems > 1) {
                tvItemCount.setText("và " + (totalItems - 1) + " sản phẩm khác");
                tvItemCount.setVisibility(View.VISIBLE);
            } else {
                tvItemCount.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(order);
                }
            });
        }
    }
}
