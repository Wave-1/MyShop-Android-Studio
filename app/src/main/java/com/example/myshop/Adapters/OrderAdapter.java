package com.example.myshop.Adapters;

import android.content.Context;
import android.graphics.Color;
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

import com.bumptech.glide.Glide;
import com.example.myshop.Models.CartModel;
import com.example.myshop.Models.OrderModel;
import com.example.myshop.R;

import java.util.List;

public class OrderAdapter extends BaseAdapter {
    private Context context;
    private List<OrderModel> orderList;

    public OrderAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public int getCount() {
        return orderList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        }

        // Gán đúng ID trong layout
        TextView tvOrderId = convertView.findViewById(R.id.tvOrderId);
        TextView tvOrderTotal = convertView.findViewById(R.id.tvOrderTotal);
        TextView tvOrderStatus = convertView.findViewById(R.id.tvOrderStatus);
        LinearLayout layoutItems = convertView.findViewById(R.id.layoutItems);
        OrderModel order = orderList.get(position);

        // Gán dữ liệu (có kiểm tra null)
        if (order != null) {
            String idText = (order.getOrderId() != null)
                    ? "Mã đơn: " + order.getOrderId()
                    : "Mã đơn: (chưa có)";
            tvOrderId.setText(idText);
            tvOrderTotal.setText("Tổng tiền: " + String.format("%,.0f ₫", order.getTotalAmount()));
            tvOrderStatus.setText("Trạng thái: " + order.getStatus());

            layoutItems.removeAllViews();

            List<CartModel> items = order.getItems();
            if(items != null && !items.isEmpty()){
                for (CartModel item : items) {
                    View itemView = LayoutInflater.from(context).inflate(R.layout.item_order_product, layoutItems, false);
                    ImageView imgProduct = itemView.findViewById(R.id.imgProduct);
                    TextView tvName = itemView.findViewById(R.id.tvName);
                    TextView tvPrice = itemView.findViewById(R.id.tvPrice);
                    TextView tvQuantity = itemView.findViewById(R.id.tvQuantity);

                    tvName.setText(item.getName());
                    tvPrice.setText(String.format("%,.0f ₫", item.getPrice()));
                    tvQuantity.setText("x" + item.getQuantity());

                    Glide.with(context)
                            .load(item.getImage()) // đường dẫn ảnh trong CartItem
                            .placeholder(R.drawable.store)
                            .into(imgProduct);

                    layoutItems.addView(itemView);
                }
            }
        }

        return convertView;
    }
}
