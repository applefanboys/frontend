package com.example.stocksapp.data.model;

public class NewsItem {
    private String id;
    private String title;
    private String url; // 또는 imageUrl

    // 🔴 생성자 추가
    public NewsItem(String id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }

    public String getTitle() { return title; }
    public String getUrl() { return url; }
    // ... 나머지 getter
}