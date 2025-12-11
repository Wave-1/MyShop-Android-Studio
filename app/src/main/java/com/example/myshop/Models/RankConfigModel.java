package com.example.myshop.Models;

public class RankConfigModel {
    private String name;
    private int targetOrders;
    private double targetSpent;
    private int discountPercent;

    public RankConfigModel() { } // Constructor rỗng cho Firebase

    public RankConfigModel(String name, int targetOrders, double targetSpent, int discountPercent) {
        this.name = name;
        this.targetOrders = targetOrders;
        this.targetSpent = targetSpent;
        this.discountPercent = discountPercent;
    }

    // Getters và Setters đầy đủ
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTargetOrders() { return targetOrders; }
    public void setTargetOrders(int targetOrders) { this.targetOrders = targetOrders; }
    public double getTargetSpent() { return targetSpent; }
    public void setTargetSpent(double targetSpent) { this.targetSpent = targetSpent; }
    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
}
