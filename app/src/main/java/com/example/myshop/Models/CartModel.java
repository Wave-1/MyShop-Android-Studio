package com.example.myshop.Models;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class CartModel implements Serializable {

    private String productId;
    private String name;
    private double price;
    private String image;
    private int quantity;
    private boolean selected = true;
    @Exclude
    private String orderId;

    @Exclude
    private boolean isReviewed = false;

    public CartModel() {}

    public CartModel(String productId, String name, String image, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Exclude
    public String getOrderId() {
        return orderId;
    }

    @Exclude
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Exclude
    public boolean isReviewed() {
        return isReviewed;
    }

    @Exclude
    public void setReviewed(boolean reviewed) {
        this.isReviewed = reviewed;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Exclude
    public void increaseQuantity() {
        this.quantity++;
    }

    @Exclude
    public void decreaseQuantity() {
        if (quantity > 1) this.quantity--;
    }
}
