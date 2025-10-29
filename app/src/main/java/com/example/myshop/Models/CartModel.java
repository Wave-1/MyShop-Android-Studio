package com.example.myshop.Models;

import java.io.Serializable;

public class CartModel implements Serializable {
    private String id;
    private String name;
    private double price;
    private String image;
    private boolean selected = true;

    private int quantity;

    public CartModel(){}

    public CartModel(String id, String name, double price, String image, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.selected = true;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public void decreaseQuantity() {
        if (quantity > 1) this.quantity--;
    }
}
