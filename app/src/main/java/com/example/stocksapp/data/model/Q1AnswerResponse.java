// Q1AnswerResponse.java  (굳이 안 써도 되지만 형식 맞춰둠)
package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Q1AnswerResponse {

    @SerializedName("selected_categories")
    private List<Category> selectedCategories;

    public List<Category> getSelectedCategories() {
        return selectedCategories;
    }
}
