package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsItem {

    // 서버에서 내려오는 필드들
    private String title;
    private String summary;
    private String source;

    // 기사 원문 링크 (일반 URL)
    private String url;

    // 기사 원문 링크 (JSON 키: origin_url)
    // 백엔드에서 origin_url을 "원문 기사 링크"로 내려주는 경우
    @SerializedName("origin_url")
    private String originUrl;

    // 🔥 TTS 서버가 originUrl을 크롤링해서 헤더로 내려주는 "실제 이미지 URL"
    // JSON에는 안 오고, 앱에서 TTS 응답을 보고 채워 넣는 용도
    private String imageUrl;

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

    // ===== originUrl (기사 원문 링크: JSON origin_url) =====
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

    // ===== imageUrl (TTS 헤더에서 가져온 실제 이미지 URL) =====
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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