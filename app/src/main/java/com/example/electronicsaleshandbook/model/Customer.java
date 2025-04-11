package com.example.electronicsaleshandbook.model;

import java.io.Serializable;

public class Customer implements Serializable {
    private String id;
    private String surname;    // Họ và đệm (e.g., "Nguyễn Văn")
    private String firstName;  // Tên (e.g., "A")
    private String address;    // Địa chỉ
    private String phone;      // Số điện thoại
    private String email;      // Email
    private String birthday;   // Ngày sinh
    private String gender;
    private int sheetRowIndex;

    public Customer(String surname, String firstName, String address, String phone) {
        this.surname = surname;
        this.firstName = firstName;
        this.address = address;
        this.phone = phone;
    }
    public Customer(String surname, String firstName, String address, String phone,
                    String email, String birthday, String gender) {
        this.surname = surname;
        this.firstName = firstName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    // Optional: Phương thức để lấy full name nếu cần
    public String getFullName() {
        return (surname != null ? surname : "") + " " + (firstName != null ? firstName : "");
    }

    public int getSheetRowIndex() { return sheetRowIndex; }
    public void setSheetRowIndex(int sheetRowIndex) { this.sheetRowIndex = sheetRowIndex; }
}