package com.example.myshop.Models;

import java.util.List;

public class OrderModel {
    private String orderId;
    private String userId;
    private String name;
    private String address;
    private String phone;
    private double totalAmount;
    private String status;
    private List<CartModel> items;

    public OrderModel() {}

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setItems(List<CartModel> items) {
        this.items = items;
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public List<CartModel> getItems() { return items; }
}
