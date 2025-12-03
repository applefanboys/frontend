package com.example.stocksapp.data.model;

public class OnboardingStatus {
    private boolean q1_completed;
    private boolean q2_completed; // 필수 단계 (이것만 해도 통과)
    private boolean q3_completed; // 선택 단계

    public boolean isQ1_completed() {
        return q1_completed;
    }

    // [NEW] 이 메서드가 필요합니다!
    public boolean isQ2_completed() {
        return q2_completed;
    }

    public boolean isQ3_completed() {
        return q3_completed;
    }
}