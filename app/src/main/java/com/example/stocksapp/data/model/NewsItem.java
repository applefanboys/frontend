package com.example.stocksapp.data.model;

public class NewsItem {
    private int count;
    private String title;
    private String summary;
    private String source;
    private String url; // 또는 imageUrl
    private String origin_url;

    // 🔴 생성자 추가
    public NewsItem() {}

    public String getTitle() { return title; }
    public void SetTitle(String title) { this.title = title; }

    public int getCount() { return count; }

    public void setCount(int count) { this.count = count; }

    public String getUrl() { return url; }
    public void SetUrl(String url) { this.url = url; }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOrigin_url() {
        return origin_url;
    }

    public void setOrigin_url(String origin_url) {
        this.origin_url = origin_url;
    }
}