package com.example.stocksapp.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AiStockResponse {

    @SerializedName("user_interest")
    @Expose
    private String userInterest;

    @SerializedName("candidates_found")
    @Expose
    private List<String> candidatesFound;

    // 🔥 ai_result 는 JSON 객체이므로 String 말고 AiResult 로 선언
    @SerializedName("ai_result")
    @Expose
    private AiResult aiResult;

    public String getUserInterest() {
        return userInterest;
    }

    public List<String> getCandidatesFound() {
        return candidatesFound;
    }

    public AiResult getAiResult() {
        return aiResult;
    }
}
