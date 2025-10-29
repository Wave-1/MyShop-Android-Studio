package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Models.CartModel;
import com.example.myshop.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {
    private Context context;
    private ArrayList<CartModel> checkoutList;

    public CheckoutAdapter(Context context, ArrayList<CartModel> checkoutList) {
        this.context = context;
        this.checkoutList = checkoutList;
    }

    @NonNull
    @Override
    public CheckoutAdapter.CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutAdapter.CheckoutViewHolder holder, int position) {
        CartModel item = checkoutList.get(position);

        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.drawable.bg_image_placeholder) // Bạn nên có một ảnh placeholder
                .into(holder.imgProduct);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(item.getPrice()) + " ₫");
        holder.tvQuantity.setText("x " + item.getQuantity());
    }

    @Override
    public int getItemCount() {
        return checkoutList.size();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder{
        ImageView imgProduct;
        TextView tvName, tvPrice, tvQuantity;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }


//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            convertView = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
//        }
//
//        CartModel item = checkoutList.get(position);
//
//        ImageView imgProduct = convertView.findViewById(R.id.imgProduct);
//        TextView tvName = convertView.findViewById(R.id.tvName);
//        TextView tvPrice = convertView.findViewById(R.id.tvPrice);
//        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
//
//        // Hiển thị ảnh sản phẩm
//        Glide.with(context)
//                .load(item.getImage())
//                .placeholder(R.drawable.bg_image_placeholder)
//                .into(imgProduct);
//
//        // Hiển thị thông tin sản phẩm
//        tvName.setText(item.getName());
//        tvPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN"))
//                .format(item.getPrice()) + " ₫");
//        tvQuantity.setText("Số lượng: " + item.getQuantity());
//
//        return convertView;
//    }
}
