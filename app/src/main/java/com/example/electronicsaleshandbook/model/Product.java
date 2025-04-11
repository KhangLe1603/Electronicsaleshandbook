package com.example.electronicsaleshandbook.model;

import java.io.Serializable;

public class Product implements Serializable {
    private String id;
    private String name;
    private String price;
    private String unitPrice; // Đơn giá
    private String unit;
    private String description;
    private int sheetRowIndex;

    public Product(String name, String description,String unitPrice, String price, String unit) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.unitPrice = unitPrice;
        this.unit = unit;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() {
        return name;
    }
    public String getDescription() { return description; }
    public String getUnitPrice() { return unitPrice; }
    public String getPrice() { return price ; }
    public String getUnit() { return unit; }
    public void setSheetRowIndex(int sheetRowIndex) { this.sheetRowIndex = sheetRowIndex; }
    public int getSheetRowIndex() {return sheetRowIndex;}
}
