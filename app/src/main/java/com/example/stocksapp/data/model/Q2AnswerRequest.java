// Q2AnswerRequest.java
package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Q2AnswerRequest {

    @SerializedName("keywords")
    private List<String> keywords;

    public Q2AnswerRequest(List<String> keywords) {
        this.keywords = keywords;
    }
}
