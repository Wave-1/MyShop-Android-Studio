package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Models.VoucherModel;
import com.example.myshop.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminVoucherAdapter extends RecyclerView.Adapter<AdminVoucherAdapter.ViewHolder> {

    private Context context;
    private List<VoucherModel> list;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(VoucherModel voucher);
    }

    public AdminVoucherAdapter(Context context, List<VoucherModel> list, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.list = list;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VoucherModel voucher = list.get(position);

        holder.tvCode.setText(voucher.getCode());
        holder.tvQuantity.setText("Số lượng: " + voucher.getQuantity());
        if (voucher.getExpiryDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvExpiry.setText("HSD: " + sdf.format(voucher.getExpiryDate()));
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(voucher);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvQuantity, tvExpiry;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            btnDelete = itemView.findViewById(R.id.btnDeleteVoucher);
        }
    }
}
