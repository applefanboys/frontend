package com.example.stocksapp.data.model;

import java.util.List;

public class PreferenceRequest {
    private String q1_text;
    private List<String> include_keywords;
    private List<String> exclude_keywords;

    public PreferenceRequest(String q1_text, List<String> include_keywords, List<String> exclude_keywords) {
        this.q1_text = q1_text;
        this.include_keywords = include_keywords;
        this.exclude_keywords = exclude_keywords;
    }

    // Getter가 필요할 경우를 대비해 추가 (Retrofit은 필드 직접 접근도 가능하지만 안전하게)
    public String getQ1_text() { return q1_text; }
    public List<String> getInclude_keywords() { return include_keywords; }
    public List<String> getExclude_keywords() { return exclude_keywords; }
}