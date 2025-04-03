package com.example.electronicsaleshandbook.model;

public class Products {
    private String name;
    private String price;

    public Products(String name, String price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price ;
    }
}
