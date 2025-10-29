package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Models.ProductModel;
import com.example.myshop.R;

import java.util.List;

public class ProductAdminAdapter extends RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder> {

    private Context context;
    private List<ProductModel> productModelList;
    private OnItemClickListener listener;

    // interface để bắt sự kiện edit/delete
    public interface OnItemClickListener {
        void onEdit(ProductModel productModel);
        void onDelete(ProductModel productModel);
        void onItemClick(ProductModel productModel);
    }

    public ProductAdminAdapter(Context context, List<ProductModel> productModelList, OnItemClickListener listener) {
        this.context = context;
        this.productModelList = productModelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_admin, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel productModel = productModelList.get(position);
        holder.tvName.setText(productModel.getName());
        holder.tvPrice.setText(String.format("%,.0f ₫", productModel.getPrice()));

        // load ảnh từ link
        Glide.with(context)
                .load(productModel.getImage())
                .placeholder(R.drawable.store)
                .into(holder.imgProduct);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(productModel));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(productModel));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(productModel));
    }

    @Override
    public int getItemCount() {
        return productModelList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView imgProduct;
        ImageButton btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}