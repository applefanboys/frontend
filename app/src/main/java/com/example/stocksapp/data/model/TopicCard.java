package com.example.stocksapp.data.model;

public class TopicCard {
    private String title;
    private String imageUrl;

    // 🔴 [중요] 이 생성자가 없어서 에러가 났던 것입니다.
    public TopicCard(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    // Getter 메서드
    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}