package com.example.myshop.Models;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderModel implements Serializable {

    // --- Thông tin chính ---
    private String orderId;
    private String userId;
    private String status;
    private double totalAmount;
    private String cancellationReason;

    // --- Đối tượng lồng nhau ---
    private AddressModel address;
    private List<String> productIds;
    private ArrayList<CartModel> items;

    // --- Xử lý Timestamp cho Serializable ---
    private transient Timestamp timestamp;
    private Date serializableTimestamp;

    // --- Constructors ---
    public OrderModel() {

    }

    // --- Getters and Setters ---

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public AddressModel getAddress() {
        return address;
    }

    public void setAddress(AddressModel address) {
        this.address = address;
    }

    public ArrayList<CartModel> getItems() {
        return items;
    }

    public void setItems(ArrayList<CartModel> items) {
        this.items = items;
    }
    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }
    public Timestamp getTimestamp() {
        if (timestamp == null && serializableTimestamp != null) {
            return new Timestamp(serializableTimestamp);
        }
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        if (timestamp != null) {
            this.serializableTimestamp = timestamp.toDate();
        }
    }
}
