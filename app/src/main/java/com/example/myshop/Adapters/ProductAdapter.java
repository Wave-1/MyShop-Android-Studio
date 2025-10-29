package com.example.myshop.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Activities.ProductDetailActivity;
import com.example.myshop.Models.ProductModel;
import com.example.myshop.R;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ProductModel> productModelList;
    private TextView tvSalesCount;

    public ProductAdapter(Context context, ArrayList<ProductModel> productModelList) {
        this.context = context;
        this.productModelList = productModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductModel p = productModelList.get(position);
//        holder.imgProduct.setImageResource(p.getImage());
        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(String.format("%,.0f ₫", p.getPrice()));
        // Chuyển tên drawable sang resource id
        Glide.with(context)
                .load(p.getImage())
                .placeholder(R.drawable.bg_image_placeholder)
                .into(holder.imgProduct);

        // Xử lý click item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", p.getProductId()); // truyền cả object
            context.startActivity(intent);
        });

        // Hiển thi số lượng đã bán
        long salesCount = p.getSalesCount();
        if (salesCount > 0){
            holder.tvSalesCount.setVisibility(View.VISIBLE);
            holder.tvSalesCount.setText("Đã bán " + salesCount);
        } else {
          holder.tvSalesCount.setVisibility(View.GONE);
        }

        if (p.isOnSale()){
            holder.tvSalePercentage.setVisibility(View.VISIBLE);
            holder.tvSalePercentage.setText("-" + p.getSalePercent() + "%");
        }else {
            holder.tvSalePercentage.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() { return productModelList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvSalesCount, tvSalePercentage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSalesCount = itemView.findViewById(R.id.tvSalesCount);
            tvSalePercentage = itemView.findViewById(R.id.tvSalePercentage);
        }
    }
    public void setProducts(ArrayList<ProductModel> newProducts) {
        this.productModelList = newProducts;
    }
}
