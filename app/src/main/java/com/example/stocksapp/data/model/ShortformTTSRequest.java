package com.example.stocksapp.data.model;

public class ShortformTTSRequest {
    private String text;
    private int max_chars;

    public ShortformTTSRequest(String text) {
        this.text = text;
        this.max_chars = 200; // 숏폼 길이에 맞춰 적당히 설정
    }
}