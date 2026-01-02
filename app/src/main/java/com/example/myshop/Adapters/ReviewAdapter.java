package com.example.myshop.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myshop.Models.ReviewModel;
import com.example.myshop.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Context context;
    private List<ReviewModel> reviewModelList;

    public ReviewAdapter(Context context, List<ReviewModel> reviewModelList) {
        this.context = context;
        this.reviewModelList = reviewModelList;
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ViewHolder holder, int position) {
        ReviewModel reviewModel = reviewModelList.get(position);

        // --- 1. HIỂN THỊ THÔNG TIN CƠ BẢN ---
        holder.ratingBar.setRating(reviewModel.getRating());
        holder.tvComment.setText(reviewModel.getComment());
        if (reviewModel.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(reviewModel.getTimestamp().toDate()));
        }

        Glide.with(context)
                .load(reviewModel.getUserAvatar()) // Dùng URL từ Firestore
                .placeholder(R.drawable.ic_person) // Ảnh chờ
                .error(R.drawable.ic_person)       // Thay R.drawable.store thành ic_person cho thống nhất
                .circleCrop()                      // Bo tròn ảnh
                .into(holder.ivUserAvatar);

        // --- 2. HIỂN THỊ TÊN VÀ AVATAR NGƯỜI DÙNG ---
        String userName = reviewModel.getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = "Người dùng"; // Giá trị mặc định nếu tên trống
        }

        if (reviewModel.isAnonymous()) {
            // --- Xử lý khi người dùng chọn "Ẩn danh" ---
            String maskedName;
            if (userName.length() > 2) {
                char firstChar = userName.charAt(0);
                char lastChar = userName.charAt(userName.length() - 1);
                maskedName = firstChar + "*****" + lastChar;
            } else {
                maskedName = "Người dùng ẩn danh";
            }
            holder.tvUserName.setText(maskedName);
        } else {
            holder.tvUserName.setText(reviewModel.getUserName());
        }

        // --- 3. HIỂN THỊ DANH SÁCH ẢNH ĐÁNH GIÁ ---
        if (reviewModel.getImageUrls() != null && !reviewModel.getImageUrls().isEmpty()) {
            holder.recyclerReviewImages.setVisibility(View.VISIBLE);
            // Cấu hình Adapter cho RecyclerView con
            ReviewImageDisplayAdapter imageAdapter = new ReviewImageDisplayAdapter(context, reviewModel.getImageUrls());
            holder.recyclerReviewImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerReviewImages.setAdapter(imageAdapter);
        } else {
            // Nếu không có ảnh, ẩn RecyclerView đi
            holder.recyclerReviewImages.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return reviewModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName, tvComment, tvDate;
        RatingBar ratingBar;
        RecyclerView recyclerReviewImages;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            recyclerReviewImages = itemView.findViewById(R.id.recyclerReviewImages);
        }
    }
}
