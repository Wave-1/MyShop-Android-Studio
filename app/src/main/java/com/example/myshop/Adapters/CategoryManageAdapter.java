package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Models.CategoryModel;
import com.example.myshop.R;

import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.CategoryViewHolder> {

    private Context context;
    private List<CategoryModel> categoryList;
    private OnCategoryDeleteListener deleteListener;

    public interface OnCategoryDeleteListener {
        void onDeleteClick(CategoryModel category);
    }

    public CategoryManageAdapter(Context context, List<CategoryModel> categoryList, OnCategoryDeleteListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_manage, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());

        holder.btnDeleteCategory.setOnClickListener(v -> {
            deleteListener.onDeleteClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageButton btnDeleteCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
