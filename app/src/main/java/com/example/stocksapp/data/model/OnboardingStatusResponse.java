// OnboardingStatusResponse.java
package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;

public class OnboardingStatusResponse {

    @SerializedName("q1_completed")
    private boolean q1Completed;

    @SerializedName("q2_completed")
    private boolean q2Completed;

    @SerializedName("q3_completed")
    private boolean q3Completed;

    public boolean isQ1Completed() {
        return q1Completed;
    }

    public boolean isQ2Completed() {
        return q2Completed;
    }

    public boolean isQ3Completed() {
        return q3Completed;
    }

    /** 세 질문 다 끝났는지 편하게 체크용 */
    public boolean isAllCompleted() {
        return q1Completed && q2Completed && q3Completed;
    }

    /** 다음에 가야 할 스텝 계산 (1,2,3, 혹은 4=끝) */
    public int getNextStep() {
        if (!q1Completed) return 1;
        if (!q2Completed) return 2;
        if (!q3Completed) return 3;
        return 4; // 전부 완료
    }
}
