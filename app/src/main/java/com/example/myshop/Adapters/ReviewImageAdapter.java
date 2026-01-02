package com.example.myshop.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.R;

import java.util.List;

public class ReviewImageAdapter extends RecyclerView.Adapter<ReviewImageAdapter.ImageViewHolder> {


    private final Context context;
    private final List<Uri> imageUris;
    private final OnImageDeleteListener deleteListener;

    public ReviewImageAdapter(Context context, List<Uri> imageUris, OnImageDeleteListener deleteListener) {
        this.context = context;
        this.imageUris = imageUris;
        this.deleteListener = deleteListener;
    }

    public interface OnImageDeleteListener {
        void onImageDeleted(int position);
    }


    @NonNull
    @Override
    public ReviewImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewImageAdapter.ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        Glide.with(context)
                .load(imageUri)
                .into(holder.ivReviewImage);
        holder.btnDeleteImage.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onImageDeleted(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivReviewImage;
        FrameLayout btnDeleteImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReviewImage = itemView.findViewById(R.id.ivReviewImage);
            btnDeleteImage = itemView.findViewById(R.id.btnDeleteImage);
        }
    }
}
