package com.example.stocksapp.data.repo;

import com.example.stocksapp.data.model.FeedResponse;
import com.example.stocksapp.data.model.NewsItem;
import com.example.stocksapp.data.model.StockTip;
import com.example.stocksapp.data.model.TopicCard;

import java.util.ArrayList;
import java.util.List;

public class FakeFeedRepository {

    // 인자 없이 호출할 수 있도록 수정함
    public static FeedResponse getFeed() {
        List<TopicCard> topics = new ArrayList<>();
        topics.add(new TopicCard("반도체 수급 개선", "https://picsum.photos/800/400?1"));
        topics.add(new TopicCard("연준 발언 요약", "https://picsum.photos/800/400?2"));
        topics.add(new TopicCard("AI 서버 투자 확대", "https://picsum.photos/800/400?3"));

        List<NewsItem> personal = new ArrayList<>();
        personal.add(new NewsItem("p1", "삼성전자 HBM 증설 이슈", "https://example.com/a"));
        personal.add(new NewsItem("p2", "엔비디아 AI 칩 수요 분석", "https://example.com/b"));
        personal.add(new NewsItem("p3", "국내 2차전지 업황 점검", "https://example.com/c"));
        personal.add(new NewsItem("p4", "달러 약세와 수출 전망", "https://example.com/d"));

        List<NewsItem> trending = new ArrayList<>();
        trending.add(new NewsItem("t1", "오늘의 인기: 금리 동결", "https://example.com/t1"));

        List<StockTip> stocks = new ArrayList<>();
        stocks.add(new StockTip("NVDA", "분기 실적 발표 임박"));
        stocks.add(new StockTip("TSLA", "교부량 컨센서스 하향"));
        stocks.add(new StockTip("005930", "HBM 관련 수혜 기대"));

        String fortune = "새로운 기회를 포착하기 좋은 날";

        // FeedResponse 생성자 순서에 맞춰서 리턴
        return new FeedResponse(topics, personal, trending, stocks, fortune);
    }
}