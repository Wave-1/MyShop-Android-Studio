package com.example.myshop.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Activities.AddEditAddressActivity;
import com.example.myshop.Models.AddressModel;
import com.example.myshop.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private Context context;
    private ArrayList<AddressModel> addressModels;
    private OnAddressSelectedListener listener;
    private ActivityResultLauncher<Intent> addEditAddressLauncher;

    public interface OnAddressSelectedListener {
        void onAddressSelected(AddressModel addressModel);
    }

    public AddressAdapter(Context context, ArrayList<AddressModel> addressModels, ActivityResultLauncher<Intent> launcher,OnAddressSelectedListener listener) {
        this.context = context;
        this.addressModels = addressModels;
        this.addEditAddressLauncher = launcher;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressAdapter.AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressAdapter.AddressViewHolder holder, int position) {
        AddressModel address = addressModels.get(position);
        holder.tvName.setText(address.getName());
        holder.tvPhone.setText(address.getPhone());
        String fullAddress = address.getAddressLine() + ", " + address.getWard() + ", " + address.getDistrict() + ", " + address.getCity();
        holder.tvAddress.setText(fullAddress);

        holder.tvDefaultLabel.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

        holder.radioSelect.setChecked(address.isDefault());

        holder.cardAddress.setOnClickListener(v -> {
            updateDefaultAddressInFirestore(address);
            if (listener != null) {
                listener.onAddressSelected(address);
            }
        });
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditAddressActivity.class);
            intent.putExtra("EDIT_ADDRESS", address);
            addEditAddressLauncher.launch(intent);
        });
    }

    private void updateDefaultAddressInFirestore(AddressModel newDefaultAddress) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        if (newDefaultAddress.isDefault()){
            return;
        }
        // 1. Tìm tất cả các địa chỉ và bỏ trạng thái mặc định của chúng
        db.collection("users").document(uid).collection("addresses")
                .whereEqualTo("default", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        // Chỉ bỏ default nếu nó không phải là cái vừa được chọn
                        if (!doc.getId().equals(newDefaultAddress.getId())) {
                            batch.update(doc.getReference(), "default", false);
                        }
                    }

                    // 2. Đặt địa chỉ mới được chọn làm mặc định
                    batch.update(db.collection("users").document(uid).collection("addresses").document(newDefaultAddress.getId()), "default", true);

                    // 3. Thực thi tất cả thay đổi
                    batch.commit().addOnSuccessListener(aVoid -> {
                        // Cập nhật thành công!
                        // Bạn cũng có thể cập nhật lại model trong list để đảm bảo đồng bộ
                        for(AddressModel model : addressModels){
                            model.setDefault(model.getId().equals(newDefaultAddress.getId()));
                        }
                        notifyDataSetChanged();
                        // Không cần notifyDataSetChanged() vì UI đã được cập nhật trước đó
                    }).addOnFailureListener(e -> {
                        // Có lỗi xảy ra, có thể thông báo cho người dùng
                        Toast.makeText(context, "Lỗi cập nhật địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    @Override
    public int getItemCount() {
        return addressModels.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardAddress;
        RadioButton radioSelect;
        TextView tvName, tvPhone, tvAddress, tvDefaultLabel;
        ImageButton btnEdit;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAddress = itemView.findViewById(R.id.cardAddress);
            radioSelect = itemView.findViewById(R.id.radioSelect);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefaultLabel = itemView.findViewById(R.id.tvDefaultLabel);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }

    }
}
