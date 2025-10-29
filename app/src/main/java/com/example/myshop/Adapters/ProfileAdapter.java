package com.example.myshop.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myshop.Models.ProfileModel;
import com.example.myshop.R;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProfileModel> items;

    public ProfileAdapter(List<ProfileModel> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        String value = items.get(position).getValue();
        if (value.equals("switch")) return 1;
        if (value.equals("switch_on")) return 2;
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1 || viewType == 2) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_switch, parent, false);
            return new SwitchViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
            return new TextViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileModel item = items.get(position);

        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(item);
        } else if (holder instanceof SwitchViewHolder) {
            ((SwitchViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvValue;
        ImageView ivArrow;

        TextViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvValue = itemView.findViewById(R.id.tvValue);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }

        void bind(ProfileModel item) {
            tvTitle.setText(item.getTitle());
            tvValue.setText(item.getValue());
            ivArrow.setVisibility(item.isShowArrow() ? View.VISIBLE : View.GONE);
        }
    }

    static class SwitchViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        Switch switchButton;

        SwitchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            switchButton = itemView.findViewById(R.id.switchButton);
        }

        void bind(ProfileModel item) {
            tvTitle.setText(item.getTitle());
            switchButton.setChecked(item.getValue().equals("switch_on"));
        }
    }
}
