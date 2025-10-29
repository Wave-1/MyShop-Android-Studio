package com.example.myshop.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Activities.OrderTrackingActivity;
import com.example.myshop.Models.UserModel;
import com.example.myshop.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserModel> userList;
    private Context context;

    public UserAdapter(List<UserModel> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText("Role: " + user.getRole());

        if("admin".equalsIgnoreCase(user.getRole())){
            holder.btnDelete.setVisibility(View.GONE);
        }else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setEnabled(true);
            holder.btnDelete.setAlpha(1f);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderTrackingActivity.class);
            intent.putExtra("userId", user.getId());
            intent.putExtra("userEmail", user.getEmail());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc muốn xoá user: " + user.getEmail() + " ?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(user.getId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(context, "Đã xoá " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                    userList.remove(position);
                                    notifyItemRemoved(position);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Lỗi xoá: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvRole;
        ImageButton btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
