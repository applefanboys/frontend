package com.example.stocksapp.data.model;

// AiStockResponse 안에 들어있는 ai_result(문자열 JSON)를 파싱한 결과
public class AiResult {

    // 예: "삼성전자"
    private String recommended_stock;

    // 예: "005930"
    private String stock_code;

    // 예: "반도체 업황 개선 기대..."
    private String reason;

    public String getRecommended_stock() {
        return recommended_stock;
    }

    public String getStock_code() {
        return stock_code;
    }

    public String getReason() {
        return reason;
    }
}
