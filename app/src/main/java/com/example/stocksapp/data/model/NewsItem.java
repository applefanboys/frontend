package com.example.stocksapp.data.model;

import java.io.Serializable;

public class NewsItem implements Serializable {
    private int id;
    private String title;
    private String summary;
    private String thumbnail_url;
    private String news_url;
    private String date;
    private String press;

    // 생성자 (기존과 동일하게 유지)
    public NewsItem(int id, String title, String summary, String thumbnail_url, String news_url, String date, String press) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.thumbnail_url = thumbnail_url;
        this.news_url = news_url;
        this.date = date;
        this.press = press;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getThumbnailUrl() { return thumbnail_url; }
    public String getNewsUrl() { return news_url; }
    public String getDate() { return date; }
    public String getPress() { return press; }

    // 🔴 [핵심 수정] Setter 추가: 이 메서드가 없어서 "cannot find symbol setSummary(String)" 에러가 발생했습니다.
    public void setSummary(String summary) {
        this.summary = summary;
    }
}