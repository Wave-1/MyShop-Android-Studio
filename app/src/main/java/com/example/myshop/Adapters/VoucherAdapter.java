package com.example.myshop.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Models.VoucherModel;
import com.example.myshop.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private final Context context;
    private final List<VoucherModel> voucherList;
    private final OnVoucherSelectListener listener;

    // Vị trí voucher đang được chọn trong danh sách này
    private int selectedPosition = -1;

    // Biến kiểm soát trạng thái "Xem thêm"
    // false: Chỉ hiện 1 voucher tốt nhất (item đầu tiên)
    // true: Hiện tất cả danh sách
    private boolean isExpanded = false;

    public interface OnVoucherSelectListener {
        void onVoucherSelected(VoucherModel voucher);
    }

    public VoucherAdapter(Context context, List<VoucherModel> voucherList, OnVoucherSelectListener listener) {
        this.context = context;
        this.voucherList = voucherList;
        this.listener = listener;
    }

    // Phương thức để Activity gọi khi bấm vào chữ "Xem thêm"
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        notifyDataSetChanged(); // Cập nhật lại danh sách để hiện đủ
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    // Phương thức để lấy voucher đang chọn (nếu cần dùng từ bên ngoài)
    public VoucherModel getSelectedVoucher() {
        if (selectedPosition != -1 && selectedPosition < voucherList.size()) {
            return voucherList.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VoucherModel voucher = voucherList.get(position);

        // 1. Hiển thị Mã
        holder.tvVoucherCode.setText(voucher.getCode());

        // 2. Hiển thị Số lượng
        holder.rbSelect.setChecked(voucher.isSelected());
        holder.rbSelect.setClickable(false);
        holder.rbSelect.setFocusable(false);

        if (voucher.getQuantity() <= 0) {
            // TRƯỜNG HỢP HẾT HÀNG
            holder.itemView.setAlpha(0.5f);
            if (holder.tvQuantity != null) {
                holder.tvQuantity.setText("Hết số lượng");
            }

            // Vô hiệu hóa click
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);

            // Vô hiệu hóa RadioButton
            if (holder.rbSelect != null) {
                holder.rbSelect.setEnabled(false);
                holder.rbSelect.setChecked(false);
            }
        } else {
            // TRƯỜNG HỢP CÒN HÀNG
            holder.itemView.setAlpha(1.0f);
            if (holder.tvQuantity != null) {
                holder.tvQuantity.setText("x " + voucher.getQuantity());
            }

            // Kích hoạt RadioButton
            if (holder.rbSelect != null) {
                holder.rbSelect.setEnabled(true);
            }

            // Kích hoạt click
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    // Logic chọn / bỏ chọn
                    boolean newState = !voucher.isSelected();
                    voucher.setSelected(newState);
                    notifyItemChanged(currentPos);

                    if (listener != null) {
                        listener.onVoucherSelected(voucher);
                    }
                }
            });
        }

        // 3. Xử lý hiển thị nội dung giảm giá
        java.text.NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String discountInfoText = "";

        if ("percentage".

                equals(voucher.getDiscountType())) {
            discountInfoText = "Giảm " + (int) voucher.getDiscountValue() + "%";
            if (voucher.getMaxDiscountValue() > 0) {
                discountInfoText += " Tối đa " + currencyFormatter.format(voucher.getMaxDiscountValue());
            }
        } else if ("freeship".

                equals(voucher.getDiscountType())) {
            discountInfoText = "Miễn phí vận chuyển";
            if (voucher.getDiscountValue() > 0) {
                discountInfoText += " Tối đa " + currencyFormatter.format(voucher.getDiscountValue());
            }
        } else {
            discountInfoText = "Giảm " + currencyFormatter.format(voucher.getDiscountValue());
        }
        holder.tvDiscountInfo.setText(discountInfoText);

        String minOrderText = "Đơn tối thiểu " + currencyFormatter.format(voucher.getMinOrderValue());
        holder.tvMinOrderInfo.setText(minOrderText);

        // 4. Xử lý màu sắc và icon bên trái
        if ("freeship".

                equals(voucher.getDiscountType())) {
            holder.llLeftPart.setBackgroundResource(android.R.color.holo_green_light);
            holder.ivVoucherIcon.setImageResource(R.drawable.ic_local_shipping);
            holder.tvVoucherDiscountType.setText("Freeship");
        } else {
            holder.llLeftPart.setBackgroundResource(R.drawable.bg_header_gradient);
            holder.ivVoucherIcon.setImageResource(R.drawable.ic_voucher_2);
            holder.tvVoucherDiscountType.setText("Giảm giá");
        }

        // 5. Ngày hết hạn
        if (voucher.getExpiryDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvVoucherExpiry.setText(String.format("HSD: %s", sdf.format(voucher.getExpiryDate())));
        } else {
            holder.tvVoucherExpiry.setText("Vĩnh viễn");
        }
    }

    @Override
    public int getItemCount() {
        if (voucherList == null) return 0;

        // Nếu list chỉ có 1 hoặc ít hơn -> Luôn hiện hết
        if (voucherList.size() <= 1) {
            return voucherList.size();
        }

        // Nếu chưa mở rộng (chưa bấm Xem thêm) -> Chỉ trả về 1 (voucher đầu tiên/tốt nhất)
        // Nếu đã mở rộng -> Trả về toàn bộ danh sách
        return isExpanded ? voucherList.size() : 1;
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvVoucherCode, tvDiscountInfo, tvMinOrderInfo, tvVoucherExpiry, tvVoucherDiscountType, tvQuantity;
        RadioButton rbSelect;
        LinearLayout llLeftPart;
        ImageView ivVoucherIcon;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            llLeftPart = itemView.findViewById(R.id.llLeftPart);
            ivVoucherIcon = itemView.findViewById(R.id.ivVoucherIcon);
            tvVoucherDiscountType = itemView.findViewById(R.id.tvVoucherDiscountType);

            tvVoucherCode = itemView.findViewById(R.id.tvVoucherCode);
            tvDiscountInfo = itemView.findViewById(R.id.tvDiscountInfo);
            tvMinOrderInfo = itemView.findViewById(R.id.tvMinOrderInfo);
            tvVoucherExpiry = itemView.findViewById(R.id.tvVoucherExpiry);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);

            rbSelect = itemView.findViewById(R.id.rbSelect);
        }
    }
}
