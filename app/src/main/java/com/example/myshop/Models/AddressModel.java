package com.example.myshop.Models;

import java.io.Serializable;

public class AddressModel implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String city;
    private String district;
    private String ward;
    private String addressLine;
    private boolean isDefault;

    public AddressModel() {
    }

    public AddressModel(String id, String name, String phone, String city, String district, String ward, String addressLine, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.addressLine = addressLine;
        this.isDefault = isDefault;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
