// TodayNewsResponse.java
package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TodayNewsResponse {

    private int count;

    @SerializedName("data")   // 🔥 여기가 중요: JSON의 "data" 배열
    private List<NewsItem> data;

    public int getCount() {
        return count;
    }

    public List<NewsItem> getData() {
        return data;
    }
}
