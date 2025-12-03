package com.example.stocksapp.data.model;

public class UserRead {
    private int id;
    private String email;
    private String username;
    private boolean is_active;

    // Getter
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public boolean isActive() { return is_active; }
}