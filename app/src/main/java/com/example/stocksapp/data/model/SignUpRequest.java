package com.example.stocksapp.data.model;

public class SignUpRequest {
    private String email;
    private String username; // 앱의 닉네임
    private String password;

    public SignUpRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}