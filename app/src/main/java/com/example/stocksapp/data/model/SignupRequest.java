package com.example.stocksapp.data.model;

public class SignupRequest {
    private String email;
    private String username;
    private String password;

    public SignupRequest() {}

    public SignupRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getEmail()     { return email; }
    public String getUsername()  { return username; }
    public String getPassword()  { return password; }

    public void setEmail(String email)       { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
