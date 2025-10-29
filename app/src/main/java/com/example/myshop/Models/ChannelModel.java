package com.example.myshop.Models;

public class ChannelModel {
    private int icon;
    private String title;

    public ChannelModel(int icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }
}
