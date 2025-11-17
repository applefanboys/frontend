package com.example.stocksapp.data.model;

import java.util.List;

public class FeedResponse {
    private List<NewsItem> news;
    private List<StockTip> stockTips;
    private List<TopicCard> topics;
    private List<Keyword> keywords;

    public List<NewsItem> getNews() {
        return news;
    }

    public List<StockTip> getStockTips() {
        return stockTips;
    }

    public List<TopicCard> getTopics() {
        return topics;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }
}
