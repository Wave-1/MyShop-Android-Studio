package com.example.myshop.Models;

import java.io.Serializable;

public class ProductModel implements Serializable {
    private String productId;
    private String name;
    private double price;
    private String image;
    private String description;
    private int salesCount;
    private int salePercent;
    private String category;

    public ProductModel() {
    }

    public ProductModel(String productId, String name, double price, String image, String description, int salesCount, String category) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.salesCount = salesCount;
        this.salePercent = salePercent;
        this.category = category;
    }

    public double getSalePrice() {
        if (salePercent > 0) {
            return price - (price * salePercent / 100.0);
        }
        return price;
    }

    public boolean isOnSale() {
        return salePercent > 0;
    }

    public int getSalePercent() {
        return salePercent;
    }

    public void setSalePercent(int salePercent) {
        this.salePercent = salePercent;
    }
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
