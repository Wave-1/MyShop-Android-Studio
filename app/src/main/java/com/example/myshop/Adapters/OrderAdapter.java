package com.example.myshop.Adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Util.Constants;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;

// ✅ BƯỚC 1: Import class ProductsToReviewAdapter

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<OrderModel> orderList;
    private final OnItemClickListener listener;
    private final OnOrderCancelListener cancelListener;
    private final OnConfirmReceiptListener confirmReceiptListener;
    private final OnReturnOrderListener returnOrderListener;
    private final ProductsToReviewAdapter.OnProductReviewClickListener productReviewClickListener;

    // --- Interfaces ---
    public interface OnItemClickListener {
        void onItemClick(OrderModel order);
    }

    public interface OnOrderCancelListener {
        void onCancelClick(OrderModel order);
    }

    public interface OnConfirmReceiptListener {
        void onConfirmReceiptClick(OrderModel order);
    }

    public interface OnReturnOrderListener {
        void onReturnOrderClick(OrderModel order);
    }

    public OrderAdapter(Context context, ArrayList<OrderModel> orderList,
                        OnItemClickListener listener,
                        OnOrderCancelListener cancelListener,
                        OnConfirmReceiptListener confirmReceiptListener,
                        OnReturnOrderListener returnOrderListener,
                        ProductsToReviewAdapter.OnProductReviewClickListener productReviewClickListener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
        this.cancelListener = cancelListener;
        this.confirmReceiptListener = confirmReceiptListener;
        this.returnOrderListener = returnOrderListener;
        this.productReviewClickListener = productReviewClickListener;
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view, productReviewClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);
        holder.bind(order, listener, cancelListener, confirmReceiptListener, returnOrderListener);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvOrderId, tvProductName, tvProductPrice, tvProductQuantity, tvItemCount, tvOrderDate, tvTotalAmount, tvOrderStatus;
        Context context;
        Button btnCancelOrder, btnReturnOrder, btnConfirmReceipt;
        LinearLayout layoutProductInfo, actionButtonsLayout;
//        RecyclerView recyclerProductsToReview;
        ProductsToReviewAdapter.OnProductReviewClickListener productReviewClickListener;

        public ViewHolder(@NonNull View itemView, ProductsToReviewAdapter.OnProductReviewClickListener productReviewClickListener) {
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

//            recyclerProductsToReview = itemView.findViewById(R.id.recyclerProductsToReview);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            this.productReviewClickListener = productReviewClickListener;

            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            btnReturnOrder = itemView.findViewById(R.id.btnReturnOrder);
            btnConfirmReceipt = itemView.findViewById(R.id.btnConfirmReceipt);
            layoutProductInfo = itemView.findViewById(R.id.layoutProductInfo);
        }

        public void bind(final OrderModel order, final OnItemClickListener listener, final OnOrderCancelListener cancelListener, final OnConfirmReceiptListener confirmReceiptListener, final OnReturnOrderListener returnOrderListener) {

            if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
                itemView.setVisibility(View.GONE);
                return;
            }
            itemView.setVisibility(View.VISIBLE);
            String status = order.getStatus();

            // --- Bind dữ liệu chung ---
            String displayOrderId = order.getOrderId();
            if (displayOrderId != null && displayOrderId.length() > 8) {
                displayOrderId = displayOrderId.substring(0, 8);
            }
            tvOrderId.setText("Mã ĐH: " + displayOrderId);
            tvTotalAmount.setText(String.format("%,.0f ₫", order.getTotalAmount()));
            tvOrderStatus.setText(status);

            if (order.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvOrderDate.setText(sdf.format(order.getTimestamp().toDate()));
            }

            int backgroundColor = context.getColor(R.color.holo_orange_light);
            if (Constants.ORDER_STATUS_SHIPPING.equals(status)) {
                backgroundColor = context.getColor(R.color.button_secondary);
            } else if ("Đã giao".equals(status)) { // Dùng chuỗi để so sánh
                backgroundColor = context.getColor(R.color.holo_blue_dark);
            } else if (Constants.ORDER_STATUS_COMPLETED.equals(status)) {
                backgroundColor = context.getColor(R.color.button_primary);
            } else if (Constants.ORDER_STATUS_CANCELLED.equals(status)) {
                backgroundColor = context.getColor(R.color.holo_red_dark);
            }
            GradientDrawable background = (GradientDrawable) tvOrderStatus.getBackground();
            background.setColor(backgroundColor);

            CartModel firstItem = order.getItems().get(0);
            tvProductName.setText(firstItem.getName());
            tvProductPrice.setText(String.format("%,.0f ₫", firstItem.getPrice()));
            tvProductQuantity.setText("x " + firstItem.getQuantity());
            Glide.with(context).load(firstItem.getImage()).into(ivProductImage);

            int totalItems = order.getItems().size();
            if (totalItems > 1) {
                tvItemCount.setText("và " + (totalItems - 1) + " sản phẩm khác");
                tvItemCount.setVisibility(View.VISIBLE);
            } else {
                tvItemCount.setVisibility(View.GONE);
            }

            // --- LOGIC HIỂN THỊ DỰA TRÊN TRẠNG THÁI ---
            if (Constants.ORDER_STATUS_COMPLETED.equalsIgnoreCase(status)) {
                // TRẠNG THÁI "HOÀN THÀNH": HIỂN THỊ DANH SÁCH SẢN PHẨM ĐỂ ĐÁNH GIÁ
                actionButtonsLayout.setVisibility(View.GONE);
//                recyclerProductsToReview.setVisibility(View.VISIBLE);

                ProductsToReviewAdapter productsAdapter = new ProductsToReviewAdapter(order.getItems(), order.getOrderId(), productReviewClickListener);
//                recyclerProductsToReview.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
//                recyclerProductsToReview.setAdapter(productsAdapter);
//                recyclerProductsToReview.setNestedScrollingEnabled(false); // Quan trọng

            } else {
                // CÁC TRẠNG THÁI KHÁC: HIỂN THỊ CÁC NÚT HÀNH ĐỘNG
                actionButtonsLayout.setVisibility(View.VISIBLE);
//                recyclerProductsToReview.setVisibility(View.GONE);

                btnCancelOrder.setVisibility(View.GONE);
                btnReturnOrder.setVisibility(View.GONE);
                btnConfirmReceipt.setVisibility(View.GONE);

                if (Constants.ORDER_STATUS_PROCESSING.equalsIgnoreCase(status)) {
                    btnCancelOrder.setVisibility(View.VISIBLE);
                } else if ("Đã giao".equalsIgnoreCase(status)) { // ✅ BƯỚC 5: Sửa lại cách so sánh trạng thái
                    btnReturnOrder.setVisibility(View.VISIBLE);
                    btnConfirmReceipt.setVisibility(View.VISIBLE);
                }
            }

            // --- Gán sự kiện Click ---
            layoutProductInfo.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(order);
            });
            btnCancelOrder.setOnClickListener(v -> {
                if (cancelListener != null) cancelListener.onCancelClick(order);
            });
            btnReturnOrder.setOnClickListener(v -> {
                if (returnOrderListener != null) returnOrderListener.onReturnOrderClick(order);
            });
            btnConfirmReceipt.setOnClickListener(v -> {
                if (confirmReceiptListener != null) confirmReceiptListener.onConfirmReceiptClick(order);
            });
        }
    }
}
