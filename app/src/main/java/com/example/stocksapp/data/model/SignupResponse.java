package com.example.stocksapp.data.model;

public class SignupResponse {
    private String email;
    private String username;
    private int id;
    private boolean is_active;

    public SignupResponse() {}

    public String getEmail()    { return email; }
    public String getUsername() { return username; }
    public int getId()          { return id; }
    public boolean isIs_active(){ return is_active; }
}
