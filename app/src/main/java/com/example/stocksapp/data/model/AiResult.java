package com.example.stocksapp.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AiResult {

    @SerializedName("recommended_stock")
    @Expose
    private String recommendedStock;

    @SerializedName("stock_code")
    @Expose
    private String stockCode;

    @SerializedName("reason")
    @Expose
    private String reason;

    public String getRecommendedStock() {
        return recommendedStock;
    }

    public String getStockCode() {
        return stockCode;
    }

    public String getReason() {
        return reason;
    }
}
