package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PersonalizedNewsResponse {
    @SerializedName("user_id")
    private int userId;

    private int days;
    private int total;

    private List<NewsItem> articles;

    public int getUserId() {
        return userId;
    }

    public int getDays() {
        return days;
    }

    public int getTotal() {
        return total;
    }

    public List<NewsItem> getArticles() {
        return articles;
    }
}
