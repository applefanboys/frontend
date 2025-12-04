// Q3AnswerResponse.java
package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Q3AnswerResponse {

    @SerializedName("exclude_keywords")
    private List<String> excludeKeywords;

    public List<String> getExcludeKeywords() {
        return excludeKeywords;
    }
}
