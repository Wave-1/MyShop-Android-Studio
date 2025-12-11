package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        holder.ratingBar.setRating(reviewModel.getRating());
        holder.tvComment.setText(reviewModel.getComment());

        String userName = reviewModel.getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = "Người dùng";
        }

        if (reviewModel.isAnonymous()) {
            String maskedName;
            if (userName.length() > 2) {
                char firstChar = userName.charAt(0);
                char lastChar = userName.charAt(userName.length() - 1);
                maskedName = firstChar + "*****" + lastChar;
            } else if (!userName.isEmpty()) {
                maskedName = userName.charAt(0) + "*";
            } else {
                maskedName = "*******";
            }
            holder.tvUserName.setText(maskedName);
            holder.ivUserAvatar.setImageResource(R.drawable.ic_account);
        } else {
            holder.tvUserName.setText(reviewModel.getUserName());
            Glide.with(context)
                    .load(reviewModel.getUserAvatar())
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .circleCrop()
                    .into(holder.ivUserAvatar);
        }
        if (reviewModel.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(reviewModel.getTimestamp().toDate()));
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


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
