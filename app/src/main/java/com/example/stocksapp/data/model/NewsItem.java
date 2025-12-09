package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsItem {

    // 서버에서 내려오는 필드들
    private String title;
    private String summary;
    private String source;

    // 기사 원문 링크
    private String url;

    // 썸네일/원본 이미지 링크 (JSON 키는 origin_url)
    @SerializedName("origin_url")
    private String originUrl;

    // 게시 시각
    @SerializedName("published_at")
    private String publishedAt;

    // (옵션) 개수 등 필요하면 사용
    private int count;

    public NewsItem() {}

    // ===== title =====
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    // ===== summary =====
    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    // ===== source =====
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    // ===== url (기사 링크) =====
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    // ===== originUrl (이미지 링크) =====
    // 새 이름
    public String getOriginUrl() {
        return originUrl;
    }
    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    // 🔁 예전 코드 호환용: getOrigin_url / setOrigin_url 도 같이 둔다
    public String getOrigin_url() {
        return originUrl;
    }
    public void setOrigin_url(String origin_url) {
        this.originUrl = origin_url;
    }

    // ===== publishedAt =====
    public String getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    // ===== count (필요하면 사용) =====
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
}
