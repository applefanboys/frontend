package com.example.stocksapp.data.repo

import com.example.stocksapp.data.model.*

objectFakeFeedRepository {
    fun getFeed(selected: List<String>): FeedResponse {
        val topics = listOf(
            TopicCard("반도체 수급 개선", "https://picsum.photos/800/400?1"),
            TopicCard("연준 발언 요약", "https://picsum.photos/800/400?2"),
            TopicCard("AI 서버 투자 확대", "https://picsum.photos/800/400?3")
        )
        val personal = listOf(
            NewsItem("p1","삼성전자 HBM 증설 이슈","https://example.com/a"),
            NewsItem("p2","엔비디아 AI 칩 수요 분석","https://example.com/b"),
            NewsItem("p3","국내 2차전지 업황 점검","https://example.com/c"),
            NewsItem("p4","달러 약세와 수출 전망","https://example.com/d")
        )
        val trending = listOf(
            NewsItem("t1","오늘의 인기: 금리 동결","https://example.com/t1"),
            NewsItem("t2","코스피 급등 배경","https://example.com/t2"),
            NewsItem("t3","테슬라 공시 이슈","https://example.com/t3"),
            NewsItem("t4","원자재 가격 급등","https://example.com/t4")
        )
        val stocks = listOf(
            StockTip("NVDA","분기 실적 발표 임박"),
            StockTip("TSLA","交付량 컨센서스 하향"),
            StockTip("005930","HBM 관련 수혜 기대")
        )
        val fortune = "새로운 기회를 포착하기 좋은 날"
        return FeedResponse(topics, personal, trending, stocks, fortune)
    }
}
