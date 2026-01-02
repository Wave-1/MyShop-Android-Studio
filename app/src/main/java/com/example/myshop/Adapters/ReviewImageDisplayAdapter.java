package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myshop.R;
import java.util.List;

public class ReviewImageDisplayAdapter extends RecyclerView.Adapter<ReviewImageDisplayAdapter.ViewHolder> {

    private final Context context;
    private final List<String> imageUrls;

    public ReviewImageDisplayAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_image_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load(imageUrl)
                .into(holder.ivReviewImage);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivReviewImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReviewImage = itemView.findViewById(R.id.ivReviewImage);
        }
    }
}
