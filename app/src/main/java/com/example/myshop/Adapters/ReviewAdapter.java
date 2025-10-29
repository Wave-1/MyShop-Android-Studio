package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    public ReviewAdapter(Context context, List<ReviewModel>reviewModelList){
        this.context = context;
        this.reviewModelList = reviewModelList;
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ViewHolder holder, int position) {
        ReviewModel reviewModel = reviewModelList.get(position);

        holder.ratingBar.setRating(reviewModel.getRating());
        holder.tvComment.setText(reviewModel.getComment());

        if (reviewModel.getTimestamp() != null){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(reviewModel.getTimestamp().toDate()));
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(reviewModel.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        String username = documentSnapshot.getString("name");
                        holder.tvUserName.setText(username != null ? username :"Nguời dùng");

                    }
                });
    }

    @Override
    public int getItemCount() {
        return reviewModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvComment, tvDate;
        RatingBar ratingBar;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
