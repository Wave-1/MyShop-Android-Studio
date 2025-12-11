package com.example.myshop.Models;

import java.io.Serializable;
import java.util.Date;

public class VoucherModel implements Serializable {
    private String id;
    private String code;
    private double discountValue;
    private String discountType;
    private double minOrderValue;
    private Date expiryDate;
    private double maxDiscountValue;
    private int quantity;
    private boolean isSelected = false;

    public VoucherModel() {
    }

    public VoucherModel(String id, String code, double discountValue, String discountType, double minOrderValue, Date expiryDate, double maxDiscountValue, int quantity, boolean isSelected) {
        this.id = id;
        this.code = code;
        this.discountValue = discountValue;
        this.discountType = discountType;
        this.minOrderValue = minOrderValue;
        this.expiryDate = expiryDate;
        this.maxDiscountValue = maxDiscountValue;
        this.quantity = quantity;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getMaxDiscountValue() {
        return maxDiscountValue;
    }

    public void setMaxDiscountValue(double maxDiscountValue) {
        this.maxDiscountValue = maxDiscountValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
