package com.example.myshop.Models;

public class ProfileModel {
    private String title;
    private String value;
    private boolean showArrow; // có hiển thị mũi tên > không

    public ProfileModel(String title, String value, boolean showArrow) {
        this.title = title;
        this.value = value;
        this.showArrow = showArrow;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public boolean isShowArrow() {
        return showArrow;
    }
}
