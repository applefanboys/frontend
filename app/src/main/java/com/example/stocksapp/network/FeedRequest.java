package com.example.stocksapp.network;

import java.util.List;

public class FeedRequest {
    private String userId;
    private List<String> tickers;

    public FeedRequest(String userId, List<String> tickers) {
        this.userId = userId;
        this.tickers = tickers;
    }
}
