package com.example.myshop.Models;

public class PaymentMethod {
    private int id;
    private String name;
    private String description;
    private boolean selected;

    public PaymentMethod(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.selected = false;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}

