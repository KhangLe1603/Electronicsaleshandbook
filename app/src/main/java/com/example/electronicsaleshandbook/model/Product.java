package com.example.electronicsaleshandbook.model;

import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private String price;
    private String unitPrice; // Đơn giá
    private String unit;
    private String description;

    public Product(String name, String description,String unitPrice, String price, String unit) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.unitPrice = unitPrice;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }
    public String getDescription() { return description; }
    public String getUnitPrice() { return unitPrice; }
    public String getPrice() { return price ; }
    public String getUnit() { return unit; }
}
