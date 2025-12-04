// Q3AnswerRequest.java
package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Q3AnswerRequest {

    @SerializedName("exclude_keywords")
    private List<String> excludeKeywords;

    public Q3AnswerRequest(List<String> excludeKeywords) {
        this.excludeKeywords = excludeKeywords;
    }
}
