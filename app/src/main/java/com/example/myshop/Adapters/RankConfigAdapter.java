package com.example.myshop.Adapters;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Models.RankConfigModel;
import com.example.myshop.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RankConfigAdapter extends RecyclerView.Adapter<RankConfigAdapter.ViewHolder> {

    private List<RankConfigModel> rankList;
    private OnDataChangeListener dataChangeListener;


    public RankConfigAdapter(List<RankConfigModel> rankList, OnDataChangeListener listener) {
        this.rankList = rankList;
        this.dataChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_config_rank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy rank hiện tại nhưng KHÔNG dùng final để có thể sửa giá trị trong list
        RankConfigModel rank = rankList.get(position);

        // Xóa listener cũ trước khi set text để tránh vòng lặp vô hạn
        holder.removeTextWatchers();

        holder.edtName.setText(rank.getName());
        holder.edtTargetOrder.setText(String.valueOf(rank.getTargetOrders()));
//        holder.edtTargetSpent.setText(String.valueOf((long)rank.getTargetSpent()));
        holder.edtDiscount.setText(String.valueOf(rank.getDiscountPercent()));

        // Nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION){
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa hạng \"" + rank.getName() + "\" không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // 1. Xóa khỏi danh sách dữ liệu
                            rankList.remove(pos);

                            // 2. Thông báo cho Adapter xóa item khỏi giao diện
                            notifyItemRemoved(pos);

                            // 3. Quan trọng: Cập nhật lại vị trí cho các item phía sau để tránh lỗi index
                            notifyItemRangeChanged(pos, rankList.size());
                            if (dataChangeListener != null) {
                                dataChangeListener.onDataChanged();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
        formatter.applyPattern("#,###");
        holder.edtTargetSpent.setText(formatter.format(rank.getTargetSpent()));

        // Thêm TextWatcher để cập nhật lại list ngay khi gõ
        holder.addTextWatchers(rank);
    }

    @Override
    public int getItemCount() {
        return rankList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText edtName, edtTargetOrder, edtTargetSpent, edtDiscount;
        ImageButton btnDelete;
        TextWatcher nameWatcher, orderWatcher, spentWatcher, discountWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            edtName = itemView.findViewById(R.id.edtRankName);
            edtTargetOrder = itemView.findViewById(R.id.edtTargetOrder);
            edtTargetSpent = itemView.findViewById(R.id.edtTargetSpent);
            edtDiscount = itemView.findViewById(R.id.edtDiscount);
            btnDelete = itemView.findViewById(R.id.btnDeleteRank);
        }

        void removeTextWatchers() {
            if (nameWatcher != null) edtName.removeTextChangedListener(nameWatcher);
            if (orderWatcher != null) edtTargetOrder.removeTextChangedListener(orderWatcher);
            if (spentWatcher != null) edtTargetSpent.removeTextChangedListener(spentWatcher);
            if (discountWatcher != null) edtDiscount.removeTextChangedListener(discountWatcher);
        }

        void addTextWatchers(RankConfigModel rank) {
            nameWatcher = new SimpleTextWatcher(s -> rank.setName(s.toString()));
            orderWatcher = new SimpleTextWatcher(s -> {
                if (!s.toString().isEmpty()) rank.setTargetOrders(Integer.parseInt(s.toString()));
            });
            spentWatcher = new TextWatcher() {
                private String current = "";

                @Override
                public void afterTextChanged(Editable s) {
                    if (!s.toString().equals(current)) {
                        edtTargetSpent.removeTextChangedListener(this);
                        String cleanString = s.toString().replaceAll("[^\\d]", "");
                        if (!cleanString.isEmpty()) {
                            try {
                                double parsed = Double.parseDouble(cleanString);
                                rank.setTargetSpent(parsed);

                                DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
                                decimalFormat.applyPattern("#, ###");
                                String formatted = decimalFormat.format(parsed);

                                current = formatted;
                                edtTargetSpent.setText(formatted);
                                edtTargetSpent.setSelection(formatted.length());
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        rank.setTargetSpent(0);
                    }
                    edtTargetSpent.addTextChangedListener(this);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }
            };
            discountWatcher = new SimpleTextWatcher(s -> {
                if (!s.toString().isEmpty())
                    rank.setDiscountPercent(Integer.parseInt(s.toString()));
            });

            edtName.addTextChangedListener(nameWatcher);
            edtTargetOrder.addTextChangedListener(orderWatcher);
            edtTargetSpent.addTextChangedListener(spentWatcher);
            edtDiscount.addTextChangedListener(discountWatcher);
        }
    }

    interface OnTextChanged {
        void onTextChanged(CharSequence s);
    }

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    static class SimpleTextWatcher implements TextWatcher {
        private final OnTextChanged onTextChanged;

        public SimpleTextWatcher(OnTextChanged onTextChanged) {
            this.onTextChanged = onTextChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextChanged.onTextChanged(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
