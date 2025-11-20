package com.example.stocksapp.data.model;

public class StockTip {
    private String symbol;       // 종목명 (예: NVDA)
    private String description;  // 설명

    // 생성자
    public StockTip(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    // Getter 메서드들
    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    // 🔴 [중요] 에러 해결을 위해 추가한 부분
    // 어댑터가 getTitle()을 찾고 있으므로, symbol(종목명)을 제목으로 넘겨줍니다.
    public String getTitle() {
        return symbol;
    }
}