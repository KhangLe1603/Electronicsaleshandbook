package com.example.electronicsaleshandbook.model;

public class AuthResult {
    private boolean isSuccess;
    private String message;
    private String userId; // Optional: Store user ID if needed

    public AuthResult(boolean isSuccess, String message, String userId) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.userId = userId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }
}