package com.example.stocksapp.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class KeywordResponse {

    @SerializedName("keywords")
    private List<String> keywords;  // 서버에서 넘어오는 "keywords" 배열

    public List<String> getKeywords() {
        return keywords;
    }
}
