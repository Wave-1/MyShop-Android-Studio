package com.example.myshop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myshop.Models.ChannelModel;
import com.example.myshop.R;

import java.util.List;

public class ChannelAdapter extends BaseAdapter {
    private Context context;
    private List<ChannelModel> channelList;

    public ChannelAdapter(Context context, List<ChannelModel> channelList) {
        this.context = context;
        this.channelList = channelList;
    }

    @Override
    public int getCount() {
        return channelList.size();
    }

    @Override
    public Object getItem(int position) {
        return channelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.imgIcon);
        TextView title = convertView.findViewById(R.id.tvTitle);

        ChannelModel item = channelList.get(position);
        icon.setImageResource(item.getIcon());
        title.setText(item.getTitle());

        return convertView;
    }
}
