package com.example.stocksapp.data.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("id")
    private int id;

    @SerializedName("key")
    private String key;

    @SerializedName("label")
    private String label;

    @SerializedName("description")
    private String description;

    // ⭐ 반드시 있어야 Gson이 값을 넣을 수 있음
    public Category() {}

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
