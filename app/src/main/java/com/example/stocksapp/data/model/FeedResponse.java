package com.example.stocksapp.data.model;

import java.util.List;

public class FeedResponse {
    // 데이터 변수 선언
    private List<TopicCard> topics;
    private List<NewsItem> news;       // 'personal' 데이터를 이걸로 받음
    private List<NewsItem> trending;
    private List<StockTip> stockTips;  // 'stocks' 데이터를 이걸로 받음
    private String fortune;

    // 🟡 [중요] 생성자: FakeFeedRepository에서 보내주는 순서와 똑같아야 합니다.
    public FeedResponse(List<TopicCard> topics,
                        List<NewsItem> news,
                        List<NewsItem> trending,
                        List<StockTip> stockTips,
                        String fortune) {
        this.topics = topics;
        this.news = news;
        this.trending = trending;
        this.stockTips = stockTips;
        this.fortune = fortune;
    }

    // 🟢 Getter 메서드: MainActivity에서 호출하는 이름과 같아야 합니다.

    public List<TopicCard> getTopics() {
        return topics;
    }

    public List<NewsItem> getNews() { // MainActivity에서 .getNews()로 호출함
        return news;
    }

    public List<NewsItem> getTrending() {
        return trending;
    }

    public List<StockTip> getStockTips() { // MainActivity에서 .getStockTips()로 호출함
        return stockTips;
    }

    public String getFortune() {
        return fortune;
    }
}