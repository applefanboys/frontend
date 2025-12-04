// Q2AnswerResponse.java
package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Q2AnswerResponse {

    @SerializedName("keywords")
    private List<String> keywords;

    public List<String> getKeywords() {
        return keywords;
    }
}
