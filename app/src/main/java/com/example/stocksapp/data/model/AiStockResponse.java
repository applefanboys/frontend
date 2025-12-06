package com.example.stocksapp.data.model;

import java.util.List;

public class AiStockResponse {

    // 예: "전체 경제"
    private String user_interest;

    // 예: ["삼성전자", "SK하이닉스"]
    private List<String> candidates_found;

    // 🔥 여기! 이제 문자열이 아니라 AiResult 객체로 받는다.
    private AiResult ai_result;

    public String getUser_interest() {
        return user_interest;
    }

    public List<String> getCandidates_found() {
        return candidates_found;
    }

    public AiResult getAi_result() {
        return ai_result;
    }
}
