// com.example.stocksapp.data.model.ResetPasswordRequest

package com.example.stocksapp.data.model;

public class ResetPasswordRequest {
    private String token;
    private String new_password; // 서버 필드명에 맞게!

    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.new_password = newPassword;
    }
}
