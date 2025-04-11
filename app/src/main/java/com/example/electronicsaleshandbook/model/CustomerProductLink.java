package com.example.electronicsaleshandbook.model;

import java.io.Serializable;

public class CustomerProductLink implements Serializable {
    private String customerId;
    private String productId;
    private int sheetRowIndex;

    public CustomerProductLink(String customerId, String productId) {
        this.customerId = customerId;
        this.productId = productId;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getSheetRowIndex() { return sheetRowIndex; }
    public void setSheetRowIndex(int sheetRowIndex) { this.sheetRowIndex = sheetRowIndex; }
}