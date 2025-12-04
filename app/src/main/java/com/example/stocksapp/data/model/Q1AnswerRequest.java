// Q1AnswerRequest.java
package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Q1AnswerRequest {

    @SerializedName("selected_category_ids")
    private List<Integer> selectedCategoryIds;

    public Q1AnswerRequest(List<Integer> selectedCategoryIds) {
        this.selectedCategoryIds = selectedCategoryIds;
    }
}
