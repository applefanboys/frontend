package com.example.stocksapp.data.model;

public class LoginResponse {
    private UserRead user;
    private String message;

    public UserRead getUser() { return user; }
    public String getMessage() { return message; }
}