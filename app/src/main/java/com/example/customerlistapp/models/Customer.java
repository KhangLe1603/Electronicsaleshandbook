package com.example.customerlistapp.models;

public class Customer {
    private String surname;    // Họ và đệm (e.g., "Nguyễn Văn")
    private String firstName;  // Tên (e.g., "A")
    private String address;    // Địa chỉ
    private String phone;      // Số điện thoại

    public Customer(String surname, String firstName, String address, String phone) {
        this.surname = surname;
        this.firstName = firstName;
        this.address = address;
        this.phone = phone;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Optional: Phương thức để lấy full name nếu cần
    public String getFullName() {
        return (surname != null ? surname : "") + " " + (firstName != null ? firstName : "");
    }
}