package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsItem {

    // 이 count는 API JSON에는 없고,
    // FakeFeed 등에서 편하게 쓰려고 남겨둔 로컬 필드라고 보면 됨
    private int count;

    @SerializedName("title")
    private String title;

    @SerializedName("summary")
    private String summary;

    @SerializedName("source")
    private String source;

    @SerializedName("url")
    private String url;

    // JSON 키는 origin_url
    @SerializedName("origin_url")
    private String originUrl;

    // 스펙에는 "publised_at" 로 되어 있고,
    // perfectNews 코드에는 "published_at" 이라서 둘 다 받게 설정
    @SerializedName(value = "publised_at", alternate = {"published_at"})
    private String publishedAt;

    // 기본 생성자
    public NewsItem() {}

    // 간단 생성자 (UI/FakeFeed에서 많이 쓸 거)
    public NewsItem(String title, String summary, String url) {
        this.title = title;
        this.summary = summary;
        this.url = url;
    }

    public NewsItem(String title,
                    String summary,
                    String url,
                    String originUrl,
                    String source,
                    int count) {
        this.title = title;
        this.summary = summary;
        this.url = url;
        this.originUrl = originUrl;
        this.source = source;
        this.count = count;
    }

    // ===== Getter / Setter =====

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {   // SetTitle -> setTitle
        this.title = title;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {       // SetUrl -> setUrl
        this.url = url;
    }

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

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}
